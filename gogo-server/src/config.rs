use log::{error, info};
use once_cell::sync::{Lazy, OnceCell};
use rand::Rng;
use reqwest::Client;
use serde::{Deserialize, Serialize};
use std::time::SystemTime;
use std::cell::RefCell;
use std::{collections::HashMap, fs::File, time::Duration};

#[derive(Deserialize, Serialize)]
pub struct SearchRequest {
    pub q: String,
    pub p: Option<u16>,
}

#[derive(Deserialize, Serialize)]
pub struct ResultEntry {
    pub name: String,
    pub url: String,
    pub desc: Option<String>,
}

#[derive(Deserialize, Serialize)]
pub struct GogoResponse<T> {
    pub error: Option<String>,
    pub result: Option<T>,
}

pub struct GoogleSearchContext {
    user_agent_index: usize,
    user_agent_index_ttl: u64,
    created_at: SystemTime,
}

static DEFAULT_USER_AGENT: &str = "Lynx/2.8.5rel.2 libwww-FM";

impl GoogleSearchContext {
    fn new() -> GoogleSearchContext {
        let config = CONFIG.get().expect("config is not initialized");
        let max_idx = (config.user_agents.len() - 1) as u64;
        GoogleSearchContext {
            user_agent_index: rand::thread_rng().gen_range(0..max_idx) as usize,
            user_agent_index_ttl: rand::thread_rng().gen_range(0..100) + 500,
            created_at: SystemTime::now(),
        }
    }

    /// Returns user agent
    pub fn user_agent(&mut self) -> &'static str {
        let config = CONFIG.get().expect("config is not initialized");
        let elapsed = self.created_at.elapsed().expect("get time error").as_secs();
        if elapsed <= self.user_agent_index_ttl {
            return match config.user_agents.get(self.user_agent_index) {
                Some(m) => m,
                None => DEFAULT_USER_AGENT,
            };
        } else {
            self.user_agent_index = if config.user_agents.len() - 1 == self.user_agent_index {
                info!("");
                0
            } else {
                self.user_agent_index + 1
            };
            return match config.user_agents.get(0) {
                Some(m) => m,
                None => DEFAULT_USER_AGENT,
            };
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Config {
    pub listen_address: String,
    pub google_base_url: String,
    pub static_path: String,
    pub http_client_pool_max_idle_per_host: usize,
    pub http_client_connect_timeout_millis: u64,
    pub danger_accept_invalid_certs: bool,
    pub user_agents: Vec<String>,
    pub headers: Option<HashMap<String, String>>,
    pub proxy: Option<String>,
    pub suggest_user_agent: String,
}

pub static CONFIG: OnceCell<Config> = OnceCell::new();

pub static HTTP_CLIENT: Lazy<Client> = Lazy::new(|| {
    let config = CONFIG.get().expect("config is not initialized");
    let mut client_builder = reqwest::ClientBuilder::new()
        .connect_timeout(Duration::from_millis(
            config.http_client_connect_timeout_millis,
        ))
        .danger_accept_invalid_certs(true)
        .connection_verbose(true)
        .pool_max_idle_per_host(config.http_client_pool_max_idle_per_host);
    match &config.proxy {
        Some(proxy_scheme) => {
            info!("proxy config detected: {}", proxy_scheme);
            client_builder = client_builder
                .proxy(reqwest::Proxy::all(proxy_scheme).expect("proxy config failed"));
        }
        None => {}
    }
    client_builder.build().expect("build client")
});

thread_local! {
   pub static SEARCH_CTX: RefCell<GoogleSearchContext> = RefCell::new(GoogleSearchContext::new());
}

pub fn init_config(config_file: File) {
    let config: Config = serde_json::from_reader(config_file).expect("file should be proper JSON");
    if config.user_agents.len() == 0 {
        panic!("user_agents cannot be empty!");
    }
    match CONFIG.set(config) {
        Ok(_) => {
            info!("config succeed: {:?}", CONFIG)
        }
        Err(_) => {
            error!("config already initialized")
        }
    };
}

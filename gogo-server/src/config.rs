use log::{error, info};
use once_cell::sync::{Lazy, OnceCell};
use reqwest::Client;
use serde::{Deserialize, Serialize};
use std::sync::atomic::AtomicU8;
use std::time::SystemTime;
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
    pub query_kv_list: Vec<(String, String)>,
    pub created: SystemTime,
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
                .proxy(reqwest::Proxy::http(proxy_scheme).expect("proxy config failed"));
        }
        None => {}
    }
    client_builder.build().expect("build client")
});

pub static USER_AGENT_INDEX: AtomicU8 = AtomicU8::new(0);

thread_local! {
   pub static USER_AGENT_TO_SEARCH_CTX: HashMap<&'static str, GoogleSearchContext> = HashMap::new();
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

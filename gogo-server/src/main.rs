use clap::Parser;
use html5ever::tendril::TendrilSink;
use log::{error, info, trace};
use once_cell::sync::{Lazy, OnceCell};
use reqwest::header::{HeaderMap, HeaderName, HeaderValue};
use reqwest::{Client, RequestBuilder};
use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::convert::Infallible;
use std::net::SocketAddr;
use std::sync::atomic::AtomicU8;
use std::{
    collections::{HashMap, VecDeque},
    fs::File,
    str::FromStr,
    time::Duration,
};
use url::Url;
use warp::{Filter, Rejection, Reply};

#[derive(Deserialize, Serialize)]
struct SearchRequest {
    q: String,
    p: Option<u16>,
}

#[derive(Deserialize, Serialize)]
struct ResultEntry {
    name: String,
    url: String,
    desc: Option<String>,
}

#[derive(Deserialize, Serialize)]
struct GogoResponse<T> {
    error: Option<String>,
    result: Option<T>,
}

struct GoogleSearchContext {
    query_kv_list: Vec<(String, String)>,
}

#[derive(Serialize, Deserialize, Debug)]
struct Config {
    listen_address: String,
    google_base_url: String,
    static_path: String,
    http_client_pool_max_idle_per_host: usize,
    http_client_connect_timeout_millis: u64,
    danger_accept_invalid_certs: bool,
    user_agents: Vec<String>,
    headers: Option<HashMap<String, String>>,
    proxy: Option<String>,
    suggest_user_agent: String,
}

#[derive(Parser)]
struct Args {
    config: String,
}

static CONFIG: OnceCell<Config> = OnceCell::new();

static HTTP_CLIENT: Lazy<Client> = Lazy::new(|| {
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

static HEADERS: Lazy<HeaderMap<HeaderValue>> = Lazy::new(|| {
    let config = CONFIG.get().expect("config is not initialized");
    match &config.headers {
        Some(m) => {
            let mut header_map: HeaderMap<HeaderValue> = HeaderMap::with_capacity(m.len() + 1);
            for (k, v) in m.into_iter() {
                match HeaderName::from_str(k) {
                    Ok(h) => match HeaderValue::from_str(v) {
                        Ok(header_value) => {
                            header_map.append(h, header_value);
                        }
                        Err(_) => {}
                    },
                    Err(_) => {}
                };
            }
            info!("header_map:{:?}", header_map);
            header_map
        }
        None => HeaderMap::new(),
    }
});

static USER_AGENT_INDEX: AtomicU8 = AtomicU8::new(0);

#[tokio::main]
async fn main() {
    let _ = pretty_env_logger::try_init();

    let args = Args::parse();
    let config_file = File::open(args.config).expect("config file should open read only");
    init_config(config_file);
    let config = CONFIG.get().expect("config is not initialized");
    let listen_address: SocketAddr =
        SocketAddr::from_str(&config.listen_address).expect("Invalid listen address");
    let search = warp::path("search")
        .and(warp::query::<SearchRequest>())
        .and_then(render_response_search);
    let suggest = warp::path("lint")
        .and(warp::query::<SearchRequest>())
        .and_then(render_response_suggest);
    let routes = warp::path("api")
        .and(search.or(suggest).recover(recover_api))
        .or(
            warp::fs::dir(&config.static_path).or(warp::fs::file(format!(
                "{}/index.html",
                &config.static_path
            ))),
        );
    warp::serve(routes).run(listen_address).await;
}

async fn recover_api(_: Rejection) -> Result<impl Reply, Infallible> {
    Ok(warp::http::StatusCode::NOT_FOUND)
}

fn init_config(config_file: File) {
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

fn user_agent() -> &'static str {
    let index_value = USER_AGENT_INDEX.fetch_add(1, std::sync::atomic::Ordering::Relaxed);
    if index_value > 199 {
        USER_AGENT_INDEX.store(0, std::sync::atomic::Ordering::SeqCst);
    }
    let config = CONFIG.get().expect("config is not initialized");
    let user_agent = match config
        .user_agents
        .get((index_value as usize) % config.user_agents.len())
    {
        Some(m) => m,
        None => "Lynx/2.8.5rel.2 libwww-FM",
    };
    return user_agent;
}

async fn fetch(request: RequestBuilder) -> Result<String, reqwest::Error> {
    let res = request
        .headers(HEADERS.clone())
        .send()
        .await?
        .text()
        .await?;
    Ok(res)
}

async fn render_response_suggest(
    request: SearchRequest,
) -> Result<impl warp::Reply, warp::Rejection> {
    let config = CONFIG.get().expect("config is not initialized");
    let http_request = HTTP_CLIENT
        .get(format!("{}/complete/search", config.google_base_url))
        .query(&[("q", request.q), ("client", "psy-ab".to_string())])
        .header("user-agent", config.suggest_user_agent.clone());
    match fetch(http_request).await {
        Ok(body) => {
            let json_value: Value = serde_json::from_str(&body).expect("invalid complete json");
            let array: &Vec<Value> = json_value[1].as_array().expect("without second item");
            let suggestions: Vec<&str> = array
                .iter()
                .map(|i| {
                    i.as_array().expect("without first item")[0]
                        .as_str()
                        .expect("failed grt string value")
                })
                .collect();
            let response = GogoResponse {
                error: None,
                result: Some(suggestions),
            };
            Ok(warp::reply::json(&response))
        }
        Err(_) => Err(warp::reject()),
    }
}

async fn render_response_search(
    request: SearchRequest,
) -> Result<impl warp::Reply, warp::Rejection> {
    let config = CONFIG.get().expect("config is not initialized");
    let start = match request.p {
        Some(v) => (v - 1) * 10,
        None => 0,
    };
    let http_request = HTTP_CLIENT
        .get(format!("{}/search", config.google_base_url))
        .query(&[("q", request.q), ("start", start.to_string())])
        .header("user-agent", user_agent());
    match fetch(http_request).await {
        Ok(body) => {
            trace!("search response: {}", body);
            let result_enteries = parse_result_entry(body);
            let response = GogoResponse {
                error: None,
                result: Some(result_enteries),
            };
            Ok(warp::reply::json(&response))
        }
        Err(_err) => Err(warp::reject()),
    }
}

async fn fetch_search_context(user_agent: &'static str) {
    let config = CONFIG.get().expect("config is not initialized");
    let http_request = HTTP_CLIENT
        .get(format!("{}", config.google_base_url))
        .header("user-agent", user_agent);
    match fetch(http_request).await {
        Ok(body) => {
            let gsc = parse_search_context(body);
            println!("{:?}",gsc.query_kv_list);
        },
        Err(_) => {}
    }
}

const google_search_query_keys: Lazy<Vec<&str>> = Lazy::new(|| {
    vec!["ie", "hl", "source", "btnG", "iflsig", "gbv"]
});

fn parse_search_context(body: String) -> GoogleSearchContext {
    let mut query: Vec<(String, String)> = Vec::new();
    let document = kuchiki::parse_html().one(body);
    match document.select("form") {
        Ok(form_select) => {
            for form_ndr in form_select {
                let attr = form_ndr.attributes.borrow();
                let action = attr.get("action");
                if action.is_none() || action.unwrap().ne("/search") {
                    continue;
                }
                match form_ndr.as_node().select("input") {
                    Ok(input_select) => {
                        for input_ndr in input_select {
                            let input_ndr_attr = input_ndr.attributes.borrow();
                            let name_opt = input_ndr_attr.get("name");
                            if name_opt.is_none() {
                                continue;
                            }
                            let name = name_opt.unwrap();
                            if !google_search_query_keys.contains(&name) {
                                continue;
                            }
                            match input_ndr_attr.get("value") {
                                Some(value) => {
                                    query.push((name.to_string(), value.to_string()));
                                }
                                None => {}
                            }
                        }
                    }
                    Err(_) => {}
                }
            }
        }
        Err(_) => {}
    }
    GoogleSearchContext { query_kv_list: query }
}

const FAKE_BASE_URL: Lazy<Url> = Lazy::new(|| Url::parse("http://a").unwrap());

fn parse_result_entry(body: String) -> VecDeque<ResultEntry> {
    let mut result_enteries: VecDeque<ResultEntry> = VecDeque::new();

    let document = kuchiki::parse_html().one(body);

    for nd in document.select("a").unwrap() {
        let attr = nd.attributes.borrow();
        let href = attr.get("href");
        if href.is_none() {
            continue;
        }
        let url = href.unwrap();
        if !url.starts_with("/url?") {
            continue;
        }
        let node = nd.as_node();
        if node.children().count() == 0 {
            continue;
        }
        let fc = node.first_child().unwrap();
        let hash_query: HashMap<_, _> = FAKE_BASE_URL
            .join(url)
            .unwrap()
            .query_pairs()
            .into_owned()
            .collect();
        let parent = node.parent().unwrap().parent().unwrap();
        let desc = if parent.children().count() >= 2 {
            Some(
                parent
                    .children()
                    .last()
                    .unwrap()
                    .text_contents()
                    .trim()
                    .to_string(),
            )
        } else {
            None
        };
        match fc.first_child() {
            Some(c) => {
                let re = ResultEntry {
                    name: c.text_contents(),
                    url: hash_query.get("q").unwrap().to_string(),
                    desc,
                };
                result_enteries.push_back(re);
            }
            None => {}
        }
    }
    result_enteries
}

#[cfg(test)]
mod tests {
    use serde_json::Value;

    use crate::fetch;
    use crate::init_config;
    use crate::parse_result_entry;
    use crate::fetch_search_context;
    use crate::user_agent;
    use crate::CONFIG;
    use crate::HTTP_CLIENT;
    use std::thread;
    use std::{fs::File, io::Read, path::Path};

    #[test]
    fn parse_result_entry_works() {
        for page in std::fs::read_dir("test/webpage").unwrap() {
            let path = page.unwrap().path();
            let body = read_file(path.as_path());
            let result = parse_result_entry(body);
            println!("{},len:{}", path.display(), result.len())
        }
    }

    #[tokio::test]
    async fn fetch_works() {
        init_config(File::open("config.json").expect("Unable to open file: config.json"));
        let config = CONFIG.get().expect("config is not initialized");
        let http_request = HTTP_CLIENT
            .get(format!("{}/search", config.google_base_url))
            .query(&[("q", "udp"), ("start", "0")])
            .header("user-agent", user_agent());
        let result = fetch(http_request).await;
        assert!(result.is_ok());
    }

    #[tokio::test]
    async fn fetch_search_context_works() {
        init_config(File::open("config.json").expect("Unable to open file: config.json"));
        fetch_search_context(user_agent()).await;
    }

    #[test]
    fn suggest_works() {
        for page in std::fs::read_dir("test/suggest").unwrap() {
            let path = page.unwrap().path();
            let body = read_file(path.as_path());
            let json_value: Value = serde_json::from_str(&body).expect("invalid complete json");
            let array: &Vec<Value> = json_value[1].as_array().expect("without second item");
            let suggestions: Vec<&str> = array
                .iter()
                .map(|i| {
                    i.as_array().expect("without first item")[0]
                        .as_str()
                        .expect("msg")
                })
                .collect();
            assert!(suggestions.len() != 0);
        }
    }

    #[test]
    fn user_agent_works() {
        let nthreads = 12;
        let mut children = vec![];
        for _ in 0..nthreads {
            children.push(thread::spawn(move || {
                for _ in 1..1000 {
                    let _ = user_agent();
                }
            }));
        }

        for child in children {
            // Wait for the thread to finish. Returns a result.
            let _ = child.join();
        }
    }

    fn read_file(path: &Path) -> String {
        let mut file = File::open(path).expect("Unable to open file");
        let mut buf = vec![];
        file.read_to_end(&mut buf).expect("read file");
        String::from_utf8_lossy(&buf).to_string()
    }
}

use reqwest::header;
use serde::{Deserialize, Serialize};
use std::{collections::LinkedList, time::Duration};
use warp::Filter;

#[tokio::main]
async fn main() {
    pretty_env_logger::init();

    let hi = warp::path("hello")
        .and(warp::path::param())
        .and(warp::header("user-agent"))
        .map(|param: String, agent: String| format!("Hello {}, whose agent is {}", param, agent));
    let hello_world = warp::path::end().map(|| "Hello, World at root!");

    let routes = warp::get().and(hello_world.or(hi));
    let mut headers = header::HeaderMap::new();
    headers.insert(
        "user-agent",
        header::HeaderValue::from_static(
            "Mozilla/5.0 (Mobile; Nokia 8110 4G; rv:46.0) Gecko/46.0 Firefox/46.0 KAIOS/2.5",
        ),
    );
    let proxy = reqwest::Proxy::http("socks5://127.0.0.1:1080").expect("socks proxy");
    let http_client = reqwest::ClientBuilder::new()
        .connect_timeout(Duration::from_secs(60))
        .default_headers(headers)
        .proxy(proxy)
        .connection_verbose(true)
        .pool_max_idle_per_host(10)
        .build()
        .expect("build client");
    let res = http_client
        .get("https://www.google.com/search")
        .query(&[("q", "udp"), ("start", "0")])
        .send()
        .await
        .expect("http request");
    let body = res.text().await.expect("body");
    print!("{}", body);
    print!("hello");

    warp::serve(routes).run(([127, 0, 0, 1], 3030)).await;
}

// fn google_search(page: u32, keyword: String) -> SearchResponse {

// }

#[derive(Deserialize, Serialize)]
struct ResultEntry {
    name: String,
    url: String,
    desc: String,
}

#[derive(Deserialize, Serialize)]
struct SearchResponse {
    error: String,
    entries: LinkedList<ResultEntry>,
}

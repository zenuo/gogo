use html5ever::tendril::TendrilSink;
use reqwest::header;
use serde::{Deserialize, Serialize};
use std::{
    collections::{HashMap, LinkedList, VecDeque},
    time::Duration, vec, io::Read,
};
use url::Url;
use warp::Filter;

#[tokio::main]
async fn main() {
    let proxy = reqwest::Proxy::http("socks5h://127.0.0.1:1080").expect("socks proxy");
    let mut headers = header::HeaderMap::new();
    headers.insert(
        "user-agent",
        header::HeaderValue::from_static(
            "Lynx/2.8.5rel.1 libwww-FM/2.14 SSL-MM/1.4.1 GNUTLS/0.8.12",
        ),
    );
    let http_client = reqwest::ClientBuilder::new()
        .connect_timeout(Duration::from_secs(60))
        .default_headers(headers)
        .proxy(proxy)
        .danger_accept_invalid_certs(true)
        .connection_verbose(true)
        .pool_max_idle_per_host(10)
        .build()
        .expect("build client");
    let res = http_client
        .get("https://h02:5001/search") // todo
        .query(&[("q", "nginx"), ("start", "0")])
        .send()
        .await
        .expect("http request");
    let body = res.text().await.expect("body");
    // let mut file = std::fs::File::open("/Users/zenuo/Downloads/nginx - Google Search.html").expect("open file error");
    // let mut buf = vec![];
    // file.read_to_end(&mut buf).expect("read file");
    // let body = String::from_utf8_lossy(&buf).to_string();
    kuchiki(body);
}

async fn http_server() {
    let hi = warp::path("hello")
        .and(warp::path::param())
        .and(warp::header("user-agent"))
        .map(|param: String, agent: String| format!("Hello {}, whose agent is {}", param, agent));
    let hello_world = warp::path::end().map(|| "Hello, World at root!");
    let routes = warp::get().and(hello_world.or(hi));
    warp::serve(routes).run(([127, 0, 0, 1], 3030)).await;
}

//  fn google_search(page: u32, keyword: String) -> SearchResponse {

// }

fn kuchiki(body: String) {
    let mut resultEnteries: VecDeque<ResultEntry> = VecDeque::new();

    let document = kuchiki::parse_html().one(body);

    let base_url: Url = Url::parse("http://a").unwrap();

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
        if node.children().count() != 4 {
            continue;
        }
        let fc = node.first_child().unwrap();
        let first_child = fc.as_element().unwrap();
        let fc_tag = first_child.name.local.to_string();
        if !fc_tag.eq("span") {
            continue;
        }
        let hash_query: HashMap<_, _> = base_url
            .join(url)
            .unwrap()
            .query_pairs()
            .into_owned()
            .collect();
        let re = ResultEntry {
            name: fc.first_child().unwrap().text_contents(),
            url: hash_query.get("q").unwrap().to_string(),
            desc: nd
                .as_node()
                .parent()
                .unwrap()
                .parent()
                .unwrap()
                .children()
                .last()
                .unwrap()
                .text_contents().trim().to_string(),
        };
        resultEnteries.push_back(re);
    }
    print!("{}", resultEnteries.len());
}

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

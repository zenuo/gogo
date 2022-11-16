use html5ever::tendril::TendrilSink;
use once_cell::sync::Lazy;
use reqwest::Client;
use serde::{Deserialize, Serialize};
use std::{
    collections::{HashMap, VecDeque},
    time::Duration, fs::File, str::FromStr,
};
use url::Url;
use warp::Filter;
use clap::Parser;
use std::net::SocketAddr;

#[derive(Deserialize, Serialize)]
struct SearchRequest {
    q: String,
    p: u16,
}

#[derive(Deserialize, Serialize)]
struct ResultEntry {
    name: String,
    url: String,
    desc: String,
}

#[derive(Deserialize, Serialize)]
struct SearchResponse {
    error: Option<String>,
    entries: Option<VecDeque<ResultEntry>>,
}

#[derive(Serialize, Deserialize)]
struct Config {
    listen_address: String,
    google_base_url: String,
    static_path: String,
    substitution: Option<HashMap<String, String>>,
}

#[derive(Parser)]
struct Args {
    config: String,
}

static CONFIG: Lazy<Config> = Lazy::new(|| {
    let args = Args::parse();
    let config_file = File::open(args.config).expect("config file should open read only");
    let config: Config = serde_json::from_reader(config_file).expect("file should be proper JSON");
    config
});

static HTTP_CLIENT: Lazy<Client> = Lazy::new(|| {
    reqwest::ClientBuilder::new()
        .connect_timeout(Duration::from_secs(60))
        .danger_accept_invalid_certs(true)
        .connection_verbose(true)
        .pool_max_idle_per_host(10)
        .build()
        .expect("build client")
});

#[tokio::main]
async fn main() {
    let listen_address:SocketAddr = SocketAddr::from_str(&CONFIG.listen_address).expect("Invalid listen address");
    let api = warp::path("api");
    let search = api
        .and(warp::path("search"))
        .and(warp::query::<SearchRequest>())
        .and_then(render_response);
    warp::serve(search).run(listen_address).await;
}

async fn fetch(request: SearchRequest) -> Result<String, reqwest::Error> {
    let start = if request.p > 1 {
        (request.p - 1) * 10
    } else {
        0
    };
    let res = HTTP_CLIENT
        .get(format!("{}/search", CONFIG.google_base_url))
        .query(&[("q", request.q), ("start", start.to_string())])
        .header(
            "user-agent",
            "Lynx/2.8.5rel.1 libwww-FM/2.14 SSL-MM/1.4.1 GNUTLS/0.8.12",
        )
        .send()
        .await?
        .text()
        .await?;
    Ok(res)
}

async fn render_response(request: SearchRequest) -> Result<impl warp::Reply, warp::Rejection> {
    let resp = fetch(request).await;
    match resp {
        Ok(body) => {
            let result_enteries = kuchiki(body);
            let response = SearchResponse {
                error: None,
                entries: Some(result_enteries),
            };
            Ok(warp::reply::json(&response))
        }
        Err(_err) => Err(warp::reject()),
    }
}

fn kuchiki(body: String) -> VecDeque<ResultEntry> {
    let mut result_enteries: VecDeque<ResultEntry> = VecDeque::new();

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
                .text_contents()
                .trim()
                .to_string(),
        };
        result_enteries.push_back(re);
    }
    result_enteries
}

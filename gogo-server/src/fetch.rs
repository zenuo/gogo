use crate::config::{
    GogoResponse, GoogleSearchContext, ResultEntry, SearchRequest, CONFIG, HTTP_CLIENT,
    USER_AGENT_INDEX,
};
use html5ever::tendril::TendrilSink;
use log::{error, info, trace};
use once_cell::sync::{Lazy};
use reqwest::header::{HeaderMap, HeaderName, HeaderValue};
use reqwest::{ RequestBuilder};
use serde_json::Value;
use std::time::SystemTime;
use std::{
    collections::{HashMap, VecDeque},
    str::FromStr,
};
use url::Url;

pub static HEADERS: Lazy<HeaderMap<HeaderValue>> = Lazy::new(|| {
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

pub const GOOGLE_SEARCH_QUERY_KEYS: Lazy<Vec<&str>> =
    Lazy::new(|| vec!["ie", "hl", "source", "btnG", "iflsig", "gbv"]);

pub async fn fetch_search_context(user_agent: &'static str) -> Option<GoogleSearchContext> {
    let config = CONFIG.get().expect("config is not initialized");
    let http_request = HTTP_CLIENT
        .get(format!("{}", config.google_base_url))
        .header("user-agent", user_agent);
    match fetch(http_request).await {
        Ok(body) => {
            let gsc: GoogleSearchContext = parse_search_context(body);
            trace!("ua:{}, query_kv_list:{:?}", user_agent, gsc.query_kv_list);
            Some(gsc)
        }
        Err(_) => None,
    }
}

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
                            if !GOOGLE_SEARCH_QUERY_KEYS.contains(&name) {
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
    GoogleSearchContext {
        query_kv_list: query,
        created: SystemTime::now(),
    }
}

pub async fn fetch(request: RequestBuilder) -> Result<String, reqwest::Error> {
    let res = request
        .headers(HEADERS.clone())
        .send()
        .await?
        .text()
        .await?;
    Ok(res)
}

pub async fn render_response_suggest(
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

pub async fn render_response_search(
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

const FAKE_BASE_URL: Lazy<Url> = Lazy::new(|| Url::parse("http://a").unwrap());

pub fn parse_result_entry(body: String) -> VecDeque<ResultEntry> {
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

pub fn user_agent() -> &'static str {
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

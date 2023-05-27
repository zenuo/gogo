use serde_json::Value;

use crate::config::{
    CONFIG, HTTP_CLIENT,
    init_config,
};
use crate::fetch::{
    parse_result_entry,
    fetch,
    user_agent
};
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

mod config;
mod fetch;
// #[cfg(feature = "observability")]
mod metric;

use clap::Parser;
use config::SearchRequest;
use log::{error, info, trace};
use std::convert::Infallible;
use std::net::SocketAddr;
use std::{fs::File, str::FromStr};
use warp::{Filter, Rejection, Reply};

#[cfg(test)]
mod test;

#[derive(Parser)]
struct Args {
    config: String,
}

#[tokio::main]
async fn main() {
    let _ = pretty_env_logger::try_init();

    // #[cfg(not(feature = "observability"))]
    // info!("feature observability not enabled");

    let args = Args::parse();
    let config_file = File::open(args.config).expect("config file should open read only");
    config::init_config(config_file);
    let config = config::CONFIG.get().expect("config is not initialized");
    let listen_address: SocketAddr =
        SocketAddr::from_str(&config.listen_address).expect("Invalid listen address");
    let search = warp::path("search")
        .and(warp::query::<SearchRequest>())
        .and_then(fetch::render_response_search);
    let suggest = warp::path("lint")
        .and(warp::query::<SearchRequest>())
        .and_then(fetch::render_response_suggest);
    let metrics = warp::path("metrics").map(|| metric::output());
    let routes = warp::path("api")
        .and(search.or(suggest).recover(recover_api))
        .or(metrics)
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

use once_cell::sync::Lazy;
use prometheus::{Counter, Encoder, Opts, Registry, TextEncoder};

pub struct AppState {
    pub registry: Registry,
    pub suggest_success_counter: Counter,
    pub search_success_counter: Counter,
    pub suggest_error_counter: Counter,
    pub search_error_counter: Counter,
}

pub static APP_STATE: Lazy<AppState> = Lazy::new(|| {
    let suggest_success_counter =
        Counter::with_opts(Opts::new("suggest_success_counter", "test counter help")).unwrap();
    let search_success_counter =
        Counter::with_opts(Opts::new("search_success_counter", "test counter help")).unwrap();
    let suggest_error_counter =
        Counter::with_opts(Opts::new("suggest_error_counter", "test counter help")).unwrap();
    let search_error_counter =
        Counter::with_opts(Opts::new("search_error_counter", "test counter help")).unwrap();

    let r = Registry::new();
    r.register(Box::new(suggest_success_counter.clone())).unwrap();
    r.register(Box::new(search_success_counter.clone())).unwrap();
    r.register(Box::new(suggest_error_counter.clone())).unwrap();
    r.register(Box::new(search_error_counter.clone())).unwrap();

    AppState {
        registry: r,
        suggest_success_counter,
        search_success_counter,
        suggest_error_counter,
        search_error_counter,
    }
});

pub fn output() -> String {
    // Gather the metrics.
    let mut buffer = vec![];
    let encoder = TextEncoder::new();
    let metric_families = APP_STATE.registry.gather();
    encoder.encode(&metric_families, &mut buffer).unwrap();
    // Output to the standard output.
    String::from_utf8(buffer).unwrap()
}

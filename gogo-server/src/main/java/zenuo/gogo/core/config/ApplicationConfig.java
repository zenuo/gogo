package zenuo.gogo.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

@Getter
@Setter
@Slf4j
public class ApplicationConfig {

    public static ApplicationConfig INSTANCE;

    static {
        try {
            INSTANCE = new ObjectMapper(new YAMLFactory()).readValue(new File("./application.yml"), ApplicationConfig.class);
        } catch (IOException e) {
            log.error("loading config error", e);
            throw new RuntimeException(e);
        }
        INSTANCE.gogoConfig.postConstruct();
    }

    public static HttpClientConfig httpClientConfig() {
        return INSTANCE.httpClientConfig;
    }

    public static GogoConfig gogoConfig() {
        return INSTANCE.gogoConfig;
    }

    @JsonProperty("http.client")
    private HttpClientConfig httpClientConfig;

    @JsonProperty("gogo")
    private GogoConfig gogoConfig;

    @Slf4j
    @Getter
    @Setter
    public static class GogoConfig {

        private final ConcurrentHashMap<String, String> substituteRuleMap = new ConcurrentHashMap<>();
        @JsonProperty("port")
        private Integer port;

        /**
         * 替换规则列表
         */
        private List<String> substitute;

        void postConstruct() {
            if (port == null) {
                port = Constants.DEFAULT_PORT;
            }
            if (substitute != null) {
                substitute.forEach(line -> {
                    final Matcher matcher = Constants.SUBSTITUTE_RULE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        final String source = matcher.group(1).trim();
                        final String target = matcher.group(2).trim();
                        this.substituteRuleMap.put(source, target);
                    }
                });
            }
            log.info("port={}, substituteRuleMap={}", port, substituteRuleMap);
        }
    }

    @Setter
    @Getter
    public static class HttpClientConfig {
        private Integer maxTotal;
        private Integer defaultMaxPerRoute;
        private Integer connectTimeout;
        private Integer connectionRequestTimeout;
        private Integer socketTimeout;
    }
}

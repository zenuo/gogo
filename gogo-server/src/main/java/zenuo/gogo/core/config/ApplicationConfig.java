package zenuo.gogo.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * application
 *
 * @author zenuo
 * @version 2020-07-25
 */
@Getter
@Setter
@Slf4j
public class ApplicationConfig {

    private static ApplicationConfig INSTANCE;

    static {
        try {
            INSTANCE = new ObjectMapper(new YAMLFactory()).readValue(new File("./application.yml"), ApplicationConfig.class);
        } catch (IOException e) {
            log.error("loading config error", e);
            throw new RuntimeException(e);
        }
        INSTANCE.gogoConfig.postConstruct();
    }

    public static void main(String[] args) {
        System.out.println(INSTANCE);
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
}

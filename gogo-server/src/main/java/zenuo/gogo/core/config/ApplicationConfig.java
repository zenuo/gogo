package zenuo.gogo.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

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

    /**
     * 配置
     *
     * @author zenuo
     * 2018-07-08 21:14:09
     */
    @Slf4j
    @Getter
    @Setter
    public static class GogoConfig {

        /**
         * 替换规则的映射
         */
        private final ConcurrentHashMap<String, String> substituteRuleMap = new ConcurrentHashMap<>();
        /**
         * 端口
         */
        @JsonProperty("port")
        private Integer port;
        /**
         * 日间模式开始时间
         */
        private LocalTime dayModeStartTime;
        @JsonProperty("day-mode-start-time-string")
        private String dayModeStartTimeString;

        /**
         * 日间模式结束时间
         */
        private LocalTime dayModeEndTime;
        @JsonProperty("day-mode-end-time-string")
        private String dayModeEndTimeString;
        /**
         * 标语
         */
        private String slogan;

        /**
         * 替换规则列表
         */
        private List<String> substitute;

        void postConstruct() {
            if (port == null) {
                port = Constants.DEFAULT_PORT;
            }
            if (StringUtils.isEmpty(dayModeStartTimeString)) {
                dayModeStartTime = Constants.DEFAULT_DAY_MODE_START_TIME;
            } else {
                dayModeStartTime = LocalTime.parse(dayModeStartTimeString);
            }
            if (StringUtils.isEmpty(dayModeEndTimeString)) {
                dayModeEndTime = Constants.DEFAULT_DAY_MODE_END_TIME;
            } else {
                dayModeEndTime = LocalTime.parse(dayModeEndTimeString);
            }
            if (slogan == null) {
                slogan = Constants.DEFAULT_SLOGAN;
            }
            if (substitute != null) {
                //替换规则
                substitute.forEach(line -> {
                    //正则匹配
                    final Matcher matcher = Constants.SUBSTITUTE_RULE_PATTERN.matcher(line);
                    //若匹配
                    if (matcher.find()) {
                        //加入到规则集合中
                        final String source = matcher.group(1).trim();
                        final String target = matcher.group(2).trim();
                        this.substituteRuleMap.put(source, target);
                    }
                });
            }
            log.info("port={}, day-mode-start-time={}, day-mode-end-time={}, slogan={}, 替换规则={}",
                    port,
                    dayModeStartTime,
                    dayModeEndTime,
                    slogan,
                    substituteRuleMap);
        }
    }

    /**
     * HTTP客户端配置
     *
     * @author zenuo
     * @date 2019/05/15
     */
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

package zenuo.gogo.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * 常量类
 */
public final class Constants {
    /**
     * 默认标语
     */
    static final String DEFAULT_SLOGAN
            = "Powered by Google Search, please <a href=\"https://github.com/zenuo/gogo\">Star me</a>, thank you.";
    /**
     * 日间模式的开始时间
     */
    static final LocalTime DEFAULT_DAY_MODE_START_TIME = LocalTime.of(23, 0);
    /**
     * 日间模式的结束时间
     */
    static final LocalTime DEFAULT_DAY_MODE_END_TIME = LocalTime.of(11, 0);
    /**
     * 监听端口
     */
    static final int DEFAULT_PORT = 5000;
    /**
     * 替换规则的正则
     */
    static final Pattern SUBSTITUTE_RULE_PATTERN = Pattern.compile("^(.+?)#(.*?)$");

    /**
     * 请求超时，毫秒
     */
    public static final int TIME_OUT = 500;
    /**
     * Object mapper
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    /**
     * 搜索结果统计的正则表达式
     */
    public static final Pattern STATS_RESULTS_PATTERN =
            Pattern.compile("About (.+?) results \\((.+?) seconds\\)");
    /**
     * 谷歌搜索URL模板
     */
    public static final String GOOGLE_SEARCH_URL_TEMPLATE
            = "https://%s/search?safe=strict&q=%s&start=%d";
    /**
     * 谷歌搜索补全URL模板
     */
    public static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE
            = "https://%s/complete/search?client=psy-ab&q=%s";
    /**
     * 搜索响应存活时间
     */
    public static final long SEARCH_RESPONSE_CACHE_TTL_IN_SECONDS = 3600L;
    /**
     * 键「搜索响应」模式
     */
    public static final String KEY_SEARCH_RESPONSE_PATTERN = "sr_%d_%d";
    /**
     * 键「提示」模式
     */
    public static final String KEY_LINT_PATTERN = "lints_%d";
}

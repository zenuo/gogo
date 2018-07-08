package yz.gogo.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * 常量类
 */
public final class Constants {

    /**
     * 夜间模式的样式字符串
     */
    public static final String HTML_INVERT_STYLE = "background-color:black;filter:invert(100%);";
    /**
     * 请求超时，毫秒
     */
    public static final int TIME_OUT = 4000;
    /**
     * Object mapper
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * 搜索结果统计的正则表达式
     */
    public static final Pattern STATS_RESULTS_PATTERN =
            Pattern.compile("About (.+?) results \\((.+?) seconds\\)");
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
    static final Pattern SUBSTITUTE_RULE_PATTERN = Pattern.compile("^\"(.+?)\" \"(.*?)\"$");
    /**
     * URL前缀
     */
    private static final String URL_PREFIX = System.getProperty("google.search.host", "https://www.google.com");
    /**
     * 谷歌搜索URL模板
     */
    public static final String GOOGLE_SEARCH_URL_TEMPLATE = URL_PREFIX +
            "/search?safe=strict&q=%s&start=%d";
    /**
     * 谷歌搜索补全URL模板
     */
    public static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = URL_PREFIX +
            "/complete/search?client=psy-ab&q=%s";
}

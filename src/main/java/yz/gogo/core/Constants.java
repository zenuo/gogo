package yz.gogo.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 常量类
 */
public final class Constants {
    /**
     * 监听端口
     */
    public static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

    /**
     * 替换规则的映射
     */
    public static final Map<String, String> SUBSTITUTE_RULE_MAP = new HashMap<>();

    /**
     * 替换规则的正则
     */
    public static final Pattern SUBSTITUTE_RULE_PATTERN = Pattern.compile("^\"(.+?)\" \"(.*?)\"$");

    /**
     * URL前缀
     */
    private static final String URL_PREFIX = System.getProperty("google.search.host", "https://www.google.com");

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
     * 主页的HTML字符串
     */
    static String INDEX_PAGE_HTML = "<!DOCTYPE html><html lang=\"en\"><head> <meta charset=\"utf-8\"/> <title>Gogo</title> <style> body { text-align: center; } h1 { font-size: 50px; font-family: \"Times New Roman\", Times, serif; } footer { font-size: 15px; font-family: 'Roboto',arial,sans-serif;} .main { margin: 0 auto; width: 50%; padding-bottom: 50px; } </style></head><body><div class=\"main\"> <h1>Gogo</h1> <form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\"> <input name=\"q\" autocomplete=\"off\" autofocus=\"autofocus\" type=\"text\"> <button value=\"Search\" type=\"submit\">Go</button> </form></div><footer> Powered by Google Search</footer></body></html>";

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

package yz.gogo.web;

import yz.gogo.core.Config;
import yz.gogo.core.Constants;
import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * 结果页面构建器
 *
 * @author 袁臻
 * 2018-07-08 20:50:25
 */
public class ResultPageBuilder {
    private static final String HTML_BEFORE_TITLE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\" />\n" +
            "<title>Gogo - ";

    private static final String HTML_BEFORE_INVERT_STYLE = "</title>\n" +
            "<style>\n" +
            "body{width:800px;padding-left:10px;";

    private static final String HTML_AFTER_INVERT_STYLE = "}.search{padding-top:5px;padding-bottom:5px}.logo{float:left;padding-right:10px;color:#000;text-decoration:none;font-family: \"Times New Roman\", Times, serif;}.entry{padding-top:5px;padding-bottom:5px;font-family: 'Roboto',arial,sans-serif;}.name{color:#1a0dab;text-decoration:none;font-size:18px}.url{color:#006621;font-size:14px}.desc{font-size:16px}.next{padding-top:5px}\n" +
            "</style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "<div class=\"search\">\n" +
            "<a href=\"/\"><span class=\"logo\"><b>Gogo</b></span></a>\n" +
            "<form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\">\n" +
            "<input name=\"q\" autocomplete=\"off\" type=\"text\" value=\"";

    private static final String HTML_BEFORE_RESULT = "\">\n" +
            "<button type=\"submit\">Go</button>\n" +
            "</form>\n" +
            "</div>\n";

    private static final String HTML_TAIL = "</body>\n" +
            "</html>";

    private static final String HTML_ERROR = "<h2>Sorry, error occurred, please try again.</h2>";

    /**
     * 由响应示例构建页面
     *
     * @param response 响应示例
     * @return 响应页面HTML字符串
     */
    public static String build(final SearchResponse response) {
        final StringBuilder sb = new StringBuilder(HTML_BEFORE_TITLE);
        sb.append(response.getKey())
                .append(HTML_BEFORE_INVERT_STYLE);
        final LocalTime now = LocalTime.now(ZoneOffset.UTC);
        //若不是日间模式
        if (now.isBefore(Config.INSTANCE.getDayModeStartTime()) ||
                now.isAfter(Config.INSTANCE.getDayModeEndTime())) {
            sb.append(Constants.HTML_INVERT_STYLE);
        }
        sb.append(HTML_AFTER_INVERT_STYLE)
                .append(response.getKey())
                .append(HTML_BEFORE_RESULT);
        if (response.getEntries() != null) {
            response.getEntries().forEach(e -> EntryBuilder.build(sb, e));
            NextBuilder.build(sb, response.getKey(), response.getPage());
        } else {
            sb.append(HTML_ERROR);
        }
        sb.append(HTML_TAIL);
        return sb.toString();
    }
}

/**
 * 条目构建器
 */
class EntryBuilder {
    private static final String HTML_BEFORE_HREF = "<div>\n" +
            "<div class=\"entry\">\n" +
            "<a class=\"name\" href=\"";

    private static final String HTML_BEFORE_NAME = "\">";

    private static final String HTML_BEFORE_URL = "</a>\n" +
            "<br />\n" +
            "<span class=\"url\">";

    private static final String HTML_BEFORE_DESC = "</span>\n" +
            "<br />\n" +
            "<span class=\"desc\">";

    private static final String HTML_TAIL = "</span>\n" +
            "</div>\n" +
            "</div>";

    /**
     * 由条目实例构建页面
     *
     * @param target 目标StringBuilder实例
     * @param entry  条目实例
     */
    static void build(
            final StringBuilder target,
            final Entry entry
    ) {
        target.append(HTML_BEFORE_HREF)
                .append(entry.getUrl())
                .append(HTML_BEFORE_NAME)
                .append(entry.getName())
                .append(HTML_BEFORE_URL)
                .append(URLDecoder.decode(entry.getUrl(), StandardCharsets.UTF_8))
                .append(HTML_BEFORE_DESC)
                .append(entry.getDesc())
                .append(HTML_TAIL);
    }
}

/**
 * 下一页按钮构建器
 */
class NextBuilder {
    private static final String HTML_BEFORE_KEY = "<div class=\"next\">\n" +
            "<a href=\"/search?q=";

    private static final String HTML_BEFORE_PAGE = "&p=";

    private static final String HTML_TAIL = "\"><button>Next</button></a>\n" +
            "</div>";

    /**
     * 构建下一页按钮
     *
     * @param target 目标StringBuilder实例
     * @param key    关键词字符串
     * @param page   当前页数
     */
    static void build(
            final StringBuilder target,
            final String key,
            final int page) {
        target.append(HTML_BEFORE_KEY)
                .append(key)
                .append(HTML_BEFORE_PAGE)
                .append(page + 1)
                .append(HTML_TAIL);
    }
}
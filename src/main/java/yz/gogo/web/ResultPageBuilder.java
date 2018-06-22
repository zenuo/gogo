package yz.gogo.web;

import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 结果页面构建器
 */
public class ResultPageBuilder {
    private static final String HTML_BEFORE_TITLE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\" />\n" +
            "<title>Gogo - ";

    private static final String HTML_BEFORE_INPUT = "</title>\n" +
            "<style>\n" +
            "body{width:800px;padding-left:10px}.search{padding-top:5px;padding-bottom:5px}.logo{float:left;padding-right:10px;color:#000;text-decoration:none;font-family: \"Times New Roman\", Times, serif;}.entry{padding-top:5px;padding-bottom:5px;font-family: 'Roboto',arial,sans-serif;}.name{color:#1a0dab;text-decoration:none;font-size:18px}.url{color:#006621;font-size:14px}.desc{font-size:16px}.next{padding-top:5px}\n" +
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

    public static String build(final SearchResponse response) {
        final StringBuilder builder = new StringBuilder(HTML_BEFORE_TITLE);
        builder.append(response.getKey())
                .append(HTML_BEFORE_INPUT)
                .append(response.getKey())
                .append(HTML_BEFORE_RESULT);
        if (response.getEntries() != null) {
            response.getEntries().forEach(e -> EntryBuilder.build(builder, e));
            NextBuilder.build(builder, response.getKey(), response.getPage());
        } else {
            builder.append(HTML_ERROR);
        }
        builder.append(HTML_TAIL);
        return builder.toString();
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
 * "Next"按钮构建器
 */
class NextBuilder {
    private static final String HTML_BEFORE_KEY = "<div class=\"next\">\n" +
            "<a href=\"/search?q=";

    private static final String HTML_BEFORE_PAGE = "&p=";

    private static final String HTML_TAIL = "\"><button>Next</button></a>\n" +
            "</div>";

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
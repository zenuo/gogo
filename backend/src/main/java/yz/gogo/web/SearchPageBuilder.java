package yz.gogo.web;

import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;

public class SearchPageBuilder {
    private static final String HTML_BEFORE_TITLE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\" />\n" +
            "<title>Gogo - ";

    private static final String HTML_BEFORE_INPUT = "</title>\n" +
            "<style>\n" +
            "body{width:800px;padding-left:10px}.search{padding-top:5px;padding-bottom:5px}.logo{float:left;padding-left:5px;padding-right:10px;color:#000;text-decoration:none}.entry{padding-top:5px;padding-bottom:5px}.name{color:#434dce;text-decoration:none;font-size:18px}.url{color:#12968f;font-size:14px}.desc{font-size:16px}.next{padding-top:5px}\n" +
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

    public static String build(final SearchResponse response) {
        final StringBuilder builder = new StringBuilder(HTML_BEFORE_TITLE);
        builder.append(response.getKey())
                .append(HTML_BEFORE_INPUT)
                .append(response.getKey())
                .append(HTML_BEFORE_RESULT);
        for (Entry e : response.getEntries()) {
            EntryBuilder.build(builder, e);
        }
        NextBuilder.build(builder, response.getKey(), response.getPage());
        builder.append(HTML_TAIL);
        return builder.toString();
    }
}

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
                .append(entry.getUrl())
                .append(HTML_BEFORE_DESC)
                .append(entry.getDesc())
                .append(HTML_TAIL);
    }
}

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
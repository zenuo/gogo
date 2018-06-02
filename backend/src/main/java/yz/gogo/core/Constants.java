package yz.gogo.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;

/**
 * Some constants
 */
public final class Constants {
    /**
     * Google search url template
     */
    public static final String GOOGLE_SEARCH_URL_TEMPLATE = System.getProperty("google.search.host", "https://www.google.com") +
            "/search?safe=strict&q=%s&start=%d";

    /**
     * Google search complete url template
     */
    public static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = System.getProperty("google.search.host", "https://www.google.com") +
            "/complete/search?client=psy-ab&q=%s";


    /**
     * user agent
     */
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36";

    /**
     * Time out in milliseconds
     */
    public static final int TIME_OUT = 10000;

    /**
     * object mapper
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * pattern of statistics result
     */
    public static final Pattern STATS_RESULTS_PATTERN =
            Pattern.compile("About (.+?) results \\((.+?) seconds\\)");

    public static String INDEX_PAGE_HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\" />\n" +
            "<title>Gogo</title>\n" +
            "<style>\n" +
            "body{text-align:center}.main{margin:0 auto;width:50%}footer{padding-top:40px;font-size:15px}" +
            "</style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "<div class=\"main\">\n" +
            "<h1>Gogo</h1>\n" +
            "<form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\">\n" +
            "<input name=\"q\" autocomplete=\"off\" autofocus=\"autofocus\" type=\"text\">\n" +
            "<button value=\"Search\" type=\"submit\">Go</button>\n" +
            "</form>\n" +
            "</div>\n" +
            "<footer>\n" +
            "Powered by &copy;Google Search\n" +
            "</footer>\n" +
            "</body>\n" +
            "</html>";
}

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

    static String INDEX_PAGE_HTML = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\" /> <title>Gogo</title> <style> body { text-align: center; } h1 { font-size: 50px; } footer { font-size: 15px; } .main { margin: 0 auto; width: 50%; padding-bottom: 50px; } </style></head><body> <div class=\"main\"> <h1>Gogo</h1> <form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\"> <input name=\"q\" autocomplete=\"off\" autofocus=\"autofocus\" type=\"text\"> <button value=\"Search\" type=\"submit\">Go</button> </form> </div> <footer> Powered by &copy;Google Search </footer></body></html>";
}

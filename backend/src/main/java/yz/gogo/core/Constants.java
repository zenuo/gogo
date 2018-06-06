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

    static String INDEX_PAGE_HTML = "<!DOCTYPE html><html lang=\"zh-CN\"><head> <meta charset=\"utf-8\"> <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"> <title>Gogo</title> <style> .logo { font-size: 50px; font-family: \"Times New Roman\", Times, serif; } body { text-align: center; height: 100vh; } footer { font-size: 15px; } .main { margin: 0 auto; padding-bottom: 30px; padding-top: 50px } </style> <link rel=\"stylesheet\" href=\"https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\"></head><body><div class=\"main container\"> <h1 class=\"logo\">Gogo</h1> <form class=\"form-group\" action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\"> <input name=\"q\" autocomplete=\"off\" autofocus=\"autofocus\" type=\"text\"> <button class=\"btn btn-primary\" type=\"submit\">Go</button> </form></div><footer> <div> <p>Powered by Google Search<br /><a href=\"https://github.com/zenuo/gogo\">Source code</a></p> </div></footer></body></html>";
}

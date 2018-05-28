package yz.gogo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Some constants
 */
public final class Constants {
    /**
     * Google search url template
     */
    //static final String GOOGLE_SEARCH_URL_TEMPLATE = "https://www.google.com/search?safe=strict&q=%s&start=%d";
    public static final String GOOGLE_SEARCH_URL_TEMPLATE = "http://176.122.157.73:5050/search?safe=strict&q=%s&start=%d";

    /**
     * Google search complete url template
     */
    //static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = "https://www.google.com/complete/search?client=psy-ab&q=%s";
    public static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = "http://176.122.157.73:5050/complete/search?client=psy-ab&q=%s";

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
}

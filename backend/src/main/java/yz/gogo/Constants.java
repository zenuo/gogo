package yz.gogo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Some constants
 */
final class Constants {
    /**
     * Google search url template
     */
    //static final String GOOGLE_SEARCH_URL_TEMPLATE = "https://www.google.com/search?safe=strict&q=%s&start=%d";
    static final String GOOGLE_SEARCH_URL_TEMPLATE = "http://176.122.157.73:5050/search?safe=strict&q=%s&start=%d";

    /**
     * Google search complete url template
     */
    //static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = "https://www.google.com/complete/search?client=psy-ab&q=%s";
    static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = "http://176.122.157.73:5050/complete/search?client=psy-ab&q=%s";

    /**
     * user agent
     */
    static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36";

    /**
     * Time out in milliseconds
     */
    static final int TIME_OUT = 10000;

    /**
     * object mapper
     */
    static final ObjectMapper MAPPER = new ObjectMapper();
}

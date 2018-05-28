package yz.gogo.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import yz.gogo.Constants;
import yz.gogo.model.CompleteResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class CompleteUtils {
    private CompleteUtils() {
    }

    /**
     * Make the request of google search complete
     *
     * @param key keyword
     * @return document instance if succeed
     * @throws IOException io exception occurred
     */
    public static Document request(final String key) throws IOException {
        final String url = String.format(Constants.GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE,
                URLEncoder.encode(key, StandardCharsets.UTF_8));
        return Jsoup.connect(url)
                .header("User-Agent", Constants.USER_AGENT)
                .timeout(Constants.TIME_OUT)
                .ignoreContentType(true)
                .get();
    }

    /**
     * Get lints of google search complete result
     *
     * @param key keyword
     * @return lint list
     * @throws IOException io exception occurred
     */
    public static List<String> complete(final String key) throws IOException {
        final Document document = request(key);
        final JsonNode bodyNode = Constants.MAPPER.readTree(document.body().text());
        final JsonNode lintNode = bodyNode.get(1);
        if (lintNode == null) {
            return null;
        }
        //lint list
        final ArrayList<String> lints = new ArrayList<>(lintNode.size());
        lintNode.forEach(lint -> lints.add(lint.get(0).asText()));
        return lints;
    }

    /**
     * Do complete and response
     *
     * @param key keyword
     * @return response instance
     */
    public static CompleteResponse response(final String key) {
        //check arguments
        if (key.equals("")) {
            return CompleteResponse.builder().error("keyword must not empty!").build();
        }
        //builder
        final CompleteResponse.CompleteResponseBuilder builder = CompleteResponse.builder();
        builder.key(key);
        try {
            final List<String> lints = complete(key);
            builder.lints(lints);
        } catch (Exception e) {
            log.error("complete {}", key, e);
            builder.error(e.getMessage());
        }
        return builder.build();
    }
}

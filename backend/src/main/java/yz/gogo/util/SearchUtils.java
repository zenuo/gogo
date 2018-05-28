package yz.gogo.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yz.gogo.Constants;
import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public final class SearchUtils {
    /**
     * Make the request of google search
     *
     * @param key  keyword
     * @param page page number
     * @return document instance if succeed, null otherwise
     */
    public static Document request(final String key, final int page) throws IOException {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than zero!");
        }
        final int start = (page - 1) * 10;
        final String url = String.format(Constants.GOOGLE_SEARCH_URL_TEMPLATE,
                URLEncoder.encode(key, StandardCharsets.UTF_8),
                start);
        return Jsoup.connect(url)
                .header("User-Agent", Constants.USER_AGENT)
                .timeout(Constants.TIME_OUT)
                .get();
    }

    /**
     * Get entries of google search result
     *
     * @param key  keyword
     * @param page page number
     * @return entries if succeed, null otherwise
     */
    public static List<Entry> search(final String key, final int page) throws IOException {
        //document
        final Document document = request(key, page);
        final Element srg = document.getElementsByClass("srg").first();
        if (srg == null) {
            return null;
        }
        //results
        final Elements results = srg.getElementsByClass("rc");
        if (results.isEmpty()) {
            return null;
        }
        return results.stream().map(result -> {
            //builder
            final Entry.EntryBuilder builder = Entry.builder();
            //name and url
            final Element nameAndUrl = result.getElementsByClass("r")
                    .first()
                    .children()
                    .first();
            if (nameAndUrl != null) {
                builder.name(nameAndUrl.text());
                builder.url(nameAndUrl.attr("href"));
            }
            //description
            final Element desc = result.getElementsByClass("st").first();
            if (desc != null) {
                builder.desc(desc.text());
            }
            return builder.build();
        }).collect(Collectors.toList());
    }

    /**
     * Do search and response
     *
     * @param key  keyword
     * @param page page number
     * @return response instance
     */
    public static SearchResponse response(final String key, final int page) {
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        try {
            final List<Entry> entries = search(key, page);
            builder.entries(entries);
        } catch (Exception e) {
            log.error("response {}, {}", key, page, e);
            builder.error(e.getMessage());
        }
        return builder.build();
    }

    /**
     * write an object to json
     * @param object the object you want to write
     * @return json string
     */
    public static String toJson(final Object object) {
        try {
            return Constants.MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            log.error("toJson", e);
            return null;
        }
    }
}

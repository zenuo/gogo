package yz.gogo;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yz.gogo.model.Entry;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
final class Utils {
    /**
     * Make the request of google search
     *
     * @param key  keyword
     * @param page page number
     * @return document instance if succeed, null otherwise
     */
    static Document request(final String key, final int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than zero!");
        }
        final int start = (page - 1) * 10;
        final String url = String.format(Constants.GOOGLE_SEARCH_URL_TEMPLATE,
                URLEncoder.encode(key, StandardCharsets.UTF_8),
                start);
        try {
            return Jsoup.connect(url)
                    .header("User-Agent", Constants.USER_AGENT)
                    .timeout(Constants.TIME_OUT)
                    .get();
        } catch (Exception e) {
            log.error("request {}", key, e);
            return null;
        }
    }

    /**
     * Get entries of google search result
     * @param key  keyword
     * @param page page number
     * @return entries if succeed, null otherwise
     */
    static List<Entry> search(final String key, final int page) {
        //document
        final Document document = request(key, page);
        if (document == null) {
            return null;
        }
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
}

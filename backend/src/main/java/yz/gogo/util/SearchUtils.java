package yz.gogo.util;

import io.netty.handler.codec.http.HttpResponseStatus;
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
import java.util.regex.Matcher;
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
        final int start = page > 1 ? (page - 1) * 10: 0;
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
    public static SearchResponse search(final String key, final int page) {
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        //document
        final Document document;
        try {
            document = request(key, page);
        } catch (IOException e) {
            log.error("request, {}, {}", key, page);
            return builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .error(e.getMessage())
                    .build();
        }
        final Element srg = document.getElementsByClass("srg").first();
        if (srg == null) {
            return patternChanged(builder);
        }
        //results
        final Elements results = srg.getElementsByClass("rc");
        if (results.isEmpty()) {
            return patternChanged(builder);
        }
        //stats
        final Element resultStats = document.getElementById("resultStats");
        if (resultStats != null) {
            final Matcher matcher = Constants.STATS_RESULTS_PATTERN
                    .matcher(resultStats.text());
            if (matcher.find() && matcher.groupCount() == 2) {
                builder.amount(Integer.valueOf(matcher.group(1).replaceAll(",", "")));
                builder.elapsed(Float.valueOf(matcher.group(2)));
            }
        }
        builder.entries(results.stream().map(result -> {
            //builder
            final Entry.EntryBuilder entryBuilder = Entry.builder();
            //name and url
            final Element nameAndUrl = result.getElementsByClass("r")
                    .first()
                    .children()
                    .first();
            if (nameAndUrl != null) {
                entryBuilder.name(nameAndUrl.text());
                entryBuilder.url(nameAndUrl.attr("href"));
            }
            //description
            final Element desc = result.getElementsByClass("st").first();
            if (desc != null) {
                entryBuilder.desc(desc.text());
            }
            return entryBuilder.build();
        }).collect(Collectors.toList()));
        return builder.status(HttpResponseStatus.OK).build();
    }

    /**
     * Do search and response
     *
     * @param key  keyword
     * @param page page number
     * @return response instance
     */
    public static SearchResponse response(final String key, final int page) {
        //check arguments
        if (page < 0) {
            return SearchResponse.builder().error("page must be greater than zero!")
                    .status(HttpResponseStatus.BAD_REQUEST)
                    .build();
        }
        return search(key, page);
    }

    public static SearchResponse patternChanged(final SearchResponse.SearchResponseBuilder builder) {
        return builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                .error("google search page pattern changed, please contact developer")
                .build();
    }
}

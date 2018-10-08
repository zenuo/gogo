package yz.gogo.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yz.gogo.core.Config;
import yz.gogo.core.Constants;
import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Implementation of Search functions.
 */
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
        final int start = page > 1 ? (page - 1) * 10 : 0;
        final String url = String.format(Constants.GOOGLE_SEARCH_URL_TEMPLATE,
                GoogleDomainUtils.get(),
                URLEncoder.encode(key, "UTF-8"),
                start);
        log.info("get [{}]", url);
        final Document document = Jsoup.connect(url)
                .header("User-Agent", UserAgentUtils.get())
                .timeout(Constants.TIME_OUT)
                .get();
        if (Config.INSTANCE.getSubstituteRuleMap().isEmpty()) {
            return document;
        } else {
            String html = document.html();
            for (Map.Entry<String, String> rule : Config.INSTANCE.getSubstituteRuleMap().entrySet()) {
                html = html.replaceAll(rule.getKey(), rule.getValue());
            }
            return Jsoup.parse(html, url);
        }
    }

    /**
     * Get entries of google search result
     *
     * @param key  keyword
     * @param page page number
     * @return entries if succeed, null otherwise
     */
    public static SearchResponse search(final String key, final int page) {
        log.info("request, [{}], [{}]", key, page);
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        //document
        final Document document;
        try {
            document = request(key, page);
        } catch (IOException e) {
            log.error("exception", e);
            return builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .error(e.getMessage())
                    .build();
        }
        final Elements results = document.getElementsByClass("rc");
        if (results.isEmpty()) {
            return patternChanged(builder);
        }
        final List<Entry> entries = new ArrayList<>();
        //stats
        final Element resultStat = document.getElementById("resultStats");
        if (resultStat != null) {
            final Matcher matcher = Constants.STATS_RESULTS_PATTERN
                    .matcher(resultStat.text());
            if (matcher.find() && matcher.groupCount() == 2) {
                builder.amount(Long.valueOf(matcher.group(1).replaceAll(",", "")));
                builder.elapsed(Float.valueOf(matcher.group(2)));
            }
        }
        //traverse search result entries
        for (Element result : results) {
            //entry builder
            final Entry.EntryBuilder entryBuilder = Entry.builder();
            //name
            final Element name = result.getElementsByClass("LC20lb").first();
            if (name == null) {
                continue;
            }
            entryBuilder.name(name.text());
            //url
            final Element url = result.getElementsByClass("iUh30").first();
            entryBuilder.url(url.text());
            //description
            final Element desc = result.getElementsByClass("st").first();
            if (desc != null) {
                //替换"<"和">"
                entryBuilder.desc(desc.text().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
                final Entry entry = entryBuilder.build();
                //name and url are not null
                if (entry.getName() != null && entry.getUrl() != null) {
                    entries.add(entry);
                }
            }
        }
        builder.entries(entries);
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
        if (page < 1) {
            return SearchResponse.builder().error("page must be greater than zero!")
                    .status(HttpResponseStatus.BAD_REQUEST)
                    .build();
        }
        return search(key, page);
    }

    private static SearchResponse patternChanged(final SearchResponse.SearchResponseBuilder builder) {
        return builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                .error("google search page pattern changed, please contact developer")
                .build();
    }
}

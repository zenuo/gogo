package zenuo.gogo.core.processor.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.StringUtils;
import zenuo.gogo.util.UserAgentUtils;

import javax.inject.Inject;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.net.http.HttpRequest.*;

/**
 * StartPage搜索
 * <p>
 * 网址：https://duckduckgo.com
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class StartPageSearchResultProviderImpl implements ISearchResultProvider {

    private static final String URL = "https://www.startpage.com/do/search";

    private final HttpClient httpClient;

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public SearchResponse search(String key, int page) {
        return search0(key, page);
    }

    SearchResponse search0(String key, int page) {
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        //document
        final Document document;
        try {
            document = httpPost(key, page);
        } catch (Exception e) {
            log.error("http error", e);
            throw new RuntimeException(e);
        }
        //根据class获取结果列表
        final Elements results = document.getElementsByClass("search-result search-item");
        if (results.isEmpty()) {
            return patternChanged(builder);
        }
        final SearchResponse searchResponse = builder.status(HttpResponseStatus.OK).build();
        //遍历
        for (Element result: results) {
            final Entry.EntryBuilder entryBuilder = Entry.builder();
            final Element h3 = result.getElementsByClass("search-item__title").first();
            if (h3 == null) {
                continue;
            }
            final Element a = h3.child(0);
            entryBuilder.name(StringUtils.escapeHtmlEntities(a.text()));
            entryBuilder.url(a.attr("href"));
            final Element p = result.getElementsByClass("search-item__body").first();
            if (p == null) {
                searchResponse.getEntries().add(entryBuilder.build());
            } else  {
                entryBuilder.desc(StringUtils.escapeHtmlEntities(p.text()));
                searchResponse.getEntries().add(entryBuilder.build());
            }
        }

        return searchResponse;
    }

    Document httpPost(String key, int page) throws Exception {
        final int startat = page > 1 ? (page - 1) * 10 : 0;
        String body = java.util.Map.of("cat", "web", "cmd", "process_search", "language", "english",
                "query", key, "startat", String.valueOf(startat))
                .entrySet()
                .stream()
                .map(entry -> String.join("=",
                        java.net.URLEncoder.encode(entry.getKey().toString(), java.nio.charset.StandardCharsets.UTF_8),
                        java.net.URLEncoder.encode(entry.getValue().toString(),
                                java.nio.charset.StandardCharsets.UTF_8)))
                .collect(java.util.stream.Collectors.joining("&"));
        HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(java.net.URI.create(URL))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("User-Agent", UserAgentUtils.get())
                .build();
        String responseBody = httpClient.send(httpRequest, BodyHandlers.ofString()).body();
        return Jsoup.parse(responseBody);
    }

    /**
     * 模式已改变
     *
     * @param builder 搜索响应构建器
     * @return 搜索响应构建
     */
    private SearchResponse patternChanged(final SearchResponse.SearchResponseBuilder builder) {
        return builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                .error("Please contact developer")
                .build();
    }
}

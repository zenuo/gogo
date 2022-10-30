package zenuo.gogo.core.processor.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.UserAgentUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 谷歌搜索
 * <p>
 * 网址：https://google.com
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class GoogleSearchResultProviderImpl implements ISearchResultProvider {

    private final ApplicationConfig applicationConfig;

    @Override
    public int priority() {
        return 0;
    }

    public static final String GOOGLE_SEARCH_URL_TEMPLATE = "https://www.google.com/search?q=%s&start=%d";

    @Override
    public SearchResponse search(String key, int page) {
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        final Document document;
        try {
            document = httpGet(key, page);
        } catch (IOException e) {
            log.error("http error", e);
            throw new RuntimeException(e);
        }
        final List<Element> searchResultElements = document.getElementsByTag("a").stream()
                .filter(a -> a.hasAttr("href")
                        && a.attr("href").startsWith("/url?")
                        && a.childrenSize() == 2
                        && "h3".equals(a.child(0).tagName()))
                .collect(Collectors.toList());
        final SearchResponse searchResponse = builder.status(HttpResponseStatus.OK).build();
        for (Element element : searchResultElements) {
            final QueryStringDecoder decoder = new QueryStringDecoder(element.attr("href"));
            final List<String> q = decoder.parameters().get("q");
            if (q == null) {
                continue;
            }
            final Entry entry = new Entry();
            searchResponse.getEntries().add(entry);
            entry.setUrl(q.get(0));
            entry.setName(element.child(0).text());
            entry.setDesc(element.parent().parent().child(2).text());
        }
        return searchResponse;
    }

    /**
     * Make the request of google search0
     *
     * @param key  keyword
     * @param page page number
     * @return document instance if succeed, null otherwise
     */
    Document httpGet(final String key, final int page) throws IOException {
        //构造URL
        final int start = page > 1 ? (page - 1) * 10 : 0;
        final String url = String.format(GOOGLE_SEARCH_URL_TEMPLATE,
                URLEncoder.encode(key, StandardCharsets.UTF_8),
                start);
        //HTTP请求
        return Jsoup.connect(url)
                .timeout(applicationConfig.getHttpClientConfig().getConnectTimeout())
                .userAgent(UserAgentUtils.get()).get();
    }
}

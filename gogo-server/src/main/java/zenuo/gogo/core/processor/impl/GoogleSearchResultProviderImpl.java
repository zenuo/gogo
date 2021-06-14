package zenuo.gogo.core.processor.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import zenuo.gogo.core.processor.IHttpClientProvider;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.UserAgentUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    private final IHttpClientProvider httpClientProvider;

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
            throw new RuntimeException(e);
        }
        final List<Element> searchResultElements = document.getElementsByTag("a").stream()
                .filter(a -> a.hasAttr("href")
                        && a.attr("href").startsWith("/url?")
                        && a.childrenSize() == 2
                        && "h3".equals(a.child(0).tagName()))
                .collect(Collectors.toList());
        final List<Entry> entries = new ArrayList<>(searchResultElements.size());
        builder.entries(entries);
        for (Element element : searchResultElements) {
            final QueryStringDecoder decoder = new QueryStringDecoder(element.attr("href"));
            final List<String> q = decoder.parameters().get("q");
            if (q == null) {
                continue;
            }
            final Entry entry = new Entry();
            entries.add(entry);
            entry.setUrl(q.get(0));
            entry.setName(element.child(0).text());
            entry.setDesc(element.parent().parent().child(2).text());
        }
        return builder.status(HttpResponseStatus.OK).build();
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
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept-Language", "en");
        httpGet.setHeader("User-Agent", UserAgentUtils.get());
        //HTTP请求
        return Jsoup.parse(httpClientProvider.execute(httpGet));
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

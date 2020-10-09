package zenuo.gogo.core.processor.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import zenuo.gogo.core.processor.IHttpClientProvider;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.exception.SearchException;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.StringUtils;
import zenuo.gogo.util.UserAgentUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * StartPage搜索
 * <p>
 * 网址：https://duckduckgo.com
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Slf4j
public final class StartPageSearchResultProviderImpl implements ISearchResultProvider {

    private static final String URL = "https://www.startpage.com/do/search";

    private static final List<BasicNameValuePair> BASIC_NAME_VALUE_PAIRS = List.of(
            new BasicNameValuePair("cat", "web"),
            new BasicNameValuePair("cmd", "process_search"),
            new BasicNameValuePair("language", "english"));

    private final IHttpClientProvider httpClientProvider = ServiceLoader.load(IHttpClientProvider.class).iterator().next();

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public SearchResponse search(String key, int page) throws SearchException {
        return search0(key, page);
    }

    SearchResponse search0(String key, int page) throws SearchException {
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        //document
        final Document document;
        try {
            document = httpPost(key, page);
        } catch (IOException e) {
            final String message = "exception occurred during request StartPage search";
            log.error(message, e);
            throw new SearchException(message, e);
        }
        //根据class获取结果列表
        final Elements results = document.getElementsByClass("search-result search-item");
        if (results.isEmpty()) {
            return patternChanged(builder);
        }
        final List<Entry> entries = new ArrayList<>(10);
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
                entries.add(entryBuilder.build());
                continue;
            }
            entryBuilder.desc(StringUtils.escapeHtmlEntities(p.text()));
            entries.add(entryBuilder.build());
        }
        builder.entries(Optional.of(entries));
        return builder.status(HttpResponseStatus.OK).build();
    }

    Document httpPost(String key, int page) throws IOException {
        final HttpPost httpPost = new HttpPost(URL);
        httpPost.setHeader("User-Agent", UserAgentUtils.get());
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        //表单
        final List<BasicNameValuePair> parameters = new ArrayList<>(5);
        parameters.addAll(BASIC_NAME_VALUE_PAIRS);
        parameters.add(new BasicNameValuePair("query", key));
        final int startat = page > 1 ? (page - 1) * 10 : 0;
        parameters.add(new BasicNameValuePair("startat", String.valueOf(startat)));
        httpPost.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
        //HTTP请求
        return Jsoup.parse(httpClientProvider.execute(httpPost));
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
                .entries(Optional.empty())
                .build();
    }
}

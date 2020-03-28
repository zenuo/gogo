package zenuo.gogo.core.processor.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zenuo.gogo.core.config.Constants;
import zenuo.gogo.core.processor.IHttpClientProvider;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.exception.SearchException;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.GoogleDomainUtils;
import zenuo.gogo.util.StringUtils;
import zenuo.gogo.util.UserAgentUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 谷歌搜索
 * <p>
 * 网址：https://google.com
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
final class GoogleSearchResultProviderImpl implements ISearchResultProvider {

    @NonNull
    private final IHttpClientProvider httpClientProvider;

    @Override
    public SearchResponse search(String key, int page) throws SearchException {
        return search0(key, page);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Get entries of google search0 result
     *
     * @param key  keyword
     * @param page page number
     * @return entries if succeed, null otherwise
     */
    SearchResponse search0(String key, int page) throws SearchException {
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        //document
        final Document document;
        try {
            document = httpGet(key, page);
        } catch (IOException e) {
            final String message = "exception occurred during request google search";
            log.error(message, e);
            throw new SearchException(message, e);
        }
        final Elements webResults = document.getElementsByClass("rc");
        if (webResults.isEmpty()) {
            return patternChanged(builder);
        }
        final List<Entry> entries = new ArrayList<>();
        //traverse search result entries
        for (Element result : webResults) {
            //entry builder
            final Entry.EntryBuilder entryBuilder = Entry.builder();
            //name
            final Element name = result.getElementsByClass("LC20lb").first();
            if (name == null) {
                continue;
            }
            entryBuilder.name(StringUtils.escapeHtmlEntities(name.text()));
            //url
            final Element url = name.parent();
            entryBuilder.url(url.attr("href"));
            //description
            final Element desc = result.getElementsByClass("st").first();
            if (desc != null) {
                entryBuilder.desc(StringUtils.escapeHtmlEntities(desc.text()));
                final Entry entry = entryBuilder.build();
                //name and url are not null
                if (entry.getName() != null && entry.getUrl() != null) {
                    entries.add(entry);
                }
            }
        }
        final Elements videoResults = document.getElementsByClass("y8AWGd llvJ5e");
        for (Element videoResult : videoResults) {
            final Element a = videoResult.child(0);
            entries.add(Entry.builder()
                    .url(a.attr("href"))
                    .name(a.child(1).text())
                    .desc(videoResult.child(2).text())
                    .build());
        }
        builder.entries(entries);
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
        final String url = String.format(Constants.GOOGLE_SEARCH_URL_TEMPLATE,
                GoogleDomainUtils.get(),
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

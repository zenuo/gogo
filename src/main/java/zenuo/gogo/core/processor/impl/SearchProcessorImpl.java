package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import zenuo.gogo.core.Config;
import zenuo.gogo.core.Constants;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IProcessor;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.GoogleDomainUtils;
import zenuo.gogo.util.JsonUtils;
import zenuo.gogo.util.UserAgentUtils;
import zenuo.gogo.web.IPageBuilder;
import zenuo.gogo.web.ResultPageBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Slf4j
public final class SearchProcessorImpl implements IProcessor {

    /**
     * 页面构建器
     */
    private static final IPageBuilder PAGE_BUILDER = new ResultPageBuilder();

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder decoder, ResponseType responseType) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || "".equals(keys.get(0))) {
            response(ctx,
                    request,
                    ResponseType.API,
                    "{\"error\": \"the keyword should not be empty\"}",
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final List<String> pages = decoder.parameters().get("p");
            final SearchResponse response = response(
                    keys.get(0),
                    pages == null || "".equals(pages.get(0)) ? 1 : Integer.parseInt(pages.get(0)));
            if (responseType == ResponseType.API) {
                response(ctx,
                        request,
                        ResponseType.API,
                        JsonUtils.toJson(response),
                        response.getStatus());
            } else {
                response(ctx,
                        request,
                        ResponseType.PAGE,
                        PAGE_BUILDER.build(response),
                        response.getStatus());
            }
        }
    }

    /**
     * Make the request of google search
     *
     * @param key  keyword
     * @param page page number
     * @return document instance if succeed, null otherwise
     */
    Document request(final String key, final int page) throws IOException {
        final int start = page > 1 ? (page - 1) * 10 : 0;
        final String url = String.format(Constants.GOOGLE_SEARCH_URL_TEMPLATE,
                GoogleDomainUtils.get(),
                URLEncoder.encode(key, StandardCharsets.UTF_8),
                start);
        final Document document = Jsoup.connect(url)
                .header("Accept-Language", "en")
                .userAgent(UserAgentUtils.get())
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
    public SearchResponse search(final String key, final int page) {
        //builder
        final SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        builder.key(key);
        builder.page(page);
        //document
        final Document document;
        try {
            document = request(key, page);
        } catch (IOException e) {
            log.error("exception occurred during request google search", e);
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
            if (matcher.find() && matcher.groupCount() == Constants.STATS_PATTERN_GROUP_COUNT) {
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
            final Element url = name.parent();
            entryBuilder.url(url.attr("href"));
            //description
            final Element desc = result.getElementsByClass("st").first();
            if (desc != null) {
                entryBuilder.desc(desc.text()
                        //sterilize "<" and ">"
                        .replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;"));
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
    SearchResponse response(final String key, final int page) {
        //check arguments
        if (page < 1) {
            return SearchResponse.builder().error("page must be greater than zero!")
                    .status(HttpResponseStatus.BAD_REQUEST)
                    .build();
        }
        return search(key, page);
    }

    private SearchResponse patternChanged(final SearchResponse.SearchResponseBuilder builder) {
        return builder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                .error("google search page pattern changed, please contact developer")
                .build();
    }
}

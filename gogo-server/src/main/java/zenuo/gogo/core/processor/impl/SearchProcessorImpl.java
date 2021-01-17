package zenuo.gogo.core.processor.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.ISearchProcessor;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.service.ICacheService;
import zenuo.gogo.util.JsonUtils;
import zenuo.gogo.web.IPageBuilder;
import zenuo.gogo.web.IResultPageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@Slf4j
public final class SearchProcessorImpl implements ISearchProcessor {

    private final IPageBuilder resultPageBuilder = ServiceLoader.load(IResultPageBuilder.class).iterator().next();

    private final ICacheService cacheService = ServiceLoader.load(ICacheService.class).iterator().next();

    private final List<ISearchResultProvider> searchResultProviders = ServiceLoader.load(ISearchResultProvider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .sorted(Comparator.comparingInt(ISearchResultProvider::priority))
            .collect(Collectors.toList());

    private static final byte[] RESPONSE_BODY_KEYWORD_EMPTY = "{\"error\": \"the keyword should not be empty\"}".getBytes(StandardCharsets.UTF_8);

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder decoder, ResponseType responseType) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || "".equals(keys.get(0))) {

            response(ctx,
                    request,
                    ResponseType.API,
                    RESPONSE_BODY_KEYWORD_EMPTY,
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final List<String> pages = decoder.parameters().get("p");
            final String key = keys.get(0);
            final int page = pages == null || "".equals(pages.get(0)) ? 1 : Integer.parseInt(pages.get(0));

            SearchResponse response = null;

            if (page < 1) {
                response = SearchResponse.builder().error("page must be greater than zero!")
                        .status(HttpResponseStatus.BAD_REQUEST)
                        .build();
            } else {
                // try to read cache
                final Optional<SearchResponse> cache = readCache(key, page);
                if (cache.isPresent()) {
                    // if hit
                    response = cache.get();
                } else {
                    // if not hit
                    for (ISearchResultProvider srp : searchResultProviders) {
                        try {
                            response = srp.search(key, page);
                            if (response.getEntries().isPresent()) {
                                // if response entries is not empty
                                break;
                            }
                        } catch (Exception e) {
                            log.error("exception {}", srp, e);
                        }
                    }
                    // cache
                    if (response != null && response.getEntries().isPresent()) {
                        writeCache(key, page, response);
                    }
                }
            }

            if (response == null) {
                response(ctx,
                        request,
                        responseType,
                        responseType == ResponseType.API ? ("{\"error\": \"try again later\"}").getBytes(StandardCharsets.UTF_8)
                                : resultPageBuilder.build(SearchResponse.builder().key(key).error("try again later").build()),
                        HttpResponseStatus.OK);

            } else {
                response(ctx,
                        request,
                        responseType,
                        responseType == ResponseType.API ? JsonUtils.toJsonBytes(response) : resultPageBuilder.build(response),
                        response.getStatus() == null ? HttpResponseStatus.OK : response.getStatus());
            }
        }
    }

    private Optional<SearchResponse> readCache(String key, int page) {
        return searchResultProviders.get(0).readCache(cacheService, key, page);
    }

    private void writeCache(String key, int page, SearchResponse searchResponse) {
        searchResultProviders.get(0).writeCache(cacheService, key, page, searchResponse);
    }
}

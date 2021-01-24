package zenuo.gogo.core.processor.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.config.Constants;
import zenuo.gogo.core.processor.ILintProcessor;
import zenuo.gogo.service.ICacheService;
import zenuo.gogo.util.JsonUtils;
import zenuo.gogo.util.UserAgentUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class LintProcessorImpl implements ILintProcessor {

    private static final byte[] RESPONSE_BODY_KEYWORD_EMPTY = "{\"error\": \"the keyword should not be empty\"}".getBytes(StandardCharsets.UTF_8);

    private final ICacheService cacheService;

    public static final String GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE = "https://www.google.com/complete/search?client=psy-ab&q=%s";

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
            final String key = keys.get(0);
            final String cacheKey = String.format(Constants.KEY_LINT_PATTERN, key.hashCode());
            final Optional<byte[]> cache = cacheService.get(cacheKey);
            if (cache.isPresent()) {
                response(ctx,
                        request,
                        ResponseType.API,
                        cache.get(),
                        HttpResponseStatus.OK);
                return;
            }
            final LintResponse response = response(key);
            final byte[] body = JsonUtils.toJsonBytes(response);
            cacheService.set(cacheKey, body);
            response(ctx,
                    request,
                    ResponseType.API,
                    body,
                    response.getStatus());
        }
    }

    /**
     * Make the request of google search0 lint
     *
     * @param key keyword
     * @return document instance if succeed
     * @throws IOException io exception occurred
     */
    Document request(final String key) {
        final String url = String.format(GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE,
                URLEncoder.encode(key, StandardCharsets.UTF_8));
        try {
            return Jsoup.connect(url)
                    .header("User-Agent", UserAgentUtils.get())
                    .timeout(Constants.TIME_OUT)
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get lints of google search0 lint result
     *
     * @param key keyword
     * @return lint list
     * @throws IOException io exception occurred
     */
    @Override
    public List<String> lint(final String key) {
        final Document document = request(key);
        final JsonNode bodyNode;
        try {
            bodyNode = Constants.MAPPER.readTree(document.body().text());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        final JsonNode lintNode = bodyNode.get(1);
        if (lintNode == null) {
            return null;
        }
        //lint list
        final ArrayList<String> lints = new ArrayList<>(lintNode.size());
        lintNode.forEach(lint -> lints.add(lint.get(0).asText()));
        return lints;
    }

    /**
     * Do lint and response
     *
     * @param key keyword
     * @return response instance
     */
    LintResponse response(final String key) {
        final LintResponse.LintResponseBuilder builder = LintResponse.builder();
        builder.key(key);
        try {
            final List<String> lints = lint(key);
            builder.lints(lints).status(HttpResponseStatus.OK);
        } catch (Exception e) {
            log.error("lint {}", key, e);
            builder.error(e.getMessage())
                    .status(HttpResponseStatus.GATEWAY_TIMEOUT);
        }
        return builder.build();
    }
}

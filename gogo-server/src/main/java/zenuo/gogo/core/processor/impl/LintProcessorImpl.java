package zenuo.gogo.core.processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.config.Constants;
import zenuo.gogo.core.processor.IHttpClientProvider;
import zenuo.gogo.core.processor.IProcessor;
import zenuo.gogo.model.LintResponse;
import zenuo.gogo.util.GoogleDomainUtils;
import zenuo.gogo.util.JsonUtils;
import zenuo.gogo.util.UserAgentUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("lintProcessor")
public final class LintProcessorImpl implements IProcessor {

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
            final LintResponse response = response(keys.get(0));
            response(ctx,
                    request,
                    ResponseType.API,
                    JsonUtils.toJson(response),
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
    Document request(final String key) throws IOException {
        final String url = String.format(Constants.GOOGLE_SEARCH_COMPLETE_URL_TEMPLATE,
                GoogleDomainUtils.get(),
                URLEncoder.encode(key, StandardCharsets.UTF_8));
        return Jsoup.connect(url)
                .header("User-Agent", UserAgentUtils.get())
                .timeout(Constants.TIME_OUT)
                .ignoreContentType(true)
                .get();
    }

    /**
     * Get lints of google search0 lint result
     *
     * @param key keyword
     * @return lint list
     * @throws IOException io exception occurred
     */
    List<String> lint(final String key) throws IOException {
        final Document document = request(key);
        final JsonNode bodyNode = Constants.MAPPER.readTree(document.body().text());
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
        //builder
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

package yz.gogo.web;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import yz.gogo.Constants;
import yz.gogo.model.CompleteResponse;
import yz.gogo.model.SearchResponse;
import yz.gogo.util.CompleteUtils;
import yz.gogo.util.JsonUtils;
import yz.gogo.util.SearchUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (request.method() != HttpMethod.GET) {
            response(ctx,
                    request,
                    JsonUtils.toJson(Map.of("error", "the http method should be GET only")),
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            log.info("Request {}, keep alive {}", request.uri(), HttpUtil.isKeepAlive(request));
            switch (decoder.path()) {
                case "/api/search":
                    search(ctx, request, decoder);
                    break;
                case "/api/complete":
                    complete(ctx, request, decoder);
                    break;
                default:
                    response(ctx,
                            request,
                            JsonUtils.toJson(Map.of("error", "the path should be '/api/search' or '/api/complete'")),
                            HttpResponseStatus.BAD_GATEWAY);
            }
        }
    }

    /**
     * handle search
     */
    private void search(final ChannelHandlerContext ctx,
                        final FullHttpRequest request,
                        final QueryStringDecoder decoder) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || keys.get(0).equals("")) {
            response(ctx,
                    request,
                    JsonUtils.toJson(Map.of("error", "the keyword should not be empty")),
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final List<String> pages = decoder.parameters().get("p");
            final SearchResponse response = SearchUtils.response(
                    keys.get(0),
                    pages == null || pages.get(0).equals("") ? 0 : Integer.parseInt(pages.get(0)));
            response(ctx,
                    request,
                    JsonUtils.toJson(response),
                    response.getStatus());
        }
    }

    /**
     * handle search
     */
    private void complete(final ChannelHandlerContext ctx,
                          final FullHttpRequest request,
                          final QueryStringDecoder decoder) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || keys.get(0).equals("")) {
            response(ctx,
                    request,
                    JsonUtils.toJson(Map.of("error", "the keyword should not be empty")),
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final CompleteResponse response = CompleteUtils.response(keys.get(0));
            response(ctx,
                    request,
                    JsonUtils.toJson(response),
                    response.getStatus());
        }
    }

    /**
     * Responses client
     */
    private void response(final ChannelHandlerContext ctx,
                          final FullHttpRequest request,
                          final String body,
                          final HttpResponseStatus status) {
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                status,
                body == null ? Unpooled.buffer() : Unpooled.copiedBuffer(body.getBytes(StandardCharsets.UTF_8)));
        response.headers().add("Content-Type", "application/json; charset=utf-8");
        response.headers().add("Server", "gogo/0.1");
        response.headers().add("Access-Control-Allow-Origin", "*");
        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            response.headers().add("Connection", "Keep-Alive");
            response.headers().add("Keep-Alive", "timeout=5, max=1000");
        }
        ctx.writeAndFlush(response);
        log.info("response");
        if (!keepAlive) {
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent)evt).state() == IdleState.ALL_IDLE) {
                ctx.close();
            }
        }
    }
}

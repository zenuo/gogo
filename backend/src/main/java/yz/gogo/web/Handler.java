package yz.gogo.web;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
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
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (msg.method() != HttpMethod.GET) {
            response(ctx,
                    msg.protocolVersion(),
                    JsonUtils.toJson(Map.of("error", "the http method should be GET only")),
                    HttpResponseStatus.BAD_REQUEST,
                    HttpUtil.isKeepAlive(msg));
        } else {
            final QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
            log.info("Request {}", msg.uri());
            switch (decoder.path()) {
                case "/api/search":
                    search(ctx, msg.protocolVersion(), decoder, HttpUtil.isKeepAlive(msg));
                    break;
                case "/api/complete":
                    complete(ctx, msg.protocolVersion(), decoder, HttpUtil.isKeepAlive(msg));
                    break;
                default:
                    response(ctx,
                            msg.protocolVersion(),
                            JsonUtils.toJson(Map.of("error", "the path should be '/api/search' or '/api/complete'")),
                            HttpResponseStatus.BAD_GATEWAY,
                            HttpUtil.isKeepAlive(msg));
            }
        }
    }

    /**
     * handle search
     */
    private void search(final ChannelHandlerContext ctx,
                        final HttpVersion version,
                        final QueryStringDecoder decoder,
                        final boolean close) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || keys.get(0).equals("")) {
            response(ctx,
                    version,
                    JsonUtils.toJson(Map.of("error", "the keyword should not be empty")),
                    HttpResponseStatus.BAD_REQUEST,
                    close);
        } else {
            final List<String> pages = decoder.parameters().get("p");
            final SearchResponse response = SearchUtils.response(
                    keys.get(0),
                    pages == null || pages.get(0).equals("") ? 0 : Integer.parseInt(pages.get(0)));
            response(ctx,
                    version,
                    JsonUtils.toJson(response),
                    response.getStatus(),
                    close);
        }
    }

    /**
     * handle search
     */
    private void complete(final ChannelHandlerContext ctx,
                          final HttpVersion version,
                          final QueryStringDecoder decoder,
                          final boolean close) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || keys.get(0).equals("")) {
            response(ctx,
                    version,
                    JsonUtils.toJson(Map.of("error", "the keyword should not be empty")),
                    HttpResponseStatus.BAD_REQUEST,
                    close);
        } else {
            final CompleteResponse response = CompleteUtils.response(keys.get(0));
            response(ctx,
                    version,
                    JsonUtils.toJson(response),
                    response.getStatus(),
                    close);
        }
    }

    /**
     * Responses client
     */
    private void response(final ChannelHandlerContext ctx,
                          final HttpVersion version,
                          final String body,
                          final HttpResponseStatus status,
                          final boolean close) {
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                version,
                status,
                body == null ? Unpooled.buffer() : Unpooled.copiedBuffer(body.getBytes(StandardCharsets.UTF_8)));
        response.headers().add("Content-Type", "application/json; charset=UTF-8");
        ctx.writeAndFlush(response);
        if (close) {
            ctx.close();
        }
    }
}

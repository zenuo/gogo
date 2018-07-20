package yz.gogo.core;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import yz.gogo.model.CompleteResponse;
import yz.gogo.model.SearchResponse;
import yz.gogo.util.CompleteUtils;
import yz.gogo.util.JsonUtils;
import yz.gogo.util.SearchUtils;
import yz.gogo.web.IndexPageBuilder;
import yz.gogo.web.ResultPageBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 处理器类，通道读取事件的回调
 */
@Slf4j
@ChannelHandler.Sharable
public class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (request.method() != HttpMethod.GET) {
            response(ctx,
                    request,
                    ResponseType.API,
                    "{\"error\", \"the http method should be GET only\"}",
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            log.debug("Request [{}], keep alive [{}]",
                    URLDecoder.decode(request.uri(), StandardCharsets.UTF_8),
                    HttpUtil.isKeepAlive(request));
            switch (decoder.path()) {
                case "/":
                    index(ctx, request, ResponseType.PAGE);
                    break;
                case "/search":
                    search(ctx, request, decoder, ResponseType.PAGE);
                    break;
                case "/api":
                    index(ctx, request, ResponseType.API);
                    break;
                case "/api/search":
                    search(ctx, request, decoder, ResponseType.API);
                    break;
                case "/api/complete":
                    complete(ctx, request, decoder);
                    break;
                default:
                    response(ctx,
                            request,
                            ResponseType.API,
                            JsonUtils.toJson(Map.of("error", "BAD_GATEWAY")),
                            HttpResponseStatus.BAD_GATEWAY);
            }
        }
    }

    /**
     * Response index by API or page
     */
    private void index(
            final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final ResponseType type
    ) {
        if (type == ResponseType.API) {
            response(ctx,
                    request,
                    ResponseType.API,
                    "{\"info\":\"Hello, I am Gogo API, https://github.com/zenuo/gogo\"}",
                    HttpResponseStatus.OK);
        } else {
            response(ctx,
                    request,
                    ResponseType.PAGE,
                    IndexPageBuilder.build(),
                    HttpResponseStatus.OK
            );
        }
    }

    /**
     * Responses search by API or Page
     */
    private void search(
            final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final QueryStringDecoder decoder,
            final ResponseType type
    ) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || "".equals(keys.get(0))) {
            response(ctx,
                    request,
                    ResponseType.API,
                    JsonUtils.toJson(Map.of("error", "the keyword should not be empty")),
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final List<String> pages = decoder.parameters().get("p");
            final SearchResponse response = SearchUtils.response(
                    keys.get(0),
                    pages == null || "".equals(pages.get(0)) ? 1 : Integer.parseInt(pages.get(0)));
            if (type == ResponseType.API) {
                response(ctx,
                        request,
                        ResponseType.API,
                        JsonUtils.toJson(response),
                        response.getStatus());
            } else {
                response(ctx,
                        request,
                        ResponseType.PAGE,
                        ResultPageBuilder.build(response),
                        response.getStatus());
            }
        }
    }

    /**
     * Responses complete by API
     */
    private void complete(
            final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final QueryStringDecoder decoder
    ) {
        final List<String> keys = decoder.parameters().get("q");
        if (keys == null || "".equals(keys.get(0))) {
            response(ctx,
                    request,
                    ResponseType.API,
                    JsonUtils.toJson(Map.of("error", "the keyword should not be empty")),
                    HttpResponseStatus.BAD_REQUEST);
        } else {
            final CompleteResponse response = CompleteUtils.response(keys.get(0));
            response(ctx,
                    request,
                    ResponseType.API,
                    JsonUtils.toJson(response),
                    response.getStatus());
        }
    }

    /**
     * Responses
     */
    private void response(
            final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final ResponseType type,
            final String body,
            final HttpResponseStatus status
    ) {
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                status,
                body == null ? Unpooled.buffer() : Unpooled.copiedBuffer(body.getBytes(StandardCharsets.UTF_8)));
        //设置头信息
        response.headers().add("Server", "gogo/0.1");
        if (type == ResponseType.API) {
            //若是API请求
            response.headers().add("Content-Type", "application/json; charset=utf-8");
            response.headers().add("Access-Control-Allow-Origin", "*");
        } else {
            //若是网页请求
            response.headers().add("Content-Type", "text/html; charset=utf-8");
        }
        ctx.writeAndFlush(response);
        log.info("response");
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                ctx.close();
            }
        }
    }
}

package zenuo.gogo.core.processor;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import zenuo.gogo.core.ResponseType;

import java.nio.charset.StandardCharsets;

public interface IGogoProcessor {

    /**
     * 处理
     *
     * @param ctx          上下文
     * @param request      HTTP请求
     * @param decoder      解码器
     * @param responseType 响应类型
     */
    void process(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder decoder, ResponseType responseType);

    /**
     * 响应
     *
     * @param ctx          上下文
     * @param request      HTTP请求实例
     * @param responseType 响应类型
     * @param body         响应体
     * @param status       HTTP响应状态
     */
    default void response(
            final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final ResponseType responseType,
            final String body,
            final HttpResponseStatus status
    ) {
        //响应对象
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                status,
                body == null ? Unpooled.buffer() : Unpooled.copiedBuffer(body.getBytes(StandardCharsets.UTF_8)));
        //设置头信息
        response.headers().add(HttpHeaderNames.SERVER, "gogo");
        if (responseType == ResponseType.API) {
            //若是API请求
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
            response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET");
        } else {
            //若是网页请求
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        }
        //响应后关闭通道
        ctx.writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);
    }
}

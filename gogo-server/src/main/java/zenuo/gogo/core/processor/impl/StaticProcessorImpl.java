package zenuo.gogo.core.processor.impl;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import zenuo.gogo.core.ResponseType;
import zenuo.gogo.core.processor.IStaticProcessor;

import java.io.InputStream;

/**
 * note
 *
 * @author zenuo
 * @version 2020-07-26
 */
@Slf4j
public final class StaticProcessorImpl implements IStaticProcessor {
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request, QueryStringDecoder decoder, ResponseType responseType) {
        final String path = "web" + decoder.path();
        final byte[] bytes;
        try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            bytes = IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            log.error("process static '{}' error", path, e);
            throw new RuntimeException(e);
        }
        //响应对象
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(bytes));
        //设置头信息
        response.headers().add(HttpHeaderNames.SERVER, "gogo");
        response.headers().add(HttpHeaderNames.CACHE_CONTROL, "private, max-age=120");
        if (path.endsWith(".js")) {
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/javascript");
        } else {
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.FILE);
        }
        //响应后关闭通道
        ctx.writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);
    }
}

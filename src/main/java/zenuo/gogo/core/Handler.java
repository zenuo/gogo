package zenuo.gogo.core;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import zenuo.gogo.core.processor.IGogoProcessor;
import zenuo.gogo.core.processor.impl.CompleteProcessorImpl;
import zenuo.gogo.core.processor.impl.IndexProcessorImpl;
import zenuo.gogo.core.processor.impl.SearchProcessorImpl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 处理器类，通道读取事件的回调
 */
@Slf4j
@ChannelHandler.Sharable
public final class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 首页处理器
     */
    private final IGogoProcessor indexProcessor = new IndexProcessorImpl();

    /**
     * 搜索处理器
     */
    private final IGogoProcessor searchProcessor = new SearchProcessorImpl();

    /**
     * 补全处理器
     */
    private final IGogoProcessor completeProcessor = new CompleteProcessorImpl();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {

        if (request.method() != HttpMethod.GET) {
            indexProcessor.response(ctx,
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
                    indexProcessor.process(ctx, request, decoder, ResponseType.PAGE);
                    break;
                case "/search":
                    searchProcessor.process(ctx, request, decoder, ResponseType.PAGE);
                    break;
                case "/api":
                    indexProcessor.process(ctx, request, decoder, ResponseType.API);
                    break;
                case "/api/search":
                    searchProcessor.process(ctx, request, decoder, ResponseType.API);
                    break;
                case "/api/complete":
                    completeProcessor.process(ctx, request, decoder, null);
                    break;
                default:
                    indexProcessor.response(ctx,
                            request,
                            ResponseType.API,
                            "{\"error\": \"BAD_GATEWAY\"}",
                            HttpResponseStatus.BAD_GATEWAY);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        //若时间为空闲状态事件
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                //关闭
                ctx.close();
            }
        }
    }
}

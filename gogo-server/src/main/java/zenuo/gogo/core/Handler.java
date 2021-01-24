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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import zenuo.gogo.core.processor.IIndexProcessor;
import zenuo.gogo.core.processor.ILintProcessor;
import zenuo.gogo.core.processor.ISearchProcessor;
import zenuo.gogo.core.processor.IStaticProcessor;

import javax.inject.Inject;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 首页处理器
     */
    private final IIndexProcessor indexProcessor;

    /**
     * 搜索处理器
     */
    private final ISearchProcessor searchProcessor;

    /**
     * 补全处理器
     */
    private final ILintProcessor lintProcessor;

    /**
     * 静态文件处理器
     */
    private final IStaticProcessor staticProcessor;

    /**
     * 工作队列
     */
    private final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(256);

    /**
     * 处理工作者线程池
     */
    private final ThreadPoolExecutor processWorkers = new ThreadPoolExecutor(2, 8,
            30, TimeUnit.SECONDS,
            workQueue,
            new GogoThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (workQueue.remainingCapacity() <= 0) {
            log.warn("too many requests, client: {}, request: {}", ctx.channel(), request);
            indexProcessor.response(ctx,
                    request,
                    ResponseType.API,
                    "{\"error\": \"too many requests, please try again later.\"}".getBytes(StandardCharsets.UTF_8),
                    HttpResponseStatus.TOO_MANY_REQUESTS);
        } else {
            // 此处线程是事件循环worker，由processWorkers完成逻辑、搜索请求解析，再由processWorkers触发worker线程响应客户端
            processWorkers.execute(() -> doChannelRead0(ctx, request));
        }
    }

    private void doChannelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        //若方法不是GET
        if (request.method() != HttpMethod.GET) {
            //响应错误
            indexProcessor.response(ctx,
                    request,
                    ResponseType.API,
                    "{\"error\", \"the http method should be GET only\"}".getBytes(StandardCharsets.UTF_8),
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
                case "/api/lint":
                    lintProcessor.process(ctx, request, decoder, null);
                    break;
                default:
                    if (decoder.path().startsWith("/static")) {
                        staticProcessor.process(ctx, request, decoder, null);
                    } else {
                        indexProcessor.response(ctx,
                                request,
                                ResponseType.API,
                                "{\"error\": \"BAD_GATEWAY\"}".getBytes(StandardCharsets.UTF_8),
                                HttpResponseStatus.BAD_GATEWAY);
                    }
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                ctx.close();
            }
        }
    }

    private static class GogoThreadFactory implements ThreadFactory {
        private final static String PREFIX = "gogo-worker-";
        private final AtomicInteger nextId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(null, runnable, PREFIX + nextId.incrementAndGet(), 0, false);
        }
    }
}
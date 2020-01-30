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
import org.springframework.stereotype.Component;
import zenuo.gogo.core.processor.IProcessor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理器类，通道读取事件的回调
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public final class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 首页处理器
     */
    private final IProcessor indexProcessor;

    /**
     * 搜索处理器
     */
    private final IProcessor searchProcessor;

    /**
     * 补全处理器
     */
    private final IProcessor lintProcessor;

    /**
     * 处理工作者线程池
     */
    private final ThreadPoolExecutor processWorkers;

    /**
     * 工作队列
     */
    private final BlockingQueue<Runnable> workQueue;

    public Handler(IProcessor indexProcessor, IProcessor searchProcessor, IProcessor lintProcessor) {
        this.indexProcessor = indexProcessor;
        this.searchProcessor = searchProcessor;
        this.lintProcessor = lintProcessor;
        this.workQueue = new ArrayBlockingQueue<>(256);
        this.processWorkers = new ThreadPoolExecutor(2, 8,
                30, TimeUnit.SECONDS,
                workQueue,
                new GogoThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (workQueue.remainingCapacity() <= 0) {
            log.warn("too many requests, client: {}, request: {}", ctx.channel(), request);
            indexProcessor.response(ctx,
                    request,
                    ResponseType.API,
                    "{\"error\": \"too many requests, please try again later.\"}",
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
                case "/api/lint":
                    lintProcessor.process(ctx, request, decoder, null);
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

/**
 * 线程池
 */
class GogoThreadFactory implements ThreadFactory {
    private final static String PREFIX = "gogo-worker-";
    private final AtomicInteger nextId = new AtomicInteger();

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(null, runnable, PREFIX + nextId.incrementAndGet(), 0, false);
    }
}
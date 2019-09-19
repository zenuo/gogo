package zenuo.gogo.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zenuo.gogo.core.config.GogoConfig;

import javax.annotation.PostConstruct;

/**
 * 服务器
 *
 * @author zenuo
 * @date 2019/05/08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public final class Server {

    @NonNull
    private final GogoConfig gogoConfig;

    @NonNull
    private final Handler handler;

    @PostConstruct
    private void postConstruct() {
        //acceptor
        final NioEventLoopGroup boss = new NioEventLoopGroup(1);
        //client
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap()
                    //设置事件循环组
                    .group(boss, worker)
                    //Channel类型
                    .channel(NioServerSocketChannel.class)
                    //在Linux下，Accept队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //Netty框架日志级别
                    .handler(new LoggingHandler(LogLevel.TRACE))
                    //通道初始化器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    //HTTP服务器编码
                                    .addLast(new HttpServerCodec())
                                    //HTTP对象聚合器
                                    .addLast(new HttpObjectAggregator(16 * 1024))
                                    //分块写处理器
                                    .addLast(new ChunkedWriteHandler())
                                    //业务处理器
                                    .addLast(handler);
                        }
                    })
                    //验证
                    .validate();
            //绑定端口
            bootstrap.bind(gogoConfig.getPort())
                    //阻塞
                    .sync();
            log.info("端口{}已绑定", gogoConfig.getPort());
        } catch (Exception e) {
            log.error("引导服务器异常", e);
        }
    }
}

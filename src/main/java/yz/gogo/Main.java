package yz.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import yz.gogo.core.Config;
import yz.gogo.core.Handler;

/**
 * 入口类 Entry point
 *
 * @author zenuo
 * 2018-06-02 19:12:15
 */
@Slf4j
public final class Main {
    public static void main(String[] args) {
        //加载配置文件
        Config.INSTANCE.init();

        final NioEventLoopGroup boss = new NioEventLoopGroup(1);
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            final ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 0, 30))
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(65536))
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new Handler());
                        }
                    })
                    .validate();
            //绑定端口
            final Channel channel = bootstrap.bind(Config.INSTANCE.getPort())
                    .sync()
                    .channel();
            log.info("Bond port " + Config.INSTANCE.getPort());
            //阻塞至通道关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("主类异常", e);
        } finally {
            //关闭线程组
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

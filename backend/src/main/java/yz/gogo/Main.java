package yz.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import yz.gogo.core.Initializer;

@Slf4j
public class Main {

    private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

    public static void main(String[] args) {
        final NioEventLoopGroup boss = new NioEventLoopGroup(1);
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new Initializer());

            final Channel channel = bootstrap.bind("0.0.0.0", PORT).sync().channel();

            log.info("Open your web browser and navigate to http://localhost:" + PORT + '/');

            //阻塞至通道关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("main", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

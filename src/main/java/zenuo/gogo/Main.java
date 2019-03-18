package zenuo.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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
import lombok.extern.slf4j.Slf4j;
import zenuo.gogo.core.Config;
import zenuo.gogo.core.Handler;

/**
 * 入口类
 *
 * @author zenuo
 * 2018-06-02 19:12:15
 */
@Slf4j
public final class Main {
    public static void main(String[] args) {
        //加载配置文件
        log.info("initialized, port={}, day-mode-start-time={}, day-mode-end-time={}, slogan={}, 匹配规则={}",
                Config.INSTANCE.getPort(),
                Config.INSTANCE.getDayModeStartTime(),
                Config.INSTANCE.getDayModeEndTime(),
                Config.INSTANCE.getSlogan(),
                Config.INSTANCE.getSubstituteRuleMap());
        //acceptor
        final NioEventLoopGroup boss = new NioEventLoopGroup(1);
        //client
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        //handler
        final Handler handler = new Handler();
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
            final Channel channel = bootstrap
                    .bind(Config.INSTANCE.getPort())
                    //阻塞
                    .sync()
                    .channel();
            log.info("端口{}已绑定", Config.INSTANCE.getPort());
            //阻塞至通道关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("引导服务器异常", e);
        } finally {
            //关闭线程组
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

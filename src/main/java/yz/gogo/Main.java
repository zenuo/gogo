package yz.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import yz.gogo.core.Constants;
import yz.gogo.core.Initializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Matcher;

@Slf4j
public final class Main {
    public static void main(String[] args) {
        //加载配置文件
        final Path path = Paths.get("./substitute.conf");
        if (Files.exists(path)) {
            try {
                Files.lines(path, StandardCharsets.UTF_8)
                        //非空行
                        .filter(((Predicate<String>) String::isEmpty).negate())
                        //非注释行
                        .filter(line -> !line.startsWith("#"))
                        .forEach(line -> {
                            //正则匹配
                            final Matcher matcher = Constants.SUBSTITUTE_RULE_PATTERN.matcher(line);
                            //若匹配
                            if (matcher.find()) {
                                //加入到规则集合中
                                final String source = matcher.group(1);
                                final String target = matcher.group(2);
                                Constants.SUBSTITUTE_RULE_MAP.put(source, target);
                            }
                        });
            } catch (IOException e) {
                log.error("读取替换规则配置文件错误", e);
            }
        } else {
            log.info("替换规则配置文件不存在");
        }
        //监听端口
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
            final Channel channel = bootstrap.bind("0.0.0.0", Constants.PORT)
                    .sync()
                    .channel();
            log.info("Bond port {}", Constants.PORT);
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

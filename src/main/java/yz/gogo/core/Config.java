package yz.gogo.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;

/**
 * 配置
 *
 * @author zenuo
 * 2018-07-08 21:14:09
 */
@Slf4j
@Getter
public enum Config {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 配置文件路径
     */
    private final static String CONF_FILE_PATH = "gogo.conf";

    /**
     * 替换规则的映射
     */
    private final ConcurrentHashMap<String, String> substituteRuleMap = new ConcurrentHashMap<>();
    /**
     * 端口
     */
    private int port;
    /**
     * 日间模式开始时间
     */
    private LocalTime dayModeStartTime;
    /**
     * 日间模式结束时间
     */
    private LocalTime dayModeEndTime;
    /**
     * 标语
     */
    private String slogan;

    /**
     * 初始化
     */
    public void init() {
        //若配置文件存在
        if (Files.exists(Paths.get(CONF_FILE_PATH))) {
            final Properties conf = new Properties();
            try (//文件输入流
                 final FileInputStream fis = new FileInputStream(CONF_FILE_PATH);
                 //输入流读者
                 final InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                //读取
                conf.load(reader);

                //读取端口
                final String port = conf.getProperty("port");
                if (port != null) {
                    this.port = Integer.parseInt(port);
                } else {
                    this.port = Constants.DEFAULT_PORT;
                }

                //读取日间模式开始时间
                final String dayModeStartTime = conf.getProperty("day-mode-start-time");
                if (dayModeStartTime != null) {
                    this.dayModeStartTime = LocalTime.parse(dayModeStartTime);
                } else {
                    this.dayModeStartTime = Constants.DEFAULT_DAY_MODE_START_TIME;
                }

                //读取日间模式结束时间
                final String dayModeEndTime = conf.getProperty("day-mode-end-time");
                if (dayModeEndTime != null) {
                    this.dayModeEndTime = LocalTime.parse(dayModeEndTime);
                } else {
                    this.dayModeEndTime = Constants.DEFAULT_DAY_MODE_END_TIME;
                }

                //读取标语
                final String slogan = conf.getProperty("slogan");
                if (slogan != null) {
                    this.slogan = slogan;
                } else {
                    this.slogan = Constants.DEFAULT_SLOGAN;
                }

                //记录配置项
                log.info("initialized, port={}, day-mode-start-time={}, day-mode-end-time={}, slogan={}",
                        this.port,
                        this.dayModeStartTime,
                        this.dayModeEndTime,
                        this.slogan);

                //加载替换规则的映射
                final Path substituteConfPath = Paths.get("." + File.separatorChar + "substitute.conf");
                if (Files.exists(substituteConfPath)) {
                    try {
                        Files.lines(substituteConfPath, StandardCharsets.UTF_8)
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
                                        this.substituteRuleMap.put(source, target);
                                    }
                                });
                        log.info("匹配规则:" + this.substituteRuleMap);
                    } catch (IOException e) {
                        log.error("读取替换规则配置文件错误", e);
                    }
                } else {
                    log.info("替换规则配置文件不存在");
                }
            } catch (Exception e) {
                log.warn("加载配置文件", e);
            }
        } else {
            //若配置文件不存在
            log.error("文件'{}'不存在", CONF_FILE_PATH);
            System.exit(1);
        }
    }
}

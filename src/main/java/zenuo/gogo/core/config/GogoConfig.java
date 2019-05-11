package zenuo.gogo.core.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * 配置
 *
 * @author zenuo
 * 2018-07-08 21:14:09
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gogo")
public class GogoConfig {

    /**
     * 替换规则的映射
     */
    private final ConcurrentHashMap<String, String> substituteRuleMap = new ConcurrentHashMap<>();
    /**
     * 端口
     */
    private Integer port;
    /**
     * 日间模式开始时间
     */
    private LocalTime dayModeStartTime;
    private String dayModeStartTimeString;

    /**
     * 日间模式结束时间
     */
    private LocalTime dayModeEndTime;
    private String dayModeEndTimeString;
    /**
     * 标语
     */
    private String slogan;

    /**
     * 替换规则列表
     */
    private List<String> substitute;

    @PostConstruct
    private void postConstruct() {
        if (port == null) {
            port = Constants.DEFAULT_PORT;
        }
        if (StringUtils.isEmpty(dayModeStartTimeString)) {
            dayModeStartTime = Constants.DEFAULT_DAY_MODE_START_TIME;
        } else {
            dayModeStartTime = LocalTime.parse(dayModeStartTimeString);
        }
        if (StringUtils.isEmpty(dayModeEndTimeString)) {
            dayModeEndTime = Constants.DEFAULT_DAY_MODE_END_TIME;
        } else {
            dayModeEndTime = LocalTime.parse(dayModeEndTimeString);
        }
        if (slogan == null) {
            slogan = Constants.DEFAULT_SLOGAN;
        }
        if (substitute != null) {
            //替换规则
            substitute.forEach(line -> {
                //正则匹配
                final Matcher matcher = Constants.SUBSTITUTE_RULE_PATTERN.matcher(line);
                //若匹配
                if (matcher.find()) {
                    //加入到规则集合中
                    final String source = matcher.group(1).trim();
                    final String target = matcher.group(2).trim();
                    this.substituteRuleMap.put(source, target);
                }
            });
        }
        log.info("port={}, day-mode-start-time={}, day-mode-end-time={}, slogan={}, 替换规则={}",
                port,
                dayModeStartTime,
                dayModeEndTime,
                slogan,
                substituteRuleMap);
    }
}

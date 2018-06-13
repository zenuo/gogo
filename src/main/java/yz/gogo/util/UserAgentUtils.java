package yz.gogo.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户代理工具类
 */
@Slf4j
final class UserAgentUtils {
    /**
     * 用户代理字符串数组
     */
    private static String[] USER_AGENTS = {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36",
            "Mozilla/5.0 (X11; FreeBSD amd64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36",
    };

    /**
     * 最后一次使用的索引
     */
    private static AtomicInteger INDEX = new AtomicInteger(0);

    /**
     * 获取用户代理字符串
     *
     * @return 用户代理字符串
     */
    static String get() {
        if (INDEX.get() > USER_AGENTS.length - 1) {
            //重置
            INDEX.set(0);
        }
        return USER_AGENTS[INDEX.getAndIncrement()];
    }
}

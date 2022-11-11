package zenuo.gogo.util;

/**
 * 用户代理工具类
 *
 */
public final class UserAgentUtils {

    /**
     * 用户代理字符串数组
     */
    private static final String[] USER_AGENTS = {
            "Lynx/2.8.5rel.2 libwww-FM",
            "Lynx/2.7.5rel.1 libwww-FM",
            "Lynx/1.9.5rel.9 libwww-FM",
            "Lynx/1.8.1rel.3 libwww-FM",
    };
    /**
     * 最后一次使用的索引
     */
    private static int INDEX = 0;

    private UserAgentUtils() {
    }

    /**
     * 获取用户代理字符串
     *
     * @return 用户代理字符串
     */
    public static synchronized String get() {
        if (INDEX > USER_AGENTS.length - 1) {
            //重置
            INDEX = 0;
        }
        return USER_AGENTS[INDEX++];
    }
}

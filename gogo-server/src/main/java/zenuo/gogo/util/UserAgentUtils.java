package zenuo.gogo.util;

/**
 * 用户代理工具类
 * 
 * https://www.zytrax.com/tech/web/mobile_ids.html
 */
public final class UserAgentUtils {

    /**
     * 用户代理字符串数组
     */
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Mobile; Nokia 8110 4G; rv:46.0) Gecko/46.0 Firefox/46.0 KAIOS/2.5",
            "Mozilla/5.0 (Mobile; Nokia 8110 4G; rv:47.0) Gecko/47.0 Firefox/47.0 KAIOS/2.5",
            "Mozilla/5.0 (Mobile; Nokia 8110 4G; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",
            "Mozilla/5.0 (Mobile; Nokia 8110 4G; rv:49.0) Gecko/49.0 Firefox/49.0 KAIOS/2.5",
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

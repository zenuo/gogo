package zenuo.gogo.util;

/**
 * 谷歌域名工具类
 */
public final class GoogleDomainUtils {

    private final static String WWW_GOOGLE_COM = "www.google.com";

    /**
     * 防止被实例化
     */
    private GoogleDomainUtils() {

    }

    /**
     * 获取域名
     *
     * @return 域名字符串
     */
    public static String get() {
        return WWW_GOOGLE_COM;
    }
}
package zenuo.gogo.util;

import lombok.NonNull;

/**
 * 字符串工具
 *
 * @author zenuo
 * @date 2019/05/23
 */
public final class StringUtils {
    private StringUtils() {
    }

    /**
     * HTML消毒处理
     *
     * @param source 源字符串
     * @return 处理之后的字符串
     */
    public static String htmlSterilize(@NonNull String source) {
        return source
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }
}

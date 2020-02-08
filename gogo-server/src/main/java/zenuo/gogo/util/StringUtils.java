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
     * 转义HTML实体
     * <p>
     * 参考: https://developer.mozilla.org/en-US/docs/Glossary/Entity
     *
     * @param source 源字符串
     * @return 处理之后的字符串
     */
    public static String escapeHtmlEntities(@NonNull String source) {
        return source
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }
}

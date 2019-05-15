package zenuo.gogo.core.processor;

/**
 * 替换处理器
 *
 * @author zenuo
 * @date 2019/05/15
 */
public interface ISubstituteProcessor {
    /**
     * 根据替换规则，处理HTML字符串
     *
     * @param sourceHtml 源字符串
     * @return 替换后的字符串
     */
    String substitute(String sourceHtml);
}

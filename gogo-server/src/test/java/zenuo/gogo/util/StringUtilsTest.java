package zenuo.gogo.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 字符串工具类测试
 *
 * @author zenuo
 * @date 2019/05/23
 */
public class StringUtilsTest {

    @Test
    public void escapeHtmlEntities() {
        Assert.assertEquals(StringUtils.escapeHtmlEntities(""), "");
        Assert.assertEquals(StringUtils.escapeHtmlEntities("<"), "&lt;");
        Assert.assertEquals(StringUtils.escapeHtmlEntities(">"), "&gt;");
        Assert.assertEquals(StringUtils.escapeHtmlEntities("\""), "&quot;");
        Assert.assertEquals(StringUtils.escapeHtmlEntities("&"), "&amp;");
    }
}
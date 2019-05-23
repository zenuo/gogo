package zenuo.gogo.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * 测试
 *
 * @author zenuo
 * @date 2019/05/23
 */
public class StringUtilsTest {

    @Test
    public void testHtmlSterilize() {
        Assert.assertEquals(StringUtils.htmlSterilize(""), "");
        Assert.assertEquals(StringUtils.htmlSterilize("<"), "&lt;");
        Assert.assertEquals(StringUtils.htmlSterilize(">"), "&gt;");
    }
}
package zenuo.gogo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * 测试环境
 *
 * @author zenuo
 * @date 2019/05/08
 */
@Slf4j
@SpringBootTest(classes = GogoApplication.class)
public class TestEnvironmentWithoutTx extends AbstractTestNGSpringContextTests {

}

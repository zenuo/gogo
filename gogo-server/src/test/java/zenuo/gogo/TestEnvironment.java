package zenuo.gogo;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试环境
 *
 * @author zenuo
 * @date 2019/05/08
 */
@Slf4j
@org.testng.annotations.Guice(modules = GogoModule.class)
public abstract class TestEnvironment {
}

package zenuo.gogo.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.config.GogoConfig;
import zenuo.gogo.model.IResponse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 主页构建器
 *
 * @author zenuo
 * 2018-07-08 20:50:25
 */
@Slf4j
public final class IndexPageBuilder implements IIndexPageBuilder {

    private final GogoConfig gogoConfig = ApplicationConfig.gogoConfig();

    private final String html;

    {
        try (final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("web/index.html")) {
            final String indexHtml = IOUtils.toString(Objects.requireNonNull(resourceAsStream), StandardCharsets.UTF_8);
            html = indexHtml.replace("__SLOGAN__", gogoConfig.getSlogan());
        } catch (Exception e) {
            log.error("build index error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建主页
     *
     * @return 主页的字符串
     */
    @Override
    public String build(IResponse response) {
        //返回字符串
        return html;
    }
}

package zenuo.gogo.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import zenuo.gogo.model.IResponse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public final class IndexPageBuilder implements IIndexPageBuilder {

    private final byte[] htmlBytes;

    public IndexPageBuilder() {
        try (final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("web/index.html")) {
            final String indexHtml = IOUtils.toString(Objects.requireNonNull(resourceAsStream), StandardCharsets.UTF_8);
            htmlBytes = indexHtml.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("build index error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] build(IResponse response) {
        return htmlBytes;
    }
}

package zenuo.gogo.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import zenuo.gogo.model.IResponse;

import java.io.InputStream;
import java.util.Objects;

@Slf4j
public final class IndexPageBuilder implements IIndexPageBuilder {

    private final byte[] htmlBytes;

    public IndexPageBuilder() {
        try (final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("web/index.html")) {
            htmlBytes = IOUtils.toByteArray(Objects.requireNonNull(resourceAsStream));
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

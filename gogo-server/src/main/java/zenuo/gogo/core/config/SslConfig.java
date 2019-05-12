package zenuo.gogo.core.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * SSL配置
 *
 * @author zenuo
 * @date 2019/05/11
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ssl")
public class SslConfig {
    /**
     * 是否启用SSL
     */
    private Boolean enabled;

    /**
     * An X.509 certificate chain file in PEM format
     */
    private String keyCertChainFile;

    /**
     * A PKCS#8 private key file in PEM format
     */
    private String keyFile;

    @PostConstruct
    private void postConstruct() {
        if (enabled) {
            log.info("ssl enabled, keyCertChainFile={}, keyFile={}", keyCertChainFile, keyFile);
        }
    }
}

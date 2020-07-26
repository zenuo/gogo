package zenuo.gogo.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP客户端配置
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Setter
@Getter
public class HttpClientConfig {
    private Integer maxTotal;
    private Integer defaultMaxPerRoute;
    private Integer connectTimeout;
    private Integer connectionRequestTimeout;
    private Integer socketTimeout;
}

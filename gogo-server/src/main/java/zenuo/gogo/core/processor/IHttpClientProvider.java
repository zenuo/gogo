package zenuo.gogo.core.processor;

import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP客户端提供者
 *
 * @author zenuo
 * @date 2019/05/15
 */
public interface IHttpClientProvider {
    /**
     * 执行HTTP GET请求
     *
     * @param httpGet GET请求
     * @return 响应体
     * @throws IOException IO异常
     */
    String httpGet(HttpGet httpGet) throws IOException;
}

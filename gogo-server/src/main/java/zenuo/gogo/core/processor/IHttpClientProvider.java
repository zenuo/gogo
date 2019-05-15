package zenuo.gogo.core.processor;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

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
     * 执行HTTP请求
     *
     * @param httpRequestBase HTTP请求
     * @return 响应体
     * @throws IOException IO异常
     */
    String execute(HttpRequestBase httpRequestBase) throws IOException;
}

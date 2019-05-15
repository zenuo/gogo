package zenuo.gogo.core.processor.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import zenuo.gogo.core.processor.IHttpClientProvider;
import zenuo.gogo.core.processor.ISubstituteProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP客户端提供者实现
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Component
@RequiredArgsConstructor
final class HttpClientProviderImpl implements IHttpClientProvider {

    @NonNull
    private final ISubstituteProcessor substituteProcessor;

    @NonNull
    private final CloseableHttpClient closeableHttpClient;

    @NonNull
    private final RequestConfig requestConfig;

    @Override
    public String httpGet(HttpGet httpGet) throws IOException {
        //设置
        httpGet.setConfig(requestConfig);
        //自动资源管理
        try (final CloseableHttpResponse response = closeableHttpClient.execute(httpGet)) {
            //读取响应体
            final String body = StreamUtils.copyToString((response.getEntity()).getContent(), StandardCharsets.UTF_8);
            //替换
            return substituteProcessor.substitute(body);
        }
    }
}

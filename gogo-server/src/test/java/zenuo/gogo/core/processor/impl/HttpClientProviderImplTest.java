package zenuo.gogo.core.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;
import zenuo.gogo.TestEnvironment;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * 测试
 *
 * @author zenuo
 * @date 2019/05/15
 */
@Slf4j
public class HttpClientProviderImplTest extends TestEnvironment {

    private final HttpClientProviderImpl httpClientProvider = ServiceLoader.load(HttpClientProviderImpl.class).iterator().next();

    @Test
    public void testHttpGet() throws InterruptedException {
        final String url = "https://wx.qq.com/";
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");

        //5个线程
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                //每个线程5次
                for (int j = 0; j < 5; j++) {
                    try {
                        final String o = httpClientProvider.execute(httpGet);
                        log.info("body hashcode={}", o.hashCode());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "test-" + (i + 1)).start();
        }
        //等待5秒
        Thread.currentThread().join(5000L);
    }
}
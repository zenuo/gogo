package zenuo.gogo.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import zenuo.gogo.util.JsonUtils;

import java.io.IOException;

/**
 * 测试
 *
 * @author zenuo
 * @date 2019/05/08
 */
public class SearchResponseTest {

    @Test
    public void deserialize() throws IOException {
        final String json = "{\"key\":\"LRU\",\"page\":1,\"amount\":5490000,\"elapsed\":0.46,\"entries\":[{\"name\":\"Cache replacement policies - Wikipedia\",\"url\":\"https://en.wikipedia.org/wiki/Cache_replacement_policies\",\"desc\":\"In computing, cache algorithms are optimizing instructions, or algorithms, that a computer ... Even worse, many cache algorithms (in particular, LRU) allow this streaming data to fill the cache, pushing out of the cache information that will be ...\"}]}";
        final SearchResponse searchResponse = JsonUtils.fromJson(json, SearchResponse.class);
        Assert.assertEquals(searchResponse.getEntries().size(), 1);
    }
}
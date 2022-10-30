package zenuo.gogo.core.processor.impl;

import org.jsoup.nodes.Document;
import org.testng.annotations.Test;
import zenuo.gogo.TestEnvironment;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.JsonUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.ServiceLoader;

public class GoogleSearchResultProviderImplTest extends TestEnvironment {

    @Inject
    private GoogleSearchResultProviderImpl searchResultProvider;

    @Test
    public void request() throws Exception {

        final Document document = searchResultProvider.httpGet("udp", 1);
        if (document != null) {
            System.out.println(document.html());
        }
    }

    @Test
    public void search() {
        final SearchResponse response = searchResultProvider.search("udp", 1);
        if (!response.getEntries().isEmpty()) {
            response.getEntries().forEach(e -> System.out.println(e.getName()));
        }
    }

    @Test
    public void response() {
        final SearchResponse searchResponse = searchResultProvider.search("udp", 2);
        final byte[] json = JsonUtils.toJsonBytes(searchResponse);
        System.out.println(Arrays.toString(json));
    }
}

package yz.gogo;

import org.jsoup.nodes.Document;
import org.junit.Test;
import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;
import yz.gogo.util.JsonUtils;
import yz.gogo.util.SearchUtils;

import java.io.IOException;
import java.util.List;

public class SearchUtilsTest {
    @Test
    public void request() throws IOException {
        final Document document = SearchUtils.request("udp", 1);
        if (document != null) {
            System.out.println(document.title());
        }
    }

    @Test
    public void search() throws IOException {
        final List<Entry> entries = SearchUtils.search("udp", 1);
        if (entries != null) {
            entries.forEach(e -> System.out.println(e.getName()));
        }
    }

    @Test
    public void response() {
        final SearchResponse searchResponse = SearchUtils.response("udp", 2);
        final String json = JsonUtils.toJson(searchResponse);
        System.out.println(json);
    }
}

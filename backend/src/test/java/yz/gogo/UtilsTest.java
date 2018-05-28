package yz.gogo;

import org.jsoup.nodes.Document;
import org.junit.Test;
import yz.gogo.model.Entry;
import yz.gogo.model.SearchResponse;

import java.io.IOException;
import java.util.List;

public class UtilsTest {
    @Test
    public void request() throws IOException {
        final Document document = Utils.request("udp", 1);
        if (document != null) {
            System.out.println(document.title());
        }
    }

    @Test
    public void search() throws IOException {
        final List<Entry> entries = Utils.search("udp", 1);
        if (entries != null) {
            entries.forEach(e -> System.out.println(e.getName()));
        }
    }

    @Test
    public void response() {
        final SearchResponse searchResponse = Utils.response("udp", 2);
        final String json = Utils.toJson(searchResponse);
        System.out.println(json);
    }
}

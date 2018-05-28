package yz.gogo;

import org.jsoup.nodes.Document;
import org.junit.Test;
import yz.gogo.model.Entry;

import java.util.List;

public class UtilsTest {
    @Test
    public void request() {
        final Document document = Utils.request("udp", 1);
        if (document != null) {
            System.out.println(document.title());
        }
    }

    @Test
    public void search() {
        final List<Entry> entries = Utils.search("udp", 1);
        if (entries != null) {
            entries.forEach(e -> System.out.println(e.getName()));
        }
    }
}

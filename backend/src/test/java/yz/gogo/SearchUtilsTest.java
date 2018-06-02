package yz.gogo;

import org.jsoup.nodes.Document;
import org.junit.Test;
import yz.gogo.core.Constants;
import yz.gogo.model.SearchResponse;
import yz.gogo.util.JsonUtils;
import yz.gogo.util.SearchUtils;

import java.io.IOException;
import java.util.regex.Matcher;

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
        final SearchResponse response = SearchUtils.search("udp", 1);
        if (response.getEntries() != null) {
            response.getEntries().forEach(e -> System.out.println(e.getName()));
        }
    }

    @Test
    public void response() {
        final SearchResponse searchResponse = SearchUtils.response("udp", 2);
        final String json = JsonUtils.toJson(searchResponse);
        System.out.println(json);
    }

    @Test
    public void stats() {
        final Matcher matcher = Constants.STATS_RESULTS_PATTERN.matcher("About 41,400,000 results (0.31 seconds)");
        System.out.println(matcher.find());
        System.out.println(matcher.groupCount());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
    }
}

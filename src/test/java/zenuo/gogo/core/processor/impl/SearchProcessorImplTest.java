package zenuo.gogo.core.processor.impl;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import zenuo.gogo.TestEnvironmentWithoutTx;
import zenuo.gogo.core.config.Constants;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.JsonUtils;

import java.io.IOException;
import java.util.regex.Matcher;

public class SearchProcessorImplTest extends TestEnvironmentWithoutTx {
    @Autowired
    private SearchProcessorImpl searchProcessor;

    @Test
    public void request() throws IOException {

        final Document document = searchProcessor.request("udp", 1);
        if (document != null) {
            System.out.println(document.html());
        }
    }

    @Test
    public void search() {
        final SearchResponse response = searchProcessor.search("udp", 1);
        if (response.getEntries() != null) {
            response.getEntries().forEach(e -> System.out.println(e.getName()));
        }
    }

    @Test
    public void response() {
        final SearchResponse searchResponse = searchProcessor.response("udp", 2);
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

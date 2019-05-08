package zenuo.gogo.core.processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import zenuo.gogo.TestEnvironment;
import zenuo.gogo.core.config.Constants;
import zenuo.gogo.model.CompleteResponse;
import zenuo.gogo.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public class CompleteProcessorImplTest extends TestEnvironment {

    @Autowired
    private CompleteProcessorImpl completeProcessor;

    @Test
    public void request() throws IOException {

        final Document document = completeProcessor.request("udp");
        System.out.println(document.body().text());
    }

    @Test
    public void parse() {
        final String completeJson = "[\"udp\",[[\"udp\",0],[\"udp\\u003cb\\u003e vs tcp\\u003c\\/b\\u003e\",0],[\"udp\\u003cb\\u003e protocol\\u003c\\/b\\u003e\",0],[\"udp\\u003cb\\u003e port\\u003c\\/b\\u003e\",0],[\"\\u003cb\\u003eusps\\u003c\\/b\\u003e\",0,[10],{\"za\":\"\\u003csc\\u003eusps\\u003c\\/sc\\u003e\",\"zb\":\"\\u003cse\\u003eudps\\u003c\\/se\\u003e\"}],[\"udp\\u003cb\\u003e header\\u003c\\/b\\u003e\",0],[\"udp\\u003cb\\u003e glucose\\u003c\\/b\\u003e\",0],[\"\\u003cb\\u003eusps\\u003c\\/b\\u003e\\u003cb\\u003e tracking\\u003c\\/b\\u003e\",0,[10],{\"za\":\"\\u003csc\\u003eusps\\u003c\\/sc\\u003e tracking\",\"zb\":\"\\u003cse\\u003eudps\\u003c\\/se\\u003e tracking\"}],[\"udp\\u003cb\\u003e unicorn\\u003c\\/b\\u003e\",0,[131]],[\"udp\\u003cb\\u003e-203\\u003c\\/b\\u003e\",0]],{\"q\":\"oqDSF-NQDrEIY2BaPtxQ4zon7GA\",\"t\":{\"bpc\":false,\"tlw\":false}}]";
        try {
            final JsonNode jsonNode = Constants.MAPPER.readTree(completeJson);
            final JsonNode lints = jsonNode.get(1);
            lints.forEach(lint -> System.out.println(lint.get(0).asText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void complete() throws IOException {
        final List<String> lints = completeProcessor.complete("udp");
        System.out.println(lints);
    }

    @Test
    public void response() {
        final CompleteResponse response = completeProcessor.response("udp");
        System.out.println(JsonUtils.toJson(response));
    }
}

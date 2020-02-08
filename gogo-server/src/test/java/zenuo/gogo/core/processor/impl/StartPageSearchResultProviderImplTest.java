package zenuo.gogo.core.processor.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;
import zenuo.gogo.model.Entry;
import zenuo.gogo.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试
 *
 * @author zenuo
 * @date 2019/05/15
 */
public class StartPageSearchResultProviderImplTest {

    @Test
    public void parse() throws IOException {
        final Document document = Jsoup.parse(new File("./startpage.html"), "UTF-8");
        final Elements results = document.getElementsByClass("search-result search-item");
        final List<Entry> entries = new ArrayList<>(10);
        //遍历
        for (Element result: results) {
            final Entry.EntryBuilder entryBuilder = Entry.builder();
            final Element h3 = result.getElementsByClass("search-item__title").first();
            if (h3 == null) {
                continue;
            }
            final Element a = h3.child(0);
            entryBuilder.name(StringUtils.escapeHtmlEntities(a.text()));
            entryBuilder.url(a.attr("href"));
            final Element p = result.getElementsByClass("search-item__body").first();
            if (p == null) {
                entries.add(entryBuilder.build());
                continue;
            }
            entryBuilder.desc(StringUtils.escapeHtmlEntities(p.text()));
            entries.add(entryBuilder.build());
        }
        System.out.println(entries);
    }
}
package zenuo.gogo.web;

import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.config.GogoConfig;
import zenuo.gogo.model.Entry;
import zenuo.gogo.model.IResponse;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

/**
 * 结果页面构建器
 *
 * @author zenuo
 * 2018-07-08 20:50:25
 */
public final class ResultPageBuilder implements IResultPageBuilder {

    private final GogoConfig gogoConfig = ApplicationConfig.gogoConfig();

    /**
     * 标题前的HTML
     */
    private static final String HTML_BEFORE_TITLE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\" />\n" +
            "<title>";

    /**
     * 样式表前的HTML
     */
    private static final String HTML_BEFORE_STYLE = " - 勾勾</title>\n<style>\n";

    /**
     * 夜间模式的样式表
     */
    private static final String HTML_NIGHT_MODE_STYLE = ".logo,body{color:#B6C5D4}." +
            "entry,.search{padding-bottom:5px;padding-top:5px}" +
            ".logo,.name{text-decoration:none}" +
            ".entry,.next,.search{padding-top:5px}" +
            "body{width:800px;padding-left:10px;background-color:#000}" +
            ".logo{float:left;padding-right:10px;font-family:\"Times New Roman\",Times,serif}" +
            ".entry{font-family:Roboto,arial,sans-serif}" +
            ".name{color:#aaab00;font-size:18px}" +
            ".url{color:#2bd8a4;font-size:14px}" +
            ".desc{font-size:16px}";

    /**
     * 日间模式的样式表
     */
    private static final String HTML_DAY_MODE_STYLE = "" +
            ".entry,.search{padding-bottom:5px;padding-top:5px}" +
            ".logo,.name{text-decoration:none}" +
            ".entry,.next,.search{padding-top:5px}" +
            "body{width:800px;padding-left:10px;background-color:#F8F4E7;}" +
            ".logo{float:left;padding-right:10px;color:#000;font-family:\"Times New Roman\",Times,serif}" +
            ".entry{font-family:Roboto,arial,sans-serif}" +
            ".name{color:#1a0dab;font-size:18px}" +
            ".url{color:#006621;font-size:14px}" +
            ".desc{font-size:16px}";

    /**
     * 样式表之后的HTML
     */
    private static final String HTML_AFTER_STYLE = "</style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div class=\"search\">\n" +
            "<a href=\"/\"><span class=\"logo\"><b>勾勾</b></span></a>\n" +
            "<datalist id=\"lints\"></datalist>\n" +
            "<form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\">\n" +
            "<input id=\"input\" name=\"q\" autocomplete=\"off\" list=\"lints\" type=\"search\" value=\"";

    /**
     * 结果条目之后的样式表
     */
    private static final String HTML_BEFORE_RESULT = "\" onkeyup=\"requestLints()\">\n" +
            "<button type=\"submit\">Go</button>\n" +
            "</form>\n" +
            "</div>\n";

    /**
     * 尾部的HTML
     */
    private static final String HTML_TAIL = "</body>\n" +
            "<script src=\"static/script.js\"></script>\n" +
            "</html>";

    /**
     * 错误的HTML
     */
    private static final String HTML_ERROR = "<h2>抱歉🥺，网络错误，请暂时使用<a href=\"https://cn.bing.com/search?q=%s\">Bing Search</a></h2>";

    /**
     * 由响应示例构建页面
     *
     * @param iResponse 响应实例
     * @return 响应页面HTML字符串
     */
    @Override
    public byte[] build(IResponse iResponse) {
        final SearchResponse response = (SearchResponse) iResponse;
        final StringBuilder sb = new StringBuilder(HTML_BEFORE_TITLE);
        final String entitiesEscapedKey = StringUtils.escapeHtmlEntities(response.getKey());
        sb.append(entitiesEscapedKey)
                .append(HTML_BEFORE_STYLE);
        final LocalTime now = LocalTime.now();
        //若不是日间模式
        if (now.isBefore(gogoConfig.getDayModeStartTime()) ||
                now.isAfter(gogoConfig.getDayModeEndTime())) {
            sb.append(HTML_NIGHT_MODE_STYLE);
        } else {
            sb.append(HTML_DAY_MODE_STYLE);
        }
        sb.append(HTML_AFTER_STYLE)
                .append(entitiesEscapedKey)
                .append(HTML_BEFORE_RESULT);
        if (!response.getEntries().isEmpty()) {
            response.getEntries().forEach(e -> EntryBuilder.build(sb, e));
            NextBuilder.build(sb, response.getKey(), response.getPage());
        } else {
            sb.append(String.format(HTML_ERROR, URLEncoder.encode(response.getKey(), StandardCharsets.UTF_8)));
        }
        sb.append(HTML_TAIL);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}

/**
 * 条目构建器
 */
final class EntryBuilder {
    /**
     * 超链接之前的HTML
     */
    private static final String HTML_BEFORE_HREF = "<div>\n" +
            "<div class=\"entry\">\n" +
            "<a class=\"name\" href=\"";

    /**
     * 名称之前的HTML
     */
    private static final String HTML_BEFORE_NAME = "\">";

    /**
     * URL字符串之前的HTML
     */
    private static final String HTML_BEFORE_URL = "</a>\n" +
            "<br />\n" +
            "<span class=\"url\">";
    /**
     * 描述之前的HTML
     */
    private static final String HTML_BEFORE_DESC = "</span>\n" +
            "<br />\n" +
            "<span class=\"desc\">";

    /**
     * 尾部HTML
     */
    private static final String HTML_TAIL = "</span>\n" +
            "</div>\n" +
            "</div>";

    /**
     * 由条目实例构建页面
     *
     * @param target 目标StringBuilder实例
     * @param entry  条目实例
     */
    static void build(
            final StringBuilder target,
            final Entry entry
    ) {
        target.append(HTML_BEFORE_HREF)
                .append(entry.getUrl())
                .append(HTML_BEFORE_NAME)
                .append(entry.getName())
                .append(HTML_BEFORE_URL)
                .append(URLDecoder.decode(entry.getUrl(), StandardCharsets.UTF_8))
                .append(HTML_BEFORE_DESC)
                .append(entry.getDesc())
                .append(HTML_TAIL);
    }
}

/**
 * 下一页按钮构建器
 */
final class NextBuilder {
    /**
     * 关键字之前的HTML
     */
    private static final String HTML_BEFORE_KEY = "<div class=\"next\">\n" +
            "<a href=\"/search?q=";

    /**
     * 页码之前的HTML
     */
    private static final String HTML_BEFORE_PAGE = "&p=";

    /**
     * 尾部HTML
     */
    private static final String HTML_TAIL = "\"><button>Next</button></a>\n" +
            "</div>";

    /**
     * 构建下一页按钮
     *
     * @param target 目标StringBuilder实例
     * @param key    关键词字符串
     * @param page   当前页数
     */
    static void build(
            final StringBuilder target,
            final String key,
            final int page) {
        target.append(HTML_BEFORE_KEY)
                .append(key)
                .append(HTML_BEFORE_PAGE)
                .append(page + 1)
                .append(HTML_TAIL);
    }
}
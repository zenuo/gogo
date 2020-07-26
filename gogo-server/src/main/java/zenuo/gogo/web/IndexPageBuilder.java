package zenuo.gogo.web;

import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.config.GogoConfig;
import zenuo.gogo.model.IResponse;

import java.time.LocalTime;

/**
 * 主页构建器
 *
 * @author zenuo
 * 2018-07-08 20:50:25
 */
public final class IndexPageBuilder implements IIndexPageBuilder {

    private final GogoConfig gogoConfig = ApplicationConfig.gogoConfig();

    /**
     * 样式表之前的HTML字符串
     */
    private static final String HTML_BEFORE_STYLE
            = "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"utf-8\"/>" +
            "<title>勾勾</title>" +
            "<style>";

    /**
     * 样式表之后的HTML字符串
     */
    private String htmlAfterStyle;

    /**
     * 夜间模式的样式表
     */
    private static final String HTML_NIGHT_MODE_STYLE = "body{text-align:center;background-color:#000;color:#B6C5D4}" +
            "h1{font-size:50px;font-family:\"Times New Roman\",Times,serif}" +
            "footer{font-size:15px;font-family:Roboto,arial,sans-serif}" +
            ".main{margin:0 auto;width:50%;padding-bottom:50px}";

    /**
     * 日间模式的样式表
     */
    private static final String HTML_DAY_MODE_STYLE = "body{text-align:center;background-color:#F8F4E7;color:#552800}" +
            "h1{font-size:50px;font-family:\"Times New Roman\",Times,serif}" +
            "footer{font-size:15px;font-family:Roboto,arial,sans-serif}" +
            ".main{margin:0 auto;width:50%;padding-bottom:50px}";

    public IndexPageBuilder() {
        htmlAfterStyle = "</style>" +
                "</head>" +
                "<body>" +
                "<a href=\"https://github.com/zenuo/gogo\"><img style=\"position: absolute; top: 0; right: 0; border: 0;\" width=\"149\" height=\"149\" src=\"https://github.blog/wp-content/uploads/2008/12/forkme_right_orange_ff7600.png?resize=149%2C149\" class=\"attachment-full size-full\" alt=\"Fork me on GitHub\" data-recalc-dims=\"1\"></a>" +
                "<div class=\"main\">" +
                "<h1>勾勾</h1>" +
                "<form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\">" +
                "<input name=\"q\" autocomplete=\"off\" autofocus=\"autofocus\" type=\"text\">" +
                "<button value=\"Search\" type=\"submit\">Go</button>" +
                "</form>" +
                "</div>" +
                "<footer>"
                + gogoConfig.getSlogan()
                + "</footer>" +
                "</body>" +
                "</html>";
    }

    /**
     * 构建主页
     *
     * @return 主页的字符串
     */
    @Override
    public String build(IResponse response) {
        //字符串构建器，初始化内容为样式表之前的HTML
        final StringBuilder sb = new StringBuilder(HTML_BEFORE_STYLE);
        //当前时间
        final LocalTime now = LocalTime.now();
        if (now.isBefore(gogoConfig.getDayModeStartTime()) ||
                now.isAfter(gogoConfig.getDayModeEndTime())) {
            //若是夜间模式，拼接夜间模式样式表
            sb.append(HTML_NIGHT_MODE_STYLE);
        } else {
            //若是日间模式，拼接日间模式样式表
            sb.append(HTML_DAY_MODE_STYLE);
        }
        //拼接样式表之后的HTML
        sb.append(htmlAfterStyle);
        //返回字符串
        return sb.toString();
    }
}

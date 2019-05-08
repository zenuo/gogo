package zenuo.gogo.web;

import org.springframework.stereotype.Component;
import zenuo.gogo.core.config.Config;
import zenuo.gogo.model.IResponse;

import java.time.LocalTime;

/**
 * 主页构建器
 *
 * @author zenuo
 * 2018-07-08 20:50:25
 */
@Component("indexPageBuilder")
public final class IndexPageBuilder implements IPageBuilder {
    /**
     * 样式表之前的HTML字符串
     */
    private static final String HTML_BEFORE_STYLE
            = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/><title>Gogo</title><style>";

    /**
     * 样式表之后的HTML字符串
     */
    private static final String HTML_AFTER_STYLE
            = "</style></head><body><div class=\"main\"><h1>Gogo</h1><form action=\"/search\" method=\"GET\" onsubmit=\"return q.value!=''\"><input name=\"q\" autocomplete=\"off\" autofocus=\"autofocus\" type=\"text\"> <button value=\"Search\" type=\"submit\">Go</button></form></div><footer>"
            + Config.INSTANCE.getSlogan()
            + "</footer></body></html>";

    /**
     * 夜间模式的样式表
     */
    private static final String HTML_NIGHT_MODE_STYLE = "body{text-align:center;background-color:#000;color:#B6C5D4}h1{font-size:50px;font-family:\"Times New Roman\",Times,serif}footer{font-size:15px;font-family:Roboto,arial,sans-serif}.main{margin:0 auto;width:50%;padding-bottom:50px}";

    /**
     * 日间模式的样式表
     */
    private static final String HTML_DAY_MODE_STYLE = "body{text-align:center;background-color:#F8F4E7;color:#552800}h1{font-size:50px;font-family:\"Times New Roman\",Times,serif}footer{font-size:15px;font-family:Roboto,arial,sans-serif}.main{margin:0 auto;width:50%;padding-bottom:50px}";

    /**
     * 构建主页
     *
     * @return 主页的字符串
     */
    public String build(IResponse response) {
        //字符串构建器，初始化内容为样式表之前的HTML
        final StringBuilder sb = new StringBuilder(HTML_BEFORE_STYLE);
        //当前时间
        final LocalTime now = LocalTime.now();
        if (now.isBefore(Config.INSTANCE.getDayModeStartTime()) ||
                now.isAfter(Config.INSTANCE.getDayModeEndTime())) {
            //若是夜间模式，拼接夜间模式样式表
            sb.append(HTML_NIGHT_MODE_STYLE);
        } else {
            //若是日间模式，拼接日间模式样式表
            sb.append(HTML_DAY_MODE_STYLE);
        }
        //拼接样式表之后的HTML
        sb.append(HTML_AFTER_STYLE);
        //返回字符串
        return sb.toString();
    }
}

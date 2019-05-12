package zenuo.gogo.web;

import zenuo.gogo.model.IResponse;

/**
 * 页面构建器
 *
 * @author zenuo
 * @date 2019/05/07
 */
public interface IPageBuilder {
    /**
     * 构建网页
     *
     * @param response 响应
     * @return 网页的HTML字符串
     */
    String build(final IResponse response);
}

package zenuo.gogo.core.processor;

import zenuo.gogo.core.config.Constants;
import zenuo.gogo.exception.SearchException;
import zenuo.gogo.model.SearchResponse;
import zenuo.gogo.service.ICacheService;
import zenuo.gogo.util.JsonUtils;

import java.util.Optional;

/**
 * 搜索结果提供者
 *
 * @author zenuo
 * @date 2019/05/15
 */
public interface ISearchResultProvider {

    /**
     * 优先级
     *
     * @return 优先级；值越小，越优先
     */
    int priority();

    /**
     * 请求
     *
     * @param key  关键词
     * @param page 页码
     * @return 搜索结果
     * @throws SearchException 搜索异常
     */
    SearchResponse search(final String key, final int page) throws SearchException;

    /**
     * 读取缓存
     *
     * @param key  搜索关键词
     * @param page 页码
     * @return 搜索结果
     */
    static Optional<SearchResponse> readCache(ICacheService cacheService, String key, int page) {
        //从缓存服务中读取
        final String cacheKey = String.format(Constants.KEY_SEARCH_RESPONSE_PATTERN, key.hashCode(), page);
        final Optional<byte[]> value = cacheService.get(cacheKey);
        //若存在，反序列化
        return value.map(s -> JsonUtils.fromJsonBytes(s, SearchResponse.class));
    }

    /**
     * 写入缓存
     *
     * @param key            搜索关键词
     * @param page           页码
     * @param searchResponse 搜索结果
     */
    static void writeCache(ICacheService cacheService, String key, int page, SearchResponse searchResponse) {
        //序列化，键
        final String cacheKey = String.format(Constants.KEY_SEARCH_RESPONSE_PATTERN, key.hashCode(), page);
        //写入缓存
        cacheService.set(cacheKey, JsonUtils.toJsonBytes(searchResponse));
    }
}

package zenuo.gogo.core.processor;

import org.springframework.core.Ordered;
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
public interface ISearchResultProvider extends Ordered {
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
    default Optional<SearchResponse> readCache(ICacheService cacheService, String key, int page) {
        //从缓存服务中读取
        final String cacheKey = String.format(Constants.KEY_SEARCH_RESPONSE_PATTERN, key.hashCode(), page);
        final Optional<String> value = cacheService.get(cacheKey);
        //若存在
        if (value.isPresent()) {
            //更新存活时间
            cacheService.expire(cacheKey, Constants.SEARCH_RESPONSE_CACHE_TTL_IN_SECONDS);
            //反序列化
            return Optional.ofNullable(JsonUtils.fromJson(value.get(), SearchResponse.class));
        } else {
            //不存在
            return Optional.empty();
        }
    }

    /**
     * 写入缓存
     *
     * @param key            搜索关键词
     * @param page           页码
     * @param searchResponse 搜索结果
     */
    default void writeCache(ICacheService cacheService, String key, int page, SearchResponse searchResponse) {
        //序列化，键
        final String cacheKey = String.format(Constants.KEY_SEARCH_RESPONSE_PATTERN, key.hashCode(), page);
        //值
        final String value = JsonUtils.toJson(searchResponse);
        //写入缓存
        cacheService.setex(cacheKey, Constants.SEARCH_RESPONSE_CACHE_TTL_IN_SECONDS, value);
    }
}

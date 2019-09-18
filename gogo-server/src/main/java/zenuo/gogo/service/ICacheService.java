package zenuo.gogo.service;

import java.util.Optional;

/**
 * 缓存服务
 *
 * @author zenuo
 * @date 2019/05/08
 */
public interface ICacheService {

    /**
     * 设置键值对，并设置存活时间
     *
     * @param key     键
     * @param value   值
     */
    void set(String key, String value);

    /**
     * 根据键获取值
     *
     * @param key 键
     * @return 值
     */
    Optional<String> get(String key);
}

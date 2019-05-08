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
     * 根据指定的键
     *
     * @param keys 键的数组
     * @return 被删除的键的数量
     */
    Optional<Long> delete(String... keys);

    /**
     * 设置键值对，并设置存活时间
     *
     * @param key     键
     * @param seconds 存活时间
     * @param value   值
     */
    Optional<String> setex(String key, long seconds, String value);

    /**
     * 根据键获取值
     *
     * @param key 键
     * @return 值
     */
    Optional<String> get(String key);

    /**
     * 设置存活时间
     *
     * @param key     键
     * @param seconds 秒
     * @return 操作状态，若为empty，则操作异常，若为true，则成功，否则失败
     */
    Optional<Boolean> expire(String key, long seconds);

    /**
     * 若不存在某个键，则设置键值对及其存活时间（毫秒）
     *
     * @param key          键
     * @param milliseconds 毫秒
     * @param value        值
     * @return 操作状态，若为empty，则操作异常，若为true，则成功，否则失败
     */
    Optional<Boolean> psetIfNotExist(String key, long milliseconds, String value);

    /**
     * 尝试获取锁
     *
     * @param lockKey              锁键
     * @param requestId            请求ID
     * @param timeoutInMillisecond 超时，单位毫秒
     * @return 若成功，返回true，否则返回false
     */
    boolean tryGetLock(String lockKey, String requestId, long timeoutInMillisecond);

    /**
     * 尝试释放锁
     *
     * @param lockKey   锁键
     * @param requestId 请求ID
     * @return 若成功，返回true，否则返回false
     */
    boolean tryReleaseLock(String lockKey, String requestId);
}

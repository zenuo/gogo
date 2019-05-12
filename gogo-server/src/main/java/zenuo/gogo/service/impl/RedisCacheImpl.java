package zenuo.gogo.service.impl;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.stereotype.Component;
import zenuo.gogo.service.ICacheService;

import java.util.Objects;
import java.util.Optional;

/**
 * Redis缓存实现
 *
 * @author zenuo
 * @date 2019/05/08
 */
@Slf4j
@Component("redisCacheImpl")
@RequiredArgsConstructor
public final class RedisCacheImpl implements ICacheService {

    private static final String OK = "OK";

    private static final Long RELEASE_SUCCESS = 1L;

    private static final String SCRIPT_DEL_IF_EQUALS
            = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @NonNull
    private final GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool;

    @Override
    public Optional<Long> delete(@NonNull String... keys) {
        try (final StatefulRedisConnection<String, String> connection = connectionPool.borrowObject()) {
            return Optional.ofNullable(connection.sync().del(keys));
        } catch (Exception e) {
            log.error("操作redis出错", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> setex(@NonNull String key, long seconds, @NonNull String value) {
        //从连接池借出连接
        try (final StatefulRedisConnection<String, String> connection = connectionPool.borrowObject()) {
            //操作
            return Optional.ofNullable(connection.sync().setex(key, seconds, value));
        } catch (Exception e) {
            log.error("redis error", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> get(@NonNull String key) {
        //从连接池借出连接
        try (final StatefulRedisConnection<String, String> connection = connectionPool.borrowObject()) {
            //操作
            return Optional.ofNullable(connection.sync().get(key));
        } catch (Exception e) {
            log.error("redis error", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> expire(@NonNull String key, long seconds) {
        try (final StatefulRedisConnection<String, String> connection = connectionPool.borrowObject()) {
            return Optional.ofNullable(connection.sync().expire(key, seconds));
        } catch (Exception e) {
            log.error("redis error", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> psetIfNotExist(@NonNull String key, long milliseconds, @NonNull String value) {
        try (final StatefulRedisConnection<String, String> connection = connectionPool.borrowObject()) {
            final Optional<String> set = Optional.ofNullable(connection.sync().set(key, value, SetArgs.Builder.nx().px(milliseconds)));
            return Optional.of(set.isPresent() && OK.equals(set.get()));
        } catch (Exception e) {
            log.error("redis error", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean tryGetLock(@NonNull String lockKey, @NonNull String requestId, long timeoutInMillisecond) {
        return psetIfNotExist(lockKey, timeoutInMillisecond, requestId)
                .orElse(false);
    }

    @Override
    public boolean tryReleaseLock(@NonNull String lockKey, @NonNull String requestId) {
        try (final StatefulRedisConnection<String, String> connection = connectionPool.borrowObject()) {
            //执行求值
            final Object eval = connection.sync().eval(SCRIPT_DEL_IF_EQUALS, ScriptOutputType.INTEGER, new String[]{lockKey}, requestId);
            //返回值是否为1L
            return Objects.equals(eval, RELEASE_SUCCESS);
        } catch (Exception e) {
            log.error("redis error", e);
            return false;
        }
    }
}

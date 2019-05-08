package zenuo.gogo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;
import zenuo.gogo.TestEnvironment;
import zenuo.gogo.service.ICacheService;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 测试
 *
 * @author zenuo
 * @date 2019/05/08
 */
public class RedisCacheImplTest extends TestEnvironment {

    private final String key = "test_key";
    private final String value = "ok";

    @Autowired
    private ICacheService cacheService;

    @Test
    public void testSetex() throws InterruptedException {
        //设置
        final long ttl = 1L;
        cacheService.setex(key, ttl, value);

        //立刻查询
        final Optional<String> get = cacheService.get(key);
        //断言存在且相等
        Assert.assertTrue(get.isPresent());
        Assert.assertEquals(get.get(), value);

        //等待1秒
        TimeUnit.SECONDS.sleep(ttl);
        //断言不存在
        Assert.assertFalse(cacheService.get(key).isPresent());
    }

    @Test
    public void testGet() {
        //设置
        cacheService.setex(key, 2L, value);

        //立刻查询
        final Optional<String> get = cacheService.get(key);
        //断言存在且相等
        Assert.assertTrue(get.isPresent());
        Assert.assertEquals(get.get(), value);

        cacheService.delete(key);
    }

    @Test
    public void testExpire() {
        //设置
        cacheService.setex(key, 10L, value);
        //过期
        cacheService.expire(key, 0L);
        //断言不存在
        Assert.assertFalse(cacheService.get(key).isPresent());
    }

    @Test
    public void testPsetIfNotExist() {
        //设置
        cacheService.setex(key, 10L, value);
        //再次设置
        Optional<Boolean> psetIfNotExist = cacheService.psetIfNotExist(key, 1000L, value);
        //断言失败
        Assert.assertTrue(psetIfNotExist.isPresent());
        Assert.assertFalse(psetIfNotExist.get());
        //删除
        cacheService.delete(key);

        //再次设置
        psetIfNotExist = cacheService.psetIfNotExist(key, 1000L, value);
        //断言成功
        Assert.assertTrue(psetIfNotExist.isPresent());
        Assert.assertTrue(psetIfNotExist.get());
        //删除
        cacheService.delete(key);
    }

    @Test
    public void testTryGetLock() {
        //尝试获取锁
        boolean tryGetLock = cacheService.tryGetLock(key, key, 1000L);
        Assert.assertTrue(tryGetLock);

        //再次获取
        tryGetLock = cacheService.tryGetLock(key, key, 1000L);
        Assert.assertFalse(tryGetLock);
    }

    @Test
    public void testTryReleaseLock() {
        //尝试获取锁
        final String requestId = UUID.randomUUID().toString();
        boolean tryGetLock = cacheService.tryGetLock(key, requestId, 1000L);
        Assert.assertTrue(tryGetLock);

        //尝试释放锁
        boolean tryReleaseLock = cacheService.tryReleaseLock(key, requestId);
        Assert.assertTrue(tryReleaseLock);

        //再次释放
        tryReleaseLock = cacheService.tryReleaseLock(key, requestId);
        Assert.assertFalse(tryReleaseLock);
    }

    @Test
    public void testDelete() {
        //设置键值对
        cacheService.setex(key, 10L, value);
        //断言存在且相等
        Optional<String> get = cacheService.get(key);
        Assert.assertTrue(get.isPresent());
        Assert.assertEquals(get.get(), value);

        //删除
        cacheService.delete(key);
        //断言不存在
        get = cacheService.get(key);
        Assert.assertFalse(get.isPresent());
    }
}
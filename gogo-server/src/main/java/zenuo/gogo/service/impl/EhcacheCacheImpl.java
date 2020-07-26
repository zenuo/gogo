package zenuo.gogo.service.impl;

import org.ehcache.Cache;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import zenuo.gogo.core.config.Constants;
import zenuo.gogo.service.ICacheService;

import java.time.Duration;
import java.util.Optional;

public final class EhcacheCacheImpl implements ICacheService {

    private static final Cache<String, byte[]> CACHE;

    static {
        final CacheConfiguration<String, byte[]> config = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        String.class, byte[].class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(4L, MemoryUnit.MB))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(Constants.SEARCH_RESPONSE_CACHE_TTL_IN_SECONDS)))
                .build();
        final String name = EhcacheCacheImpl.class.getName();
        CACHE = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(name, config)
                .build(true)
                .getCache(name, String.class, byte[].class);
    }

    @Override
    public void set(String key, byte[] value) {
        CACHE.put(key, value);
    }

    @Override
    public Optional<byte[]> get(String key) {
        return Optional.ofNullable(CACHE.get(key));
    }
}

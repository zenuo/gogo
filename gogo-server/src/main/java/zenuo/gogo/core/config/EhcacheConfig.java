package zenuo.gogo.core.config;

import org.ehcache.Cache;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import java.time.Duration;

public class EhcacheConfig {
    public static Cache<String, String> get() {
        CacheConfiguration<String, String> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(16L, MemoryUnit.MB))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(Constants.SEARCH_RESPONSE_CACHE_TTL_IN_SECONDS)))
                .build();
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(Constants.SEARCH_RESULT_CACHE_ALIAS, config)
                .build(true)
                .getCache(Constants.SEARCH_RESULT_CACHE_ALIAS, String.class, String.class);
    }
}

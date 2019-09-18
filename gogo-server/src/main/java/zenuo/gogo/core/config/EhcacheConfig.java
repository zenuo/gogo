package zenuo.gogo.core.config;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class EhcacheConfig {
    @Bean
    private CacheManager cacheManager() {
        CacheConfiguration<String, String> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(16L, MemoryUnit.MB))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(Constants.SEARCH_RESPONSE_CACHE_TTL_IN_SECONDS)))
                .build();
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(Constants.SEARCH_RESULT_CACHE_ALIAS, config)
                .build(true);
    }

    @Bean
    private Cache<String, String> cache(CacheManager cacheManager) {
        return cacheManager.getCache(Constants.SEARCH_RESULT_CACHE_ALIAS, String.class, String.class);
    }
}

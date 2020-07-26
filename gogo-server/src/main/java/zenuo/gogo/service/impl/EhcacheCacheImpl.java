package zenuo.gogo.service.impl;

import org.ehcache.Cache;
import zenuo.gogo.core.config.EhcacheConfig;
import zenuo.gogo.service.ICacheService;

import java.util.Optional;

public final class EhcacheCacheImpl implements ICacheService {

    private final Cache<String, String> cache = EhcacheConfig.get();

    @Override
    public void set(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}

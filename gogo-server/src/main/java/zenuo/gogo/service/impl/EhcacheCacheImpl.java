package zenuo.gogo.service.impl;

import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zenuo.gogo.service.ICacheService;

import java.util.Optional;

@Component
final class EhcacheCacheImpl implements ICacheService {

    @Autowired
    private Cache<String, String> cache;

    @Override
    public void set(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}

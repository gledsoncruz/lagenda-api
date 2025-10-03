package com.lasystems.lagenda.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cache-test")
public class CacheTestController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache instanceof CaffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        ((CaffeineCache) cache).getNativeCache();
                stats.put(name, nativeCache.stats());
            }
        });

        return stats;
    }
}
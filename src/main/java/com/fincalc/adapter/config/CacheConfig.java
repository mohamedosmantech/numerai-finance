package com.fincalc.adapter.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Objects;

/**
 * Cache configuration for rate data.
 * Caches external API responses to reduce calls and improve performance.
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("fredRates", "taxBrackets");
    }

    /**
     * Clear rate cache every hour to get fresh data.
     * FRED data is updated weekly for mortgage rates, daily for fed funds.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void evictRatesCache() {
        CacheManager manager = cacheManager();
        Objects.requireNonNull(manager.getCache("fredRates")).clear();
    }
}

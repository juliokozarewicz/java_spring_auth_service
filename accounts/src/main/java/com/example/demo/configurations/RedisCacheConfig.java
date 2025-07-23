package com.example.demo.configurations;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(

        RedisConnectionFactory redisConnectionFactory

    ) {

        // standard config
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(2))
            .disableCachingNullValues();

        // profile cache (6 months)
        RedisCacheConfiguration profileCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues();

        // map config
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("jwtCache", defaultCacheConfig);
        cacheConfigs.put("profileCache", profileCacheConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig) // fallback
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}

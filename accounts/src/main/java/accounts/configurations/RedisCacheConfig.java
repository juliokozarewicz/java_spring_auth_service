package accounts.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        // 1. Create and configure ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Enable polymorphic type handling for proper serialization/deserialization
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Register Java 8 Time module for proper date/time handling
        objectMapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps (use ISO-8601 format instead)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Create JSON serializer using the configured ObjectMapper
        RedisSerializationContext.SerializationPair<Object> serializationPair =
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(objectMapper)
            );

        // Default cache configuration (2 minutes TTL)
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(2)) // Set default expiration time
            .disableCachingNullValues()      // Don't cache null values
            .serializeValuesWith(serializationPair); // Use JSON serialization

        // Profile cache configuration (no specific TTL - will use default)
        RedisCacheConfiguration profileCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues()
            .serializeValuesWith(serializationPair);

        // Address cache configuration (no specific TTL - will use default)
        RedisCacheConfiguration addressCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues()
            .serializeValuesWith(serializationPair);

        // Refresh login verification cache configuration
        RedisCacheConfiguration ArrayLoginsCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues()
            .serializeValuesWith(serializationPair);

        // Refresh login verification cache configuration
        RedisCacheConfiguration refreshLoginCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofDays(15))
            .disableCachingNullValues()
            .serializeValuesWith(serializationPair);

        // Pin verification cache configuration
        RedisCacheConfiguration pinVerificationCacheConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .disableCachingNullValues()
            .serializeValuesWith(serializationPair);

        // Create cache configurations map for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("jwtCache", defaultCacheConfig);
        cacheConfigs.put("profileCache", profileCacheConfig);
        cacheConfigs.put("addressCache", addressCacheConfig);
        cacheConfigs.put("refreshLoginCache", refreshLoginCacheConfig);
        cacheConfigs.put("pinVerificationCache", pinVerificationCacheConfig);
        cacheConfigs.put("ArrayLoginsCache", ArrayLoginsCacheConfig);

        // Build and return the CacheManager instance
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig) // Default configuration
            .withInitialCacheConfigurations(cacheConfigs) // Custom cache configurations
            .build();

    }

}
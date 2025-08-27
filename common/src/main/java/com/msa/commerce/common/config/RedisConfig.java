package com.msa.commerce.common.config;

import com.msa.commerce.common.config.cache.CacheDefinition;
import com.msa.commerce.common.config.cache.CacheStrategy;
import com.msa.commerce.common.config.cache.DomainCacheRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {

    public static final String DEFAULT_CACHE = "default";
    public static final String PRODUCT_CACHE = "product";
    public static final String PRODUCT_VIEW_COUNT_CACHE = "product-view-count";
    public static final String USER_CACHE = "user";
    public static final String ORDER_CACHE = "order";
    public static final String PAYMENT_CACHE = "payment";

    @PostConstruct
    public void registerDefaultCaches() {
        DomainCacheRegistry.registerCaches(
            CacheDefinition.of(PRODUCT_CACHE, CacheStrategy.LONG_TERM, 
                "Product basic information - master data that changes infrequently"),
            CacheDefinition.of(PRODUCT_VIEW_COUNT_CACHE, CacheStrategy.SHORT_TERM, 
                "Product view count - real-time critical data"),
            
            CacheDefinition.of(USER_CACHE, CacheStrategy.MEDIUM_TERM, 
                "User profile information - session based data"),
            
            CacheDefinition.of(ORDER_CACHE, CacheStrategy.MEDIUM_TERM, 
                "Order information - transactional data with state changes"),
            
            CacheDefinition.of(PAYMENT_CACHE, CacheStrategy.SHORT_TERM, 
                "Payment information - rapidly changing status data"),
            
            CacheDefinition.of(DEFAULT_CACHE, CacheStrategy.DEFAULT, 
                "Default general purpose cache")
        );
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(CacheStrategy.DEFAULT.getTtl())
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = buildCacheConfigurations(defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    private Map<String, RedisCacheConfiguration> buildCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();
        
        DomainCacheRegistry.getAllCacheDefinitions().forEach((cacheName, cacheDefinition) -> {
            RedisCacheConfiguration config = defaultConfig.entryTtl(cacheDefinition.getTtl());
            configurations.put(cacheName, config);
        });
        
        return configurations;
    }

    public static class CacheNames {
        public static final String DEFAULT = DEFAULT_CACHE;
        public static final String PRODUCT = PRODUCT_CACHE;
        public static final String PRODUCT_VIEW_COUNT = PRODUCT_VIEW_COUNT_CACHE;
        public static final String USER = USER_CACHE;
        public static final String ORDER = ORDER_CACHE;
        public static final String PAYMENT = PAYMENT_CACHE;
        
        private CacheNames() {
        }
    }

    public static class CacheUtils {
        
        public static void registerCache(String cacheName, CacheStrategy strategy, String description) {
            DomainCacheRegistry.registerCache(
                CacheDefinition.of(cacheName, strategy, description)
            );
        }
        
        public static CacheDefinition getCacheInfo(String cacheName) {
            return DomainCacheRegistry.getCacheDefinition(cacheName);
        }
        
        public static Set<String> getAllCacheNames() {
            return DomainCacheRegistry.getCacheNames();
        }
        
        public static boolean isCacheRegistered(String cacheName) {
            return DomainCacheRegistry.isCacheRegistered(cacheName);
        }
        
        public static String generateCacheName(String domain, String type) {
            if (domain == null || domain.trim().isEmpty()) {
                throw new IllegalArgumentException("Domain cannot be null or empty");
            }
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("Type cannot be null or empty");
            }
            return domain.toLowerCase().trim() + "-" + type.toLowerCase().trim();
        }
        
        public static String generateHierarchicalCacheName(String... parts) {
            if (parts == null || parts.length == 0) {
                throw new IllegalArgumentException("At least one part is required");
            }
            return String.join(":", parts).toLowerCase();
        }
        
        private CacheUtils() {
        }
    }
}

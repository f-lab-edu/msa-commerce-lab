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

    // 기본 캐시 이름 상수 (하위 호환성 유지)
    public static final String DEFAULT_CACHE = "default";
    public static final String PRODUCT_CACHE = "product";
    public static final String PRODUCT_VIEW_COUNT_CACHE = "product-view-count";
    public static final String USER_CACHE = "user";
    public static final String ORDER_CACHE = "order";
    public static final String PAYMENT_CACHE = "payment";

    @PostConstruct
    public void registerDefaultCaches() {
        // 기본 캐시들 등록
        DomainCacheRegistry.registerCaches(
            // 상품 관련 캐시
            CacheDefinition.of(PRODUCT_CACHE, CacheStrategy.LONG_TERM, 
                "상품 기본 정보 - 자주 변경되지 않는 마스터 데이터"),
            CacheDefinition.of(PRODUCT_VIEW_COUNT_CACHE, CacheStrategy.SHORT_TERM, 
                "상품 조회수 - 실시간성이 중요한 데이터"),
            
            // 사용자 관련 캐시
            CacheDefinition.of(USER_CACHE, CacheStrategy.MEDIUM_TERM, 
                "사용자 프로필 정보 - 세션 기반 데이터"),
            
            // 주문 관련 캐시
            CacheDefinition.of(ORDER_CACHE, CacheStrategy.MEDIUM_TERM, 
                "주문 정보 - 상태 변경이 있는 트랜잭션 데이터"),
            
            // 결제 관련 캐시
            CacheDefinition.of(PAYMENT_CACHE, CacheStrategy.SHORT_TERM, 
                "결제 정보 - 상태가 빠르게 변경되는 데이터"),
            
            // 기본 캐시
            CacheDefinition.of(DEFAULT_CACHE, CacheStrategy.DEFAULT, 
                "기본 범용 캐시")
        );
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key serializer - String 사용
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value serializer - JSON 사용
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(CacheStrategy.DEFAULT.getTtl())
                .disableCachingNullValues(); // null 값 캐싱 비활성화

        // 등록된 캐시 정의를 기반으로 캐시 설정 구성
        Map<String, RedisCacheConfiguration> cacheConfigurations = buildCacheConfigurations(defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 트랜잭션 지원
                .build();
    }

    private Map<String, RedisCacheConfiguration> buildCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();
        
        // 레지스트리에서 등록된 모든 캐시 정의를 가져와서 설정
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
            // 유틸리티 클래스로 인스턴스화 방지
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
            // 유틸리티 클래스로 인스턴스화 방지
        }
    }
}
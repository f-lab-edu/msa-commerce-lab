package com.msa.commerce.common.config;

import com.msa.commerce.common.config.cache.CacheDefinition;
import com.msa.commerce.common.config.cache.CacheStrategy;
import com.msa.commerce.common.config.cache.DomainCacheRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Redis Configuration Tests")
class RedisConfigTest {

    @BeforeEach
    void setUp() {
        // Clear registry before each test to ensure clean state
        DomainCacheRegistry.clearAll();
    }

    @Test
    @DisplayName("Should define legacy cache names constants for backward compatibility")
    void shouldDefineLegacyCacheNamesConstants() {
        // Given & When & Then
        assertEquals("default", RedisConfig.DEFAULT_CACHE);
        assertEquals("product", RedisConfig.PRODUCT_CACHE);
        assertEquals("product-view-count", RedisConfig.PRODUCT_VIEW_COUNT_CACHE);
        assertEquals("user", RedisConfig.USER_CACHE);
        assertEquals("order", RedisConfig.ORDER_CACHE);
        assertEquals("payment", RedisConfig.PAYMENT_CACHE);
    }

    @Test
    @DisplayName("Should define CacheNames utility class constants")
    void shouldDefineCacheNamesUtilityClassConstants() {
        // Given & When & Then
        assertEquals("default", RedisConfig.CacheNames.DEFAULT);
        assertEquals("product", RedisConfig.CacheNames.PRODUCT);
        assertEquals("product-view-count", RedisConfig.CacheNames.PRODUCT_VIEW_COUNT);
        assertEquals("user", RedisConfig.CacheNames.USER);
        assertEquals("order", RedisConfig.CacheNames.ORDER);
        assertEquals("payment", RedisConfig.CacheNames.PAYMENT);
    }

    @Test
    @DisplayName("Should ensure cache name consistency")
    void shouldEnsureCacheNameConsistency() {
        // Given & When & Then
        assertEquals(RedisConfig.DEFAULT_CACHE, RedisConfig.CacheNames.DEFAULT);
        assertEquals(RedisConfig.PRODUCT_CACHE, RedisConfig.CacheNames.PRODUCT);
        assertEquals(RedisConfig.PRODUCT_VIEW_COUNT_CACHE, RedisConfig.CacheNames.PRODUCT_VIEW_COUNT);
        assertEquals(RedisConfig.USER_CACHE, RedisConfig.CacheNames.USER);
        assertEquals(RedisConfig.ORDER_CACHE, RedisConfig.CacheNames.ORDER);
        assertEquals(RedisConfig.PAYMENT_CACHE, RedisConfig.CacheNames.PAYMENT);
    }

    @Test
    @DisplayName("Should validate conditional property annotation presence")
    void shouldValidateConditionalPropertyAnnotationPresence() throws Exception {
        // Given
        RedisConfig config = new RedisConfig();
        
        // When - Check if the class has ConditionalOnProperty annotation
        boolean hasConditionalAnnotation = config.getClass().isAnnotationPresent(
            org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class);
        
        // Then
        assertTrue(hasConditionalAnnotation, "RedisConfig should have ConditionalOnProperty annotation");
        
        // Verify the property name (annotation.name() returns an array)
        org.springframework.boot.autoconfigure.condition.ConditionalOnProperty annotation = 
            config.getClass().getAnnotation(
                org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class);
        assertEquals("spring.data.redis.host", annotation.name()[0]);
    }

    @Test
    @DisplayName("Should register default caches during PostConstruct")
    void shouldRegisterDefaultCachesDuringPostConstruct() {
        // Given
        RedisConfig config = new RedisConfig();
        assertEquals(0, DomainCacheRegistry.size());

        // When
        config.registerDefaultCaches();

        // Then
        assertTrue(DomainCacheRegistry.size() > 0);
        assertTrue(DomainCacheRegistry.isCacheRegistered("product"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("product-view-count"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("user"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("order"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("payment"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("default"));
    }

    @Test
    @DisplayName("Should verify default cache strategies")
    void shouldVerifyDefaultCacheStrategies() {
        // Given
        RedisConfig config = new RedisConfig();
        config.registerDefaultCaches();

        // When & Then
        CacheDefinition productCache = DomainCacheRegistry.getCacheDefinition("product");
        assertEquals(CacheStrategy.LONG_TERM, productCache.getStrategy());

        CacheDefinition viewCountCache = DomainCacheRegistry.getCacheDefinition("product-view-count");
        assertEquals(CacheStrategy.SHORT_TERM, viewCountCache.getStrategy());

        CacheDefinition userCache = DomainCacheRegistry.getCacheDefinition("user");
        assertEquals(CacheStrategy.MEDIUM_TERM, userCache.getStrategy());

        CacheDefinition defaultCache = DomainCacheRegistry.getCacheDefinition("default");
        assertEquals(CacheStrategy.DEFAULT, defaultCache.getStrategy());
    }

    @Test
    @DisplayName("Should test CacheUtils helper methods")
    void shouldTestCacheUtilsHelperMethods() {
        // Given
        RedisConfig config = new RedisConfig();
        config.registerDefaultCaches();

        // When & Then - Test cache registration
        RedisConfig.CacheUtils.registerCache("test-cache", CacheStrategy.LONG_TERM, "Test cache");
        assertTrue(RedisConfig.CacheUtils.isCacheRegistered("test-cache"));
        assertNotNull(RedisConfig.CacheUtils.getCacheInfo("test-cache"));

        // Test cache name generation
        assertEquals("user-profile", RedisConfig.CacheUtils.generateCacheName("User", "Profile"));
        assertEquals("user:profile:basic", RedisConfig.CacheUtils.generateHierarchicalCacheName("User", "Profile", "Basic"));

        // Test all cache names retrieval
        assertTrue(RedisConfig.CacheUtils.getAllCacheNames().size() > 0);
        assertTrue(RedisConfig.CacheUtils.getAllCacheNames().contains("product"));
    }

    @Test
    @DisplayName("Should test cache name generation edge cases")
    void shouldTestCacheNameGenerationEdgeCases() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () ->
                RedisConfig.CacheUtils.generateCacheName(null, "type"));

        assertThrows(IllegalArgumentException.class, () ->
                RedisConfig.CacheUtils.generateCacheName("", "type"));

        assertThrows(IllegalArgumentException.class, () ->
                RedisConfig.CacheUtils.generateCacheName("domain", null));

        assertThrows(IllegalArgumentException.class, () ->
                RedisConfig.CacheUtils.generateCacheName("domain", ""));

        assertThrows(IllegalArgumentException.class, () ->
                RedisConfig.CacheUtils.generateHierarchicalCacheName());

        assertThrows(IllegalArgumentException.class, () ->
                RedisConfig.CacheUtils.generateHierarchicalCacheName(new String[]{}));
    }
}
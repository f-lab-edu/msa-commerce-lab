package com.msa.commerce.common.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;

@DisplayName("Redis Configuration Tests")
class RedisConfigTest {

    @Test
    @DisplayName("Should validate configuration annotations")
    void shouldValidateConfigurationAnnotations() {
        // Given
        Class<RedisConfig> configClass = RedisConfig.class;

        // Then - Verify @Configuration annotation is present
        assertTrue(configClass.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class),
            "RedisConfig should have @Configuration annotation");

        // Verify @EnableCaching annotation is present
        assertTrue(configClass.isAnnotationPresent(EnableCaching.class),
            "RedisConfig should have @EnableCaching annotation");

        // Verify @ConditionalOnProperty annotation
        assertTrue(configClass.isAnnotationPresent(ConditionalOnProperty.class),
            "RedisConfig should have @ConditionalOnProperty annotation");

        ConditionalOnProperty conditionalAnnotation =
            configClass.getAnnotation(ConditionalOnProperty.class);
        assertEquals("spring.data.redis.host", conditionalAnnotation.name()[0],
            "Should be conditional on spring.data.redis.host property");
    }

    @Test
    @DisplayName("Should define RedisTemplate bean method")
    void shouldDefineRedisTemplateBeanMethod() throws NoSuchMethodException {
        // Given
        Class<RedisConfig> configClass = RedisConfig.class;

        // Then - Verify redisTemplate method exists
        assertDoesNotThrow(() ->
                configClass.getDeclaredMethod("redisTemplate",
                    org.springframework.data.redis.connection.RedisConnectionFactory.class),
            "RedisConfig should have redisTemplate method");

        // Verify it has @Bean annotation
        var method = configClass.getDeclaredMethod("redisTemplate",
            org.springframework.data.redis.connection.RedisConnectionFactory.class);
        assertTrue(method.isAnnotationPresent(
                org.springframework.context.annotation.Bean.class),
            "redisTemplate method should have @Bean annotation");
    }

    @Test
    @DisplayName("Should define CacheManager bean method")
    void shouldDefineCacheManagerBeanMethod() throws NoSuchMethodException {
        // Given
        Class<RedisConfig> configClass = RedisConfig.class;

        // Then - Verify cacheManager method exists
        assertDoesNotThrow(() ->
                configClass.getDeclaredMethod("cacheManager",
                    org.springframework.data.redis.connection.RedisConnectionFactory.class),
            "RedisConfig should have cacheManager method");

        // Verify it has @Bean annotation
        var method = configClass.getDeclaredMethod("cacheManager",
            org.springframework.data.redis.connection.RedisConnectionFactory.class);
        assertTrue(method.isAnnotationPresent(
                org.springframework.context.annotation.Bean.class),
            "cacheManager method should have @Bean annotation");
    }

}

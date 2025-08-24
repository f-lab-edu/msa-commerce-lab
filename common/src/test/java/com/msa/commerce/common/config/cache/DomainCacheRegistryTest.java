package com.msa.commerce.common.config.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Cache Registry Tests")
class DomainCacheRegistryTest {

    @BeforeEach
    void setUp() {
        // Clear registry before each test
        DomainCacheRegistry.clearAll();
    }

    @Test
    @DisplayName("Should register cache definition")
    void shouldRegisterCacheDefinition() {
        // Given
        CacheDefinition definition = CacheDefinition.of("test-cache", CacheStrategy.LONG_TERM);

        // When
        DomainCacheRegistry.registerCache(definition);

        // Then
        assertTrue(DomainCacheRegistry.isCacheRegistered("test-cache"));
        assertEquals(definition, DomainCacheRegistry.getCacheDefinition("test-cache"));
        assertEquals(1, DomainCacheRegistry.size());
    }

    @Test
    @DisplayName("Should register multiple cache definitions")
    void shouldRegisterMultipleCacheDefinitions() {
        // Given
        CacheDefinition cache1 = CacheDefinition.of("cache1", CacheStrategy.SHORT_TERM);
        CacheDefinition cache2 = CacheDefinition.of("cache2", CacheStrategy.MEDIUM_TERM);
        CacheDefinition cache3 = CacheDefinition.of("cache3", CacheStrategy.LONG_TERM);

        // When
        DomainCacheRegistry.registerCaches(cache1, cache2, cache3);

        // Then
        assertEquals(3, DomainCacheRegistry.size());
        assertTrue(DomainCacheRegistry.isCacheRegistered("cache1"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("cache2"));
        assertTrue(DomainCacheRegistry.isCacheRegistered("cache3"));
    }

    @Test
    @DisplayName("Should return null for non-existent cache")
    void shouldReturnNullForNonExistentCache() {
        // Given & When
        CacheDefinition definition = DomainCacheRegistry.getCacheDefinition("non-existent");

        // Then
        assertNull(definition);
        assertFalse(DomainCacheRegistry.isCacheRegistered("non-existent"));
    }

    @Test
    @DisplayName("Should return all cache definitions")
    void shouldReturnAllCacheDefinitions() {
        // Given
        CacheDefinition cache1 = CacheDefinition.of("cache1", CacheStrategy.SHORT_TERM);
        CacheDefinition cache2 = CacheDefinition.of("cache2", CacheStrategy.MEDIUM_TERM);
        DomainCacheRegistry.registerCaches(cache1, cache2);

        // When
        var allDefinitions = DomainCacheRegistry.getAllCacheDefinitions();

        // Then
        assertEquals(2, allDefinitions.size());
        assertTrue(allDefinitions.containsKey("cache1"));
        assertTrue(allDefinitions.containsKey("cache2"));
        assertEquals(cache1, allDefinitions.get("cache1"));
        assertEquals(cache2, allDefinitions.get("cache2"));
    }

    @Test
    @DisplayName("Should return all cache names")
    void shouldReturnAllCacheNames() {
        // Given
        DomainCacheRegistry.registerCaches(
                CacheDefinition.of("user-cache", CacheStrategy.MEDIUM_TERM),
                CacheDefinition.of("product-cache", CacheStrategy.LONG_TERM)
        );

        // When
        var cacheNames = DomainCacheRegistry.getCacheNames();

        // Then
        assertEquals(2, cacheNames.size());
        assertTrue(cacheNames.contains("user-cache"));
        assertTrue(cacheNames.contains("product-cache"));
    }

    @Test
    @DisplayName("Should unregister cache")
    void shouldUnregisterCache() {
        // Given
        CacheDefinition definition = CacheDefinition.of("temp-cache", CacheStrategy.SHORT_TERM);
        DomainCacheRegistry.registerCache(definition);
        assertTrue(DomainCacheRegistry.isCacheRegistered("temp-cache"));

        // When
        DomainCacheRegistry.unregisterCache("temp-cache");

        // Then
        assertFalse(DomainCacheRegistry.isCacheRegistered("temp-cache"));
        assertNull(DomainCacheRegistry.getCacheDefinition("temp-cache"));
        assertEquals(0, DomainCacheRegistry.size());
    }

    @Test
    @DisplayName("Should clear all caches")
    void shouldClearAllCaches() {
        // Given
        DomainCacheRegistry.registerCaches(
                CacheDefinition.of("cache1", CacheStrategy.SHORT_TERM),
                CacheDefinition.of("cache2", CacheStrategy.MEDIUM_TERM),
                CacheDefinition.of("cache3", CacheStrategy.LONG_TERM)
        );
        assertEquals(3, DomainCacheRegistry.size());

        // When
        DomainCacheRegistry.clearAll();

        // Then
        assertEquals(0, DomainCacheRegistry.size());
        assertTrue(DomainCacheRegistry.getCacheNames().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when registering null cache definition")
    void shouldThrowExceptionWhenRegisteringNullCacheDefinition() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () ->
                DomainCacheRegistry.registerCache(null)
        );
    }

    @Test
    @DisplayName("Should overwrite existing cache definition with same name")
    void shouldOverwriteExistingCacheDefinitionWithSameName() {
        // Given
        CacheDefinition original = CacheDefinition.of("same-cache", CacheStrategy.SHORT_TERM, "Original");
        CacheDefinition updated = CacheDefinition.of("same-cache", CacheStrategy.LONG_TERM, "Updated");

        DomainCacheRegistry.registerCache(original);
        assertEquals(original, DomainCacheRegistry.getCacheDefinition("same-cache"));

        // When
        DomainCacheRegistry.registerCache(updated);

        // Then
        assertEquals(updated, DomainCacheRegistry.getCacheDefinition("same-cache"));
        assertEquals(1, DomainCacheRegistry.size());
        assertEquals("Updated", DomainCacheRegistry.getCacheDefinition("same-cache").getDescription());
    }
}
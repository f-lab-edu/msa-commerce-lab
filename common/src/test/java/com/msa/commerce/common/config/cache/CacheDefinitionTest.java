package com.msa.commerce.common.config.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cache Definition Tests")
class CacheDefinitionTest {

    @Test
    @DisplayName("Should create cache definition with builder")
    void shouldCreateCacheDefinitionWithBuilder() {
        // Given & When
        CacheDefinition definition = CacheDefinition.builder()
                .name("test-cache")
                .strategy(CacheStrategy.LONG_TERM)
                .description("Test cache")
                .build();

        // Then
        assertEquals("test-cache", definition.getName());
        assertEquals(CacheStrategy.LONG_TERM.getTtl(), definition.getTtl());
        assertEquals(CacheStrategy.LONG_TERM, definition.getStrategy());
        assertEquals("Test cache", definition.getDescription());
    }

    @Test
    @DisplayName("Should create cache definition with factory method")
    void shouldCreateCacheDefinitionWithFactoryMethod() {
        // Given & When
        CacheDefinition definition = CacheDefinition.of("user-cache", CacheStrategy.MEDIUM_TERM, "User profile cache");

        // Then
        assertEquals("user-cache", definition.getName());
        assertEquals(CacheStrategy.MEDIUM_TERM.getTtl(), definition.getTtl());
        assertEquals(CacheStrategy.MEDIUM_TERM, definition.getStrategy());
        assertEquals("User profile cache", definition.getDescription());
    }

    @Test
    @DisplayName("Should create cache definition without description")
    void shouldCreateCacheDefinitionWithoutDescription() {
        // Given & When
        CacheDefinition definition = CacheDefinition.of("simple-cache", CacheStrategy.SHORT_TERM);

        // Then
        assertEquals("simple-cache", definition.getName());
        assertEquals(CacheStrategy.SHORT_TERM.getTtl(), definition.getTtl());
        assertEquals(CacheStrategy.SHORT_TERM, definition.getStrategy());
        assertNull(definition.getDescription());
    }

    @Test
    @DisplayName("Should use custom TTL when specified")
    void shouldUseCustomTtlWhenSpecified() {
        // Given
        Duration customTtl = Duration.ofMinutes(45);

        // When
        CacheDefinition definition = CacheDefinition.builder()
                .name("custom-cache")
                .strategy(CacheStrategy.LONG_TERM)
                .ttl(customTtl)
                .build();

        // Then
        assertEquals("custom-cache", definition.getName());
        assertEquals(customTtl, definition.getTtl());
        assertEquals(CacheStrategy.LONG_TERM, definition.getStrategy());
    }

    @Test
    @DisplayName("Should use strategy TTL when custom TTL is not specified")
    void shouldUseStrategyTtlWhenCustomTtlNotSpecified() {
        // Given & When
        CacheDefinition definition = CacheDefinition.builder()
                .name("strategy-cache")
                .strategy(CacheStrategy.VERY_LONG_TERM)
                .build();

        // Then
        assertEquals("strategy-cache", definition.getName());
        assertEquals(CacheStrategy.VERY_LONG_TERM.getTtl(), definition.getTtl());
        assertEquals(CacheStrategy.VERY_LONG_TERM, definition.getStrategy());
    }

    @Test
    @DisplayName("Should use default strategy when not specified")
    void shouldUseDefaultStrategyWhenNotSpecified() {
        // Given & When
        CacheDefinition definition = CacheDefinition.builder()
                .name("default-cache")
                .build();

        // Then
        assertEquals("default-cache", definition.getName());
        assertEquals(CacheStrategy.DEFAULT.getTtl(), definition.getTtl());
        assertEquals(CacheStrategy.DEFAULT, definition.getStrategy());
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () ->
                CacheDefinition.builder().build()
        );
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void shouldThrowExceptionWhenNameIsEmpty() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () ->
                CacheDefinition.builder().name("").build()
        );
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () ->
                CacheDefinition.builder().name("   ").build()
        );
    }

    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        CacheDefinition definition = CacheDefinition.of("test-cache", CacheStrategy.LONG_TERM, "Test description");

        // When
        String toString = definition.toString();

        // Then
        assertTrue(toString.contains("test-cache"));
        assertTrue(toString.contains("LONG_TERM"));
        assertTrue(toString.contains("Test description"));
    }
}

package com.msa.commerce.common.config.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cache Strategy Tests")
class CacheStrategyTest {

    @Test
    @DisplayName("Should define correct TTL values for each strategy")
    void shouldDefineCorrectTtlValues() {
        // Given & When & Then
        assertEquals(Duration.ofMinutes(5), CacheStrategy.SHORT_TERM.getTtl());
        assertEquals(Duration.ofMinutes(15), CacheStrategy.MEDIUM_TERM.getTtl());
        assertEquals(Duration.ofHours(1), CacheStrategy.LONG_TERM.getTtl());
        assertEquals(Duration.ofHours(6), CacheStrategy.VERY_LONG_TERM.getTtl());
        assertEquals(Duration.ofDays(1), CacheStrategy.DAILY.getTtl());
        assertEquals(Duration.ofMinutes(30), CacheStrategy.DEFAULT.getTtl());
    }

    @Test
    @DisplayName("Should convert TTL to minutes correctly")
    void shouldConvertTtlToMinutesCorrectly() {
        // Given & When & Then
        assertEquals(5, CacheStrategy.SHORT_TERM.getTtlInMinutes());
        assertEquals(15, CacheStrategy.MEDIUM_TERM.getTtlInMinutes());
        assertEquals(60, CacheStrategy.LONG_TERM.getTtlInMinutes());
        assertEquals(360, CacheStrategy.VERY_LONG_TERM.getTtlInMinutes());
        assertEquals(1440, CacheStrategy.DAILY.getTtlInMinutes());
        assertEquals(30, CacheStrategy.DEFAULT.getTtlInMinutes());
    }

    @Test
    @DisplayName("Should convert TTL to seconds correctly")
    void shouldConvertTtlToSecondsCorrectly() {
        // Given & When & Then
        assertEquals(300, CacheStrategy.SHORT_TERM.getTtlInSeconds());
        assertEquals(900, CacheStrategy.MEDIUM_TERM.getTtlInSeconds());
        assertEquals(3600, CacheStrategy.LONG_TERM.getTtlInSeconds());
        assertEquals(21600, CacheStrategy.VERY_LONG_TERM.getTtlInSeconds());
        assertEquals(86400, CacheStrategy.DAILY.getTtlInSeconds());
        assertEquals(1800, CacheStrategy.DEFAULT.getTtlInSeconds());
    }

    @Test
    @DisplayName("Should have ascending TTL values")
    void shouldHaveAscendingTtlValues() {
        // Given & When & Then
        assertTrue(CacheStrategy.SHORT_TERM.getTtlInSeconds() < CacheStrategy.MEDIUM_TERM.getTtlInSeconds());
        assertTrue(CacheStrategy.MEDIUM_TERM.getTtlInSeconds() < CacheStrategy.DEFAULT.getTtlInSeconds());
        assertTrue(CacheStrategy.DEFAULT.getTtlInSeconds() < CacheStrategy.LONG_TERM.getTtlInSeconds());
        assertTrue(CacheStrategy.LONG_TERM.getTtlInSeconds() < CacheStrategy.VERY_LONG_TERM.getTtlInSeconds());
        assertTrue(CacheStrategy.VERY_LONG_TERM.getTtlInSeconds() < CacheStrategy.DAILY.getTtlInSeconds());
    }
}

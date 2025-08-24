package com.msa.commerce.common.config.cache;

import java.time.Duration;

public class CacheDefinition {
    
    private final String name;
    private final Duration ttl;
    private final String description;
    private final CacheStrategy strategy;
    
    private CacheDefinition(Builder builder) {
        this.name = builder.name;
        this.ttl = builder.ttl;
        this.description = builder.description;
        this.strategy = builder.strategy;
    }
    
    public String getName() {
        return name;
    }
    
    public Duration getTtl() {
        return ttl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public CacheStrategy getStrategy() {
        return strategy;
    }
    
    public static class Builder {
        private String name;
        private Duration ttl;
        private String description;
        private CacheStrategy strategy = CacheStrategy.DEFAULT;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder strategy(CacheStrategy strategy) {
            this.strategy = strategy;
            this.ttl = strategy.getTtl();
            return this;
        }
        
        public Builder ttl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public CacheDefinition build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Cache name is required");
            }
            if (ttl == null) {
                ttl = strategy.getTtl();
            }
            return new CacheDefinition(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static CacheDefinition of(String name, CacheStrategy strategy, String description) {
        return builder()
                .name(name)
                .strategy(strategy)
                .description(description)
                .build();
    }
    
    public static CacheDefinition of(String name, CacheStrategy strategy) {
        return of(name, strategy, null);
    }
    
    @Override
    public String toString() {
        return String.format("CacheDefinition{name='%s', ttl=%s, strategy=%s, description='%s'}", 
                name, ttl, strategy, description);
    }
}
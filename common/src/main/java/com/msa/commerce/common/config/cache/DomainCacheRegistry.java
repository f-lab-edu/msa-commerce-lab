package com.msa.commerce.common.config.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DomainCacheRegistry {
    
    private static final Map<String, CacheDefinition> cacheDefinitions = new HashMap<>();
    
    public static void registerCache(CacheDefinition cacheDefinition) {
        if (cacheDefinition == null) {
            throw new IllegalArgumentException("Cache definition cannot be null");
        }
        cacheDefinitions.put(cacheDefinition.getName(), cacheDefinition);
    }
    
    public static void registerCaches(CacheDefinition... cacheDefinitions) {
        for (CacheDefinition definition : cacheDefinitions) {
            registerCache(definition);
        }
    }
    
    public static CacheDefinition getCacheDefinition(String cacheName) {
        return cacheDefinitions.get(cacheName);
    }
    
    public static Map<String, CacheDefinition> getAllCacheDefinitions() {
        return new HashMap<>(cacheDefinitions);
    }
    
    public static Set<String> getCacheNames() {
        return cacheDefinitions.keySet();
    }
    
    public static boolean isCacheRegistered(String cacheName) {
        return cacheDefinitions.containsKey(cacheName);
    }
    
    public static void unregisterCache(String cacheName) {
        cacheDefinitions.remove(cacheName);
    }
    
    public static void clearAll() {
        cacheDefinitions.clear();
    }
    
    public static int size() {
        return cacheDefinitions.size();
    }
}
package com.msa.commerce.common.config.cache;

public class DomainCacheConfigurations {

    public static class ProductCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                CacheDefinition.of("product", CacheStrategy.LONG_TERM, 
                    "Basic product information - master data"),
                    
                CacheDefinition.of("product-view-count", CacheStrategy.SHORT_TERM,
                    "Product view count - real-time data"),
                    
                CacheDefinition.of("product-category", CacheStrategy.VERY_LONG_TERM,
                    "Product category - rarely changed master data"),
                    
                CacheDefinition.of("product-review-summary", CacheStrategy.MEDIUM_TERM,
                    "Product review summary - updated when reviews change"),
                    
                CacheDefinition.of("product-stock-status", CacheStrategy.SHORT_TERM,
                    "Product stock status - real-time inventory data"),
                    
                CacheDefinition.of("product:popular:by-brand", CacheStrategy.LONG_TERM,
                    "Popular products by brand - periodically updated aggregated data")
            );
        }
    }

    public static class UserCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                CacheDefinition.of("user", CacheStrategy.MEDIUM_TERM,
                    "User profile information - session based data"),
                    
                CacheDefinition.of("user-permissions", CacheStrategy.LONG_TERM,
                    "User permissions - infrequently changed data"),
                    
                CacheDefinition.of("user-session", CacheStrategy.MEDIUM_TERM,
                    "User session information - temporary session data"),
                    
                CacheDefinition.of("user-preferences", CacheStrategy.LONG_TERM,
                    "User preferences - personal settings that change infrequently"),
                    
                CacheDefinition.of("user:cart", CacheStrategy.MEDIUM_TERM,
                    "User cart - temporary session data"),
                    
                CacheDefinition.of("user:activity:summary", CacheStrategy.SHORT_TERM,
                    "User activity summary - real-time activity data")
            );
        }
    }

    public static class OrderCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                CacheDefinition.of("order", CacheStrategy.MEDIUM_TERM,
                    "Order information - transactional data with state changes"),
                    
                CacheDefinition.of("order-status", CacheStrategy.SHORT_TERM,
                    "Order status - rapidly changing status data"),
                    
                CacheDefinition.of("order-delivery", CacheStrategy.SHORT_TERM,
                    "Order delivery information - real-time delivery status data"),
                    
                CacheDefinition.of("order:stats:daily", CacheStrategy.DAILY,
                    "Daily order statistics - aggregated data refreshed daily"),
                    
                CacheDefinition.of("order:recent:by-user", CacheStrategy.MEDIUM_TERM,
                    "Recent orders by user - user session based data")
            );
        }
    }

    public static class PaymentCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                CacheDefinition.of("payment", CacheStrategy.SHORT_TERM,
                    "Payment information - rapidly changing status data"),
                    
                CacheDefinition.of("payment-status", CacheStrategy.SHORT_TERM,
                    "Payment status - real-time payment status data"),
                    
                CacheDefinition.of("payment-methods", CacheStrategy.VERY_LONG_TERM,
                    "Payment methods - rarely changed configuration data"),
                    
                CacheDefinition.of("payment:gateway:config", CacheStrategy.LONG_TERM,
                    "Payment gateway configuration - infrequently changed data"),
                    
                CacheDefinition.of("payment:failure:stats", CacheStrategy.MEDIUM_TERM,
                    "Payment failure statistics - monitoring aggregated data")
            );
        }
    }

    public static class SystemCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                CacheDefinition.of("system-config", CacheStrategy.VERY_LONG_TERM,
                    "System configuration - rarely changed system settings"),
                    
                CacheDefinition.of("country-codes", CacheStrategy.DAILY,
                    "Country codes - static master data"),
                    
                CacheDefinition.of("exchange-rates", CacheStrategy.MEDIUM_TERM,
                    "Exchange rates - periodically updated external data"),
                    
                CacheDefinition.of("api-rate-limits", CacheStrategy.MEDIUM_TERM,
                    "API rate limits - per-user API call limit data"),
                    
                CacheDefinition.of("system:health", CacheStrategy.SHORT_TERM,
                    "System health status - real-time monitoring data")
            );
        }
    }

    public static void registerAllDomainCaches() {
        ProductCaches.register();
        UserCaches.register();
        OrderCaches.register();
        PaymentCaches.register();
        SystemCaches.register();
    }

    public static class CacheNameHelpers {
        
        public static String userSpecific(String cacheType, Long userId) {
            return String.format("user:%s:%d", cacheType, userId);
        }
        
        public static String productSpecific(String cacheType, Long productId) {
            return String.format("product:%s:%d", cacheType, productId);
        }
        
        public static String orderSpecific(String cacheType, Long orderId) {
            return String.format("order:%s:%d", cacheType, orderId);
        }
        
        public static String dateSpecific(String cacheType, String date) {
            return String.format("%s:date:%s", cacheType, date);
        }
        
        public static String regionSpecific(String cacheType, String region) {
            return String.format("%s:region:%s", cacheType, region);
        }
        
        private CacheNameHelpers() {
        }
    }
}

package com.msa.commerce.common.config.cache;

public class DomainCacheConfigurations {

    public static class ProductCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                // 기본 상품 정보 - 마스터 데이터
                CacheDefinition.of("product", CacheStrategy.LONG_TERM, 
                    "상품 기본 정보 - 자주 변경되지 않는 마스터 데이터"),
                    
                // 상품 조회수 - 실시간 데이터
                CacheDefinition.of("product-view-count", CacheStrategy.SHORT_TERM,
                    "상품 조회수 - 실시간성이 중요한 데이터"),
                    
                // 상품 카테고리 - 거의 변경되지 않는 데이터
                CacheDefinition.of("product-category", CacheStrategy.VERY_LONG_TERM,
                    "상품 카테고리 - 거의 변경되지 않는 마스터 데이터"),
                    
                // 상품 리뷰 요약 - 중간 빈도 변경
                CacheDefinition.of("product-review-summary", CacheStrategy.MEDIUM_TERM,
                    "상품 리뷰 요약 - 리뷰 추가/수정시 변경되는 데이터"),
                    
                // 상품 재고 상태 - 실시간
                CacheDefinition.of("product-stock-status", CacheStrategy.SHORT_TERM,
                    "상품 재고 상태 - 실시간 재고 변동 데이터"),
                    
                // 브랜드별 인기 상품 - 집계 데이터
                CacheDefinition.of("product:popular:by-brand", CacheStrategy.LONG_TERM,
                    "브랜드별 인기 상품 - 일정 주기로 업데이트되는 집계 데이터")
            );
        }
    }

    public static class UserCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                // 기본 사용자 프로필
                CacheDefinition.of("user", CacheStrategy.MEDIUM_TERM,
                    "사용자 프로필 정보 - 세션 기반 데이터"),
                    
                // 사용자 권한 정보
                CacheDefinition.of("user-permissions", CacheStrategy.LONG_TERM,
                    "사용자 권한 정보 - 권한 변경이 적은 데이터"),
                    
                // 사용자 세션
                CacheDefinition.of("user-session", CacheStrategy.MEDIUM_TERM,
                    "사용자 세션 정보 - 세션 기반 임시 데이터"),
                    
                // 사용자 설정
                CacheDefinition.of("user-preferences", CacheStrategy.LONG_TERM,
                    "사용자 설정 정보 - 자주 변경되지 않는 개인 설정"),
                    
                // 사용자별 장바구니
                CacheDefinition.of("user:cart", CacheStrategy.MEDIUM_TERM,
                    "사용자 장바구니 - 세션 기반 임시 데이터"),
                    
                // 사용자 활동 로그 요약
                CacheDefinition.of("user:activity:summary", CacheStrategy.SHORT_TERM,
                    "사용자 활동 요약 - 실시간 활동 데이터")
            );
        }
    }

    public static class OrderCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                // 기본 주문 정보
                CacheDefinition.of("order", CacheStrategy.MEDIUM_TERM,
                    "주문 정보 - 상태 변경이 있는 트랜잭션 데이터"),
                    
                // 주문 상태
                CacheDefinition.of("order-status", CacheStrategy.SHORT_TERM,
                    "주문 상태 - 빠르게 변경되는 상태 데이터"),
                    
                // 주문 배송 정보
                CacheDefinition.of("order-delivery", CacheStrategy.SHORT_TERM,
                    "주문 배송 정보 - 실시간 배송 상태 데이터"),
                    
                // 일별 주문 통계
                CacheDefinition.of("order:stats:daily", CacheStrategy.DAILY,
                    "일별 주문 통계 - 일 단위로 갱신되는 집계 데이터"),
                    
                // 사용자별 최근 주문
                CacheDefinition.of("order:recent:by-user", CacheStrategy.MEDIUM_TERM,
                    "사용자별 최근 주문 - 사용자 세션 기반 데이터")
            );
        }
    }

    public static class PaymentCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                // 기본 결제 정보
                CacheDefinition.of("payment", CacheStrategy.SHORT_TERM,
                    "결제 정보 - 상태가 빠르게 변경되는 데이터"),
                    
                // 결제 상태
                CacheDefinition.of("payment-status", CacheStrategy.SHORT_TERM,
                    "결제 상태 - 실시간 결제 상태 데이터"),
                    
                // 결제 방법 정보
                CacheDefinition.of("payment-methods", CacheStrategy.VERY_LONG_TERM,
                    "결제 방법 정보 - 거의 변경되지 않는 설정 데이터"),
                    
                // 결제 게이트웨이 설정
                CacheDefinition.of("payment:gateway:config", CacheStrategy.LONG_TERM,
                    "결제 게이트웨이 설정 - 설정 변경이 적은 데이터"),
                    
                // 결제 실패 통계
                CacheDefinition.of("payment:failure:stats", CacheStrategy.MEDIUM_TERM,
                    "결제 실패 통계 - 모니터링용 집계 데이터")
            );
        }
    }

    public static class SystemCaches {
        
        public static void register() {
            DomainCacheRegistry.registerCaches(
                // 시스템 설정
                CacheDefinition.of("system-config", CacheStrategy.VERY_LONG_TERM,
                    "시스템 설정 - 거의 변경되지 않는 시스템 설정"),
                    
                // 국가/지역 코드
                CacheDefinition.of("country-codes", CacheStrategy.DAILY,
                    "국가/지역 코드 - 정적 마스터 데이터"),
                    
                // 통화 환율
                CacheDefinition.of("exchange-rates", CacheStrategy.MEDIUM_TERM,
                    "통화 환율 - 주기적으로 업데이트되는 외부 데이터"),
                    
                // API 제한 정보
                CacheDefinition.of("api-rate-limits", CacheStrategy.MEDIUM_TERM,
                    "API 제한 정보 - 사용자별 API 호출 제한 데이터"),
                    
                // 시스템 건강 상태
                CacheDefinition.of("system:health", CacheStrategy.SHORT_TERM,
                    "시스템 건강 상태 - 실시간 모니터링 데이터")
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
            // 유틸리티 클래스로 인스턴스화 방지
        }
    }
}
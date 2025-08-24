# Generic Redis Cache Framework

범용적으로 사용할 수 있는 Redis 캐시 프레임워크입니다. 각 도메인과 마이크로서비스에서 유연하게 캐시 전략을 정의하고 사용할 수 있습니다.

## 주요 특징

- 🎯 **전략 기반 캐시**: 데이터 특성에 맞는 다양한 캐시 전략 제공
- 🔄 **동적 등록**: 런타임에 캐시 정의를 동적으로 등록 가능
- 🏗️ **빌더 패턴**: 직관적이고 유연한 캐시 정의 생성
- 📝 **이름 생성 유틸리티**: 일관된 캐시 이름 생성 지원
- 🔙 **하위 호환성**: 기존 코드와 완전 호환

## 캐시 전략

| 전략 | TTL | 용도 | 예시 |
|------|-----|------|------|
| `SHORT_TERM` | 5분 | 실시간성 중요 데이터 | 재고량, 조회수, 세션 |
| `MEDIUM_TERM` | 15분 | 적당히 변경되는 데이터 | 사용자 프로필, 장바구니 |
| `LONG_TERM` | 1시간 | 거의 변경되지 않는 데이터 | 상품 정보, 카테고리 |
| `VERY_LONG_TERM` | 6시간 | 정적 마스터 데이터 | 국가 코드, 시스템 설정 |
| `DAILY` | 24시간 | 일별 집계 데이터 | 일일 통계, 이미지 메타데이터 |
| `DEFAULT` | 30분 | 일반적인 데이터 | 명확하지 않은 데이터 |

## 기본 사용법

### 1. 단순한 캐시 어노테이션 사용

```java
@Service
public class ProductService {
    
    // 기존 방식과 동일하게 사용 (하위 호환성)
    @Cacheable(value = RedisConfig.PRODUCT_CACHE, key = "#productId")
    public Product getProduct(Long productId) {
        return productRepository.findById(productId);
    }
    
    // 또는 CacheNames 유틸리티 클래스 사용
    @Cacheable(value = RedisConfig.CacheNames.PRODUCT, key = "#productId")
    public Product getProductInfo(Long productId) {
        return productRepository.findById(productId);
    }
}
```

### 2. 도메인별 추가 캐시 등록

```java
@Configuration
public class ProductDomainCacheConfig {
    
    @PostConstruct
    public void registerProductCaches() {
        // 간단한 등록
        RedisConfig.CacheUtils.registerCache(
            "product-reviews", 
            CacheStrategy.MEDIUM_TERM, 
            "상품 리뷰 데이터"
        );
        
        // 또는 여러 캐시를 한번에 등록
        DomainCacheRegistry.registerCaches(
            CacheDefinition.of("product-category", CacheStrategy.VERY_LONG_TERM, "상품 카테고리"),
            CacheDefinition.of("product-stock", CacheStrategy.SHORT_TERM, "실시간 재고")
        );
    }
}
```

### 3. 동적 캐시 이름 생성

```java
@Service
public class UserService {
    
    // 사용자별 캐시
    @Cacheable(value = "user-profile", key = "#userId")
    public UserProfile getUserProfile(Long userId) {
        String cacheName = RedisConfig.CacheUtils.generateCacheName("user", "profile");
        // cacheName = "user-profile"
        return userRepository.findById(userId);
    }
    
    // 계층적 캐시 이름
    @Cacheable(value = "user:settings:notification", key = "#userId")
    public NotificationSettings getNotificationSettings(Long userId) {
        String cacheName = RedisConfig.CacheUtils.generateHierarchicalCacheName(
            "user", "settings", "notification"
        );
        // cacheName = "user:settings:notification"
        return settingsRepository.findByUserId(userId);
    }
}
```

### 4. 커스텀 캐시 전략 정의

```java
@Configuration
public class CustomCacheConfig {
    
    @PostConstruct
    public void registerCustomCaches() {
        // 빌더 패턴으로 세밀한 제어
        CacheDefinition customCache = CacheDefinition.builder()
                .name("special-cache")
                .strategy(CacheStrategy.LONG_TERM)
                .ttl(Duration.ofMinutes(45)) // 전략 TTL 오버라이드
                .description("특별한 요구사항이 있는 캐시")
                .build();
                
        DomainCacheRegistry.registerCache(customCache);
    }
}
```

## 도메인별 캐시 예시

### 상품 도메인

```java
// 기본 상품 정보 - 자주 변경되지 않음
@Cacheable(value = "product", key = "#productId")
public Product getProduct(Long productId) { ... }

// 상품 조회수 - 실시간성 중요
@Cacheable(value = "product-view-count", key = "#productId")  
public Long getViewCount(Long productId) { ... }

// 브랜드별 인기 상품 - 집계 데이터
@Cacheable(value = "product:popular:by-brand", key = "#brandId")
public List<Product> getPopularProductsByBrand(String brandId) { ... }
```

### 사용자 도메인

```java
// 사용자 프로필
@Cacheable(value = "user", key = "#userId")
public User getUserProfile(Long userId) { ... }

// 사용자별 장바구니
@Cacheable(value = "user:cart", key = "#userId") 
public Cart getUserCart(Long userId) { ... }

// 사용자 권한 정보
@Cacheable(value = "user-permissions", key = "#userId")
public Set<Permission> getUserPermissions(Long userId) { ... }
```

## 고급 사용법

### 1. 조건부 캐싱

```java
@Cacheable(
    value = "product", 
    key = "#productId", 
    condition = "#productId != null",
    unless = "#result == null"
)
public Product getProduct(Long productId) { ... }
```

### 2. 캐시 제거

```java
@CacheEvict(value = "product", key = "#product.id")
public Product updateProduct(Product product) { ... }

@CacheEvict(value = {"product", "product-view-count"}, allEntries = true)
public void clearProductCaches() { ... }
```

### 3. 캐시 업데이트

```java
@CachePut(value = "user", key = "#user.id")
public User updateUser(User user) { ... }
```

## 프로그래밍 방식 캐시 관리

```java
@Service
public class CacheManagementService {
    
    @Autowired
    private CacheManager cacheManager;
    
    public void manualCacheOperation(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);           // 캐시 저장
            Object cached = cache.get(key);  // 캐시 조회
            cache.evict(key);                // 캐시 제거
        }
    }
}
```

## 모니터링 및 디버깅

### 등록된 캐시 확인

```java
@RestController
public class CacheInfoController {
    
    @GetMapping("/cache/info")
    public Map<String, Object> getCacheInfo() {
        return Map.of(
            "registeredCaches", RedisConfig.CacheUtils.getAllCacheNames(),
            "totalCount", DomainCacheRegistry.size()
        );
    }
    
    @GetMapping("/cache/{cacheName}/info")
    public CacheDefinition getCacheDefinition(@PathVariable String cacheName) {
        return RedisConfig.CacheUtils.getCacheInfo(cacheName);
    }
}
```

### 캐시 전략 확인

```java
public void printCacheStrategies() {
    DomainCacheRegistry.getAllCacheDefinitions().forEach((name, definition) -> {
        System.out.printf("Cache: %s, Strategy: %s, TTL: %s%n", 
            name, definition.getStrategy(), definition.getTtl());
    });
}
```

## 마이그레이션 가이드

### 기존 코드에서 마이그레이션

기존 코드는 수정 없이 그대로 사용 가능합니다:

```java
// 기존 코드 - 계속 동작함
@Cacheable(value = "product", key = "#id")
public Product getProduct(Long id) { ... }

// 새로운 방식으로 점진적 마이그레이션
@Cacheable(value = RedisConfig.CacheNames.PRODUCT, key = "#id")
public Product getProduct(Long id) { ... }
```

### 새 도메인 추가

```java
@Configuration
public class NewDomainCacheConfig {
    
    @PostConstruct
    public void registerCaches() {
        // 새 도메인의 캐시들을 등록
        DomainCacheConfigurations.NewDomainCaches.register();
    }
}
```

## 성능 고려사항

- **캐시 키 설계**: 효율적인 키 네이밍과 구조 설계
- **TTL 최적화**: 데이터 특성에 맞는 적절한 TTL 설정
- **메모리 사용량**: 큰 객체는 압축 또는 부분 캐싱 고려
- **네트워크 부하**: 배치 작업과 파이프라인 활용

## 트러블슈팅

### 일반적인 문제

1. **캐시가 동작하지 않음**: `@EnableCaching` 설정 확인
2. **캐시 키 중복**: 네임스페이스나 프리픽스 사용
3. **메모리 부족**: TTL 조정 또는 캐시 크기 제한
4. **직렬화 오류**: JSON 직렬화 가능한 객체 사용

### 디버깅 팁

```java
// 캐시 동작 로깅 활성화
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```

이 프레임워크를 사용하면 각 도메인의 특성에 맞는 캐시 전략을 쉽게 구현할 수 있으며, 프로젝트 전체에서 일관된 캐시 관리가 가능합니다.
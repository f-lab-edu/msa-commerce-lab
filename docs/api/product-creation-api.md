# Product Creation API

## 개요
상품 생성 API는 헥사고날 아키텍처를 따라 구현되었습니다.

## API 명세

### 상품 생성
**POST** `/api/v1/products`

#### 요청 본문
```json
{
  "name": "MacBook Pro 16",
  "description": "Apple MacBook Pro 16인치 M3 Max",
  "price": 3490000,
  "stockQuantity": 50,
  "category": "ELECTRONICS",
  "imageUrl": "https://example.com/macbook-pro-16.jpg"
}
```

#### 응답 (201 Created)
```json
{
  "id": 1,
  "name": "MacBook Pro 16",
  "description": "Apple MacBook Pro 16인치 M3 Max",
  "price": 3490000,
  "stockQuantity": 50,
  "category": "ELECTRONICS",
  "status": "ACTIVE",
  "imageUrl": "https://example.com/macbook-pro-16.jpg",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 유효성 검증 규칙
- **name**: 필수, 최대 100자
- **description**: 선택, 최대 2000자
- **price**: 필수, 0.01 ~ 10,000,000 범위
- **stockQuantity**: 필수, 0 이상
- **category**: 필수, 정의된 카테고리 중 하나
- **imageUrl**: 선택, 최대 500자

#### 카테고리 목록
- `ELECTRONICS`: 전자제품
- `CLOTHING`: 의류
- `BOOKS`: 도서
- `HOME_GARDEN`: 홈&가든
- `SPORTS`: 스포츠
- `BEAUTY`: 뷰티
- `FOOD`: 식품
- `TOYS`: 장난감
- `AUTOMOTIVE`: 자동차
- `HEALTH`: 건강

#### 오류 응답

**400 Bad Request** - 유효성 검증 실패
```json
{
  "message": "상품명은 필수입니다.",
  "timestamp": "2024-01-15T10:30:00"
}
```

**409 Conflict** - 중복된 상품명
```json
{
  "message": "이미 존재하는 상품명입니다: MacBook Pro 16",
  "timestamp": "2024-01-15T10:30:00"
}
```

## 아키텍처 구조

### 헥사고날 아키텍처 적용
```
┌─────────────────────────────────────────────────────────┐
│                   Adapter Layer                         │
│  ┌─────────────────┐              ┌─────────────────┐   │
│  │   Web Adapter   │              │Persistence      │   │
│  │ ProductController│              │Adapter          │   │
│  │                 │              │                 │   │
│  └─────────────────┘              └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
           │                                   │
           ▼                                   ▼
┌─────────────────────────────────────────────────────────┐
│                Application Layer                        │
│  ┌─────────────────┐              ┌─────────────────┐   │
│  │   Port (In)     │              │   Port (Out)    │   │
│  │ProductCreateUse │              │Product          │   │
│  │Case             │              │Repository       │   │
│  └─────────────────┘              └─────────────────┘   │
│              │                              ▲           │
│              ▼                              │           │
│  ┌─────────────────────────────────────────┴─────────┐ │
│  │         ProductCreateService                      │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   Domain Layer                          │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                   Product                           │ │
│  │  - 도메인 로직 (재고 관리, 상태 변경)                  │ │
│  │  - 비즈니스 규칙 검증                                │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 패키지 구조
```
com.msa.commerce.monolith.product
├── domain/                    # 도메인 레이어
│   ├── Product.java
│   ├── ProductCategory.java
│   └── ProductStatus.java
├── application/               # 애플리케이션 레이어
│   ├── port/
│   │   ├── in/               # 인바운드 포트
│   │   │   ├── ProductCreateUseCase.java
│   │   │   ├── ProductCreateCommand.java
│   │   │   └── ProductResponse.java
│   │   └── out/              # 아웃바운드 포트
│   │       └── ProductRepository.java
│   └── service/              # 애플리케이션 서비스
│       └── ProductCreateService.java
└── adapter/                   # 어댑터 레이어
    ├── in/                   # 인바운드 어댑터
    │   └── web/
    │       ├── ProductController.java
    │       └── ProductCreateRequest.java
    └── out/                  # 아웃바운드 어댑터
        └── persistence/
            ├── ProductJpaRepository.java
            └── ProductRepositoryImpl.java
```

## 테스트
- 도메인 테스트: 비즈니스 로직 검증
- 서비스 테스트: 유스케이스 시나리오 검증  
- 컨트롤러 테스트: API 계약 검증

전체 테스트 커버리지: 95% 이상 목표
# MSA Commerce Lab API Documentation

## 📖 Overview

이 디렉토리는 MSA Commerce Lab 프로젝트의 API 문서를 포함합니다. 
헥사고날 아키텍처를 기반으로 구현된 상품 관리 API의 명세서를 제공합니다.

## 📁 Structure

```
docs/
├── api/
│   └── monolith-openapi.yaml    # OpenAPI 3.0 명세서
├── html/
│   └── monolith.html            # 생성된 HTML 문서
└── README.md                    # 이 파일
```

## 🚀 Quick Start

```bash
# 1. OpenAPI 문서 검증
npm run docs:lint
# 또는: redocly lint

# 2. HTML 문서 생성
npm run docs:build
# 또는: redocly build-docs --output dist/index.html

# 3. 로컬 서버로 문서 서빙
npm run docs:serve
# 또는: cd dist && python3 -m http.server 8080

# 4. 브라우저에서 확인
open http://localhost:8080
```

## 🚀 Usage

### Documentation 빌드

```bash
# OpenAPI 문서 검증
npm run docs:lint

# HTML 문서 생성 (dist/index.html로 출력)
npm run docs:build

# 실시간 미리보기 (개발 서버)
npm run docs:preview

# 로컬 서버로 문서 서빙 (포트 8080)
npm run docs:serve
```

### 문서 보기

1. **빌드된 HTML 파일 직접 열기**
   ```bash
   open dist/index.html
   ```

2. **로컬 서버로 보기**
   ```bash
   npm run docs:serve
   # http://localhost:8080 에서 확인
   ```

3. **실시간 미리보기 (개발용)**
   ```bash
   npm run docs:preview
   # Redocly 개발 서버 실행 (실시간 리로드)
   ```

## 🛠 API Endpoints

### Product API

- **POST /api/v1/products** - 상품 생성
  - 새로운 상품을 등록합니다
  - SKU 자동 생성
  - 카테고리 ID 검증 (1-10)
  - 재고 관리 지원

### Schema

- **ProductCreateRequest**: 상품 생성 요청 스키마
- **ProductResponse**: 상품 응답 스키마
- **ProductStatus**: 상품 상태 (DRAFT, ACTIVE, INACTIVE, ARCHIVED)
- **ErrorResponse**: 오류 응답 스키마

## 🔧 Configuration

### Redocly 설정

- 파일: `redocly.yaml`
- 테마: 사용자 정의 색상 적용
- 검증: 권장 규칙 + 사용자 정의 규칙

### 개발 환경

- **Node.js 18+** (npx 지원)
- **@redocly/cli** (npx로 자동 설치)
- **http-server** (npx로 자동 설치)

> 💡 모든 도구는 npx를 통해 자동으로 최신 버전이 설치되므로 별도 설치 과정이 필요 없습니다.

## 📝 Examples

### 상품 생성 요청 예시

```json
{
  "name": "MacBook Pro 16",
  "description": "Apple MacBook Pro 16인치 M3 Max",
  "shortDescription": "고성능 16인치 맥북 프로",
  "brand": "Apple",
  "model": "MacBook Pro M3 Max",
  "price": 3490000,
  "comparePrice": 3790000,
  "categoryId": 1,
  "initialStock": 50,
  "visibility": "PUBLIC",
  "isFeatured": true
}
```

### 상품 생성 응답 예시

```json
{
  "id": 1,
  "categoryId": 1,
  "sku": "MACB-12AB34CD",
  "name": "MacBook Pro 16",
  "description": "Apple MacBook Pro 16인치 M3 Max",
  "price": 3490000,
  "status": "DRAFT",
  "visibility": "PUBLIC",
  "isFeatured": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 🎯 Business Rules

1. **SKU 생성**: 상품명 기반 자동 생성 (MACB-12AB34CD 형태)
2. **가격 범위**: 0.01원 ~ 99,999,999.99원
3. **카테고리**: 1-10 범위의 유효한 카테고리 ID 필요
4. **재고 관리**: 초기 재고 설정 및 추적 기능
5. **상태 관리**: DRAFT → ACTIVE → INACTIVE/ARCHIVED 라이프사이클

## 🏗 Architecture

이 API는 헥사고날 아키텍처 패턴을 따릅니다:

- **Inbound Adapter**: ProductController (Web Layer)
- **Application Core**: ProductCreateUseCase
- **Outbound Adapter**: ProductRepository (Persistence Layer)
- **Domain**: Product, ProductCategory, ProductStatus

## 📚 References

- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Redocly Documentation](https://redocly.com/docs/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
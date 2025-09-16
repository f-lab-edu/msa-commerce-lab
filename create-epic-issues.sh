#!/bin/bash

# MSA Commerce Lab Epic Issues 생성 스크립트
# 사용법: ./create-epic-issues.sh

REPO="f-lab-edu/msa-commerce-lab"

echo "🚀 MSA Commerce Lab Epic Issues 생성을 시작합니다..."

# Epic 1: Order Service 기본 기능 구현
echo "📝 Epic 1: Order Service 기본 기능 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Order Service 기본 기능 구현" \
  --assignee "joel-you" \
  --body "## Epic: Order Service 기본 기능 구현

**Priority:** High
**Duration:** 2주 (Sprint 4-5)
**Start Date:** 2025-09-01
**End Date:** 2025-09-15

### Description
주문 생성, 조회, 상태 관리 등 Order Service의 핵심 기능 구현

### Acceptance Criteria
- [ ] 주문 생성 API 구현
- [ ] 주문 조회 API 구현
- [ ] 주문 상태 변경 API 구현
- [ ] Order DB 설계 및 연동
- [ ] Kafka 메시지 발행 기능 구현

### Technical Requirements
- Spring Boot 기반 REST API
- JPA/Hibernate를 통한 데이터 액세스
- Kafka Producer 구현
- 단위 테스트 및 통합 테스트 작성

### Definition of Done
- 모든 API가 정상적으로 동작
- 테스트 커버리지 80% 이상
- API 문서화 완료
- 코드 리뷰 완료" \

# Epic 2: Payment Service 기본 기능 구현
echo "📝 Epic 2: Payment Service 기본 기능 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Payment Service 기본 기능 구현" \
  --assignee "joel-you" \
  --body "## Epic: Payment Service 기본 기능 구현

**Priority:** High
**Duration:** 2주 (Sprint 5-6)
**Start Date:** 2025-09-08
**End Date:** 2025-09-22

### Description
결제 처리, 검증, 취소 등 Payment Service의 핵심 기능 구현

### Acceptance Criteria
- [ ] 결제 요청 처리 API 구현
- [ ] 결제 상태 조회 API 구현
- [ ] 결제 취소/환불 API 구현
- [ ] Payment DB 설계 및 연동
- [ ] PG사 연동 기본 구조 구현

### Technical Requirements
- Spring Boot 기반 REST API
- 결제 상태 관리 (PENDING, SUCCESS, FAILED, CANCELLED)
- 외부 PG사 연동 인터페이스 설계
- 결제 실패 시 재시도 로직

### Definition of Done
- 모든 결제 API 동작 확인
- PG사 연동 테스트 완료
- 예외 처리 및 로깅 구현
- 보안 검토 완료" \

# Epic 3: Order-Orchestrator 구현
echo "📝 Epic 3: Order-Orchestrator 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Order-Orchestrator 구현 (최우선)" \
  --assignee "joel-you" \
  --body "## Epic: Order-Orchestrator 구현

**Priority:** Critical ⭐
**Duration:** 3주 (Sprint 6-8)
**Start Date:** 2025-09-15
**End Date:** 2025-10-06

### Description
주문과 결제 간의 오케스트레이션 로직 구현, Saga 패턴 적용

### Acceptance Criteria
- [ ] Order Orchestrator 서비스 기본 구조 구현
- [ ] Saga 패턴 기반 트랜잭션 관리 구현
- [ ] Order Service와의 통신 구현
- [ ] Payment Service와의 통신 구현
- [ ] 실패 시 보상 트랜잭션 로직 구현
- [ ] 상태 추적 및 모니터링 기능 구현

### Technical Requirements
- Saga 패턴 구현 (Choreography 또는 Orchestration)
- 분산 트랜잭션 상태 관리
- 보상 트랜잭션 (Compensating Transaction) 구현
- 이벤트 소싱 패턴 적용
- 장애 복구 메커니즘

### Business Flow
1. 주문 생성 요청
2. 재고 확인
3. 결제 처리
4. 주문 확정 또는 실패 시 롤백

### Definition of Done
- End-to-End 시나리오 테스트 완료
- 장애 상황 시뮬레이션 테스트 통과
- 성능 테스트 완료
- 모니터링 대시보드 구성" \

# Epic 4: Kafka 이벤트 기반 통신 구현
echo "📝 Epic 4: Kafka 이벤트 기반 통신 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Kafka 이벤트 기반 통신 구현" \
  --assignee "joel-you" \
  --body "## Epic: Kafka 이벤트 기반 통신 구현

**Priority:** High
**Duration:** 2주 (Sprint 7-8)
**Start Date:** 2025-09-22
**End Date:** 2025-10-06

### Description
서비스 간 비동기 통신을 위한 Kafka 이벤트 스트리밍 구현

### Acceptance Criteria
- [ ] Kafka 클러스터 설정 및 구성
- [ ] Order Topic 구현 (주문 생성, 상태 변경)
- [ ] Payment Result Topic 구현
- [ ] Retry Topic 구현 (실패 재처리)
- [ ] Dead Letter Topic 구현
- [ ] 이벤트 스키마 정의 및 관리

### Technical Requirements
- Kafka Cluster 구성 (Docker Compose)
- Schema Registry 설정
- Avro 스키마 정의
- Producer/Consumer 구현
- At-least-once 메시지 전달 보장

### Event Topics
- \`order.created\` - 주문 생성 이벤트
- \`order.updated\` - 주문 상태 변경 이벤트
- \`payment.result\` - 결제 결과 이벤트
- \`retry.events\` - 재처리 이벤트
- \`dead.letter\` - 처리 실패 이벤트

### Definition of Done
- 모든 토픽 정상 동작 확인
- 메시지 순서 보장 검증
- 장애 복구 테스트 완료
- 모니터링 설정 완료" \

# Epic 5: Settlement Service 구현
echo "📝 Epic 5: Settlement Service 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Settlement Service 구현" \
  --assignee "joel-you" \
  --body "## Epic: Settlement Service 구현

**Priority:** Medium
**Duration:** 2주 (Sprint 8-9)
**Start Date:** 2025-09-29
**End Date:** 2025-10-13

### Description
정산 처리를 위한 Settlement Service 구현

### Acceptance Criteria
- [ ] 일별/월별 정산 로직 구현
- [ ] 정산 데이터 수집 및 계산 기능
- [ ] 정산 내역 조회 API 구현
- [ ] Settlement DB 설계 및 연동
- [ ] 정산 보고서 생성 기능

### Technical Requirements
- 배치 처리 시스템 (Spring Batch)
- 대용량 데이터 처리 최적화
- 정산 규칙 엔진
- 보고서 생성 (PDF/Excel)

### Definition of Done
- 정산 정확성 검증 완료
- 성능 테스트 통과
- 스케줄링 설정 완료" \

# Epic 6: Materialized View Worker 구현
echo "📝 Epic 6: Materialized View Worker 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Materialized View Worker 구현" \
  --assignee "joel-you" \
  --body "## Epic: Materialized View Worker 구현

**Priority:** Medium
**Duration:** 2주 (Sprint 9-10)
**Start Date:** 2025-10-06
**End Date:** 2025-10-20

### Description
실시간 데이터 동기화를 위한 Materialized View Worker 구현

### Acceptance Criteria
- [ ] Kafka 메시지 소비 기능 구현
- [ ] 실시간 뷰 데이터 업데이트 로직
- [ ] Redis 캐시 연동 및 관리
- [ ] 데이터 일관성 보장 로직
- [ ] 장애 복구 및 재처리 메커니즘

### Technical Requirements
- Kafka Consumer 구현
- Redis Cluster 연동
- CQRS 패턴 적용
- Event Sourcing
- 데이터 동기화 검증

### Definition of Done
- 실시간 동기화 성능 검증
- 데이터 정합성 테스트 완료
- 장애 복구 시나리오 테스트" \

# Epic 7: Notification Service 구현
echo "📝 Epic 7: Notification Service 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "Notification Service 구현" \
  --assignee "joel-you" \
  --body "## Epic: Notification Service 구현

**Priority:** Low
**Duration:** 1주 (Sprint 10)
**Start Date:** 2025-10-13
**End Date:** 2025-10-20

### Description
주문/결제 상태 변경 시 알림 발송 기능 구현

### Acceptance Criteria
- [ ] 알림 발송 API 구현
- [ ] 이메일/SMS 발송 기능
- [ ] 알림 템플릿 관리
- [ ] 알림 이력 저장 및 조회
- [ ] 알림 설정 관리 기능

### Technical Requirements
- 다중 채널 알림 지원
- 템플릿 엔진 적용
- 대용량 발송 처리
- 발송 실패 재시도 로직

### Definition of Done
- 모든 알림 채널 테스트 완료
- 템플릿 관리 시스템 구축
- 발송 성능 검증" \

# Epic 8: API Gateway 및 인증/인가 구현
echo "📝 Epic 8: API Gateway 및 인증/인가 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "API Gateway 및 인증/인가 구현" \
  --assignee "joel-you" \
  --body "## Epic: API Gateway 및 인증/인가 구현

**Priority:** High
**Duration:** 2주 (Sprint 10-11)
**Start Date:** 2025-10-13
**End Date:** 2025-10-27

### Description
마이크로서비스 진입점 및 보안 관리 구현

### Acceptance Criteria
- [ ] API Gateway 기본 라우팅 구현
- [ ] JWT 기반 인증 구현
- [ ] 권한별 API 접근 제어
- [ ] Rate Limiting 구현
- [ ] API 요청/응답 로깅
- [ ] Circuit Breaker 패턴 적용

### Technical Requirements
- Spring Cloud Gateway 또는 Zuul
- JWT Token 관리
- OAuth2/OIDC 지원
- Rate Limiting 알고리즘
- 로그 수집 및 분석

### Definition of Done
- 모든 라우팅 규칙 테스트 완료
- 보안 취약점 검사 통과
- 성능 부하 테스트 완료" \

# Epic 9: 통합 테스트 및 모니터링 구현
echo "📝 Epic 9: 통합 테스트 및 모니터링 구현 생성 중..."
gh issue create \
  --repo "$REPO" \
  --title "통합 테스트 및 모니터링 구현" \
  --assignee "joel-you" \
  --body "## Epic: 통합 테스트 및 모니터링 구현

**Priority:** Medium
**Duration:** 2주 (Sprint 11-12)
**Start Date:** 2025-10-20
**End Date:** 2025-11-03

### Description
전체 시스템 통합 테스트 및 모니터링 환경 구축

### Acceptance Criteria
- [ ] End-to-End 테스트 시나리오 작성
- [ ] 성능 테스트 환경 구축
- [ ] 헬스체크 API 구현
- [ ] 메트릭 수집 및 대시보드 구축
- [ ] 로그 수집 및 분석 환경 구축
- [ ] 알람 및 장애 대응 시스템 구축

### Technical Requirements
- TestContainers 기반 통합 테스트
- JMeter/K6 성능 테스트
- Prometheus + Grafana 모니터링
- ELK Stack 로그 관리
- PagerDuty/Slack 알람 연동

### Test Scenarios
- 주문-결제 완전 성공 플로우
- 결제 실패 시 롤백 플로우
- 대용량 주문 처리 성능 테스트
- 장애 상황 복구 테스트

### Definition of Done
- 모든 E2E 테스트 자동화 완료
- 모니터링 대시보드 구축
- 알람 규칙 설정 완료
- 운영 가이드 문서 작성" \

echo "✅ 모든 Epic 이슈가 성공적으로 생성되었습니다!"
echo ""
echo "🔗 프로젝트 보드에서 확인하세요: https://github.com/orgs/f-lab-edu/projects/405/views/1"
echo "🔗 이슈 목록: https://github.com/f-lab-edu/msa-commerce-lab/issues"

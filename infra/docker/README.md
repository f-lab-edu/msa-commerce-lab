# Docker Infrastructure Guide

MSA Commerce Lab의 로컬 개발 환경을 위한 Docker Compose 설정 가이드입니다.

## 📁 파일 구조

```
infra/docker/
├── ecommerce.local.yml      # KRaft 모드 Kafka
├── mysql/                   # MySQL 설정 파일
└── README.md               # 이 문서
```

## 🚀 Kafka KRaft 모드

```bash
# 최신 Kafka 아키텍처 - Zookeeper 불필요
docker-compose -f infra/docker/ecommerce.local.yml up -d
```

**장점:**

- ✅ 단순한 아키텍처 (Kafka 단일 서비스)
- ✅ 빠른 시작 시간 (~30초)
- ✅ 적은 메모리 사용량 (~1.5GB)
- ✅ Apache Kafka 3.x+ 표준
- ✅ Kafka 4.0+ 미래 지향적 (Zookeeper 지원 완전 제거 예정)
- ✅ 운영 복잡성 감소

**왜 KRaft만 사용하나요?**

- 🔮 **미래 지향적**: Kafka 4.0에서 Zookeeper 지원 완전 제거 예정
- 🎯 **Apache 권장**: 새로운 프로젝트는 KRaft 모드 사용 권장
- ⚡ **성능 향상**: 더 빠른 메타데이터 처리
- 🛠️ **단순함**: 관리 포인트 감소

## 🔧 서비스 구성

### 공통 서비스

- **MySQL 9.4.0**: 메인 데이터베이스
    - Port: 3306
    - Database: db_flyway
    - User: app_rw / Password: 1q2w3e4r!

- **Redis 8.2.0**: 캐시 및 세션 스토어
    - Port: 6379
    - Password: 1q2w3e4r

### Kafka KRaft 설정

| 구성요소                | 설정값                                    |
|---------------------|----------------------------------------|
| **Kafka Container** | msa-ecommerce-kafka                    |
| **총 컨테이너 수**        | 3개 (MySQL, Redis, Kafka)               |
| **Kafka 설정**        | KAFKA_PROCESS_ROLES: broker,controller |
| **메타데이터 저장**        | 자체 로그 디렉토리 (/tmp/kraft-combined-logs)  |
| **시작 의존성**          | 없음 (독립적 시작)                            |
| **포트**              | 9092 (클라이언트), 9101 (JMX)               |

## 📋 사용법

### 서비스 시작

```bash
# KRaft 모드 Kafka 시작
docker-compose -f infra/docker/ecommerce.local.yml up -d
```

### 서비스 상태 확인

```bash
# 컨테이너 상태 확인
docker ps

# 헬스체크 상태 확인
docker-compose -f infra/docker/ecommerce.local.yml ps
```

### Kafka 기능 테스트

```bash
# 토픽 생성
docker exec msa-ecommerce-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic test-topic \
  --partitions 3 --replication-factor 1

# 토픽 목록 확인
docker exec msa-ecommerce-kafka kafka-topics \
  --bootstrap-server localhost:9092 --list

# 메시지 전송 테스트
echo "Hello Kafka" | docker exec -i msa-ecommerce-kafka \
  kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic

# 메시지 수신 테스트
docker exec msa-ecommerce-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 --topic test-topic --from-beginning --timeout-ms 5000
```

### 서비스 중지 및 정리

```bash
# 서비스 중지
docker-compose -f infra/docker/ecommerce.local.yml down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose -f infra/docker/ecommerce.local.yml down -v
```

## 🎯 KRaft 모드 장점

### 개발 환경

- **리소스 효율적**: 적은 메모리와 CPU 사용
- **빠른 시작**: 30초 내 완전 구동
- **단순 구조**: 관리할 서비스 감소

### 운영 준비

- **미래 지향**: Kafka 4.0+ 표준
- **성능 향상**: 더 빠른 메타데이터 처리
- **안정성**: 단일 장애점 감소

### 학습/연구

- **최신 기술**: Apache Kafka 최신 아키텍처 학습
- **업계 트렌드**: 현대적 스트리밍 플랫폼 표준
- **실무 적용**: 실제 프로덕션 환경과 동일

## 🔍 트러블슈팅

### 일반적인 문제

1. **포트 충돌**: 기존 서비스와 포트 겹침
   ```bash
   # 포트 사용 확인
   netstat -tulpn | grep :9092
   lsof -i :9092
   ```

2. **메모리 부족**: Docker Desktop 메모리 한계
   ```bash
   # Docker Desktop에서 메모리 할당량 증가 (4GB+ 권장)
   ```

3. **클러스터 ID 오류**: 기존 볼륨 데이터 충돌
   ```bash
   # KRaft 볼륨 초기화
   docker volume rm docker_kafka-data
   docker-compose -f infra/docker/ecommerce.local.yml up -d
   ```

4. **서비스 시작 실패**: 컨테이너 로그 확인
   ```bash
   # Kafka 로그 확인
   docker logs msa-ecommerce-kafka
   
   # 모든 서비스 상태 확인
   docker-compose -f infra/docker/ecommerce.local.yml ps
   ```

## 📚 추가 정보

- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [KRaft 모드 가이드](https://kafka.apache.org/documentation/#kraft)
- [Kafka 4.0 로드맵](https://cwiki.apache.org/confluence/display/KAFKA/KIP-833%3A+Mark+KRaft+as+Production+Ready)
- [Confluent Platform 가이드](https://docs.confluent.io/)
- [Docker Compose 참조](https://docs.docker.com/compose/)

## 🔮 미래 전망

**Kafka 4.0 주요 변경사항 (예정)**:

- ❌ Zookeeper 지원 완전 제거
- ✅ KRaft 모드가 유일한 클러스터 모드
- ⚡ 성능 및 안정성 개선
- 🛠️ 운영 도구 완전 지원

**결론**: 지금부터 KRaft 모드로 개발하는 것이 미래를 위한 최선의 선택입니다.

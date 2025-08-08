 # MSA Commerce Lab - Docker Infrastructure

로컬 개발 환경을 위한 Docker Compose 설정입니다.

## 🚀 Quick Start

```bash
# 전체 인프라 시작
docker-compose up -d

# 특정 서비스만 시작
docker-compose up -d mysql-unified redis

# 로그 확인
docker-compose logs -f mysql-unified

# 인프라 중지
docker-compose down
```

## 📊 Architecture Overview

### **변경된 Database 구조**
- **기존**: 4개 MySQL 인스턴스 (포트: 3306, 3307, 3308, 3309)
- **현재**: 1개 MySQL 서버 + 4개 Database (포트: 3306)

### **Database 구성**
```yaml
mysql-unified:3306:
  - monolith_db         # Monolith Service
  - order_db           # Order Orchestrator  
  - payment_db         # Payment Service
  - materialized_view_db # Materialized View Service
```

## 🏗️ Infrastructure Components

### **Database Layer**
- **MySQL 8.0**: 통합 데이터베이스 서버
- **Redis 7**: 캐싱 및 세션 저장소

### **Message Queue**
- **Apache Kafka**: 이벤트 스트리밍
- **Zookeeper**: Kafka 코디네이터

## 🔧 Configuration Details

### **MySQL Configuration**
- **포트**: 3306
- **최대 연결**: 200
- **문자셋**: utf8mb4_unicode_ci
- **Health Check**: 10초 간격

### **서비스별 Database 연결**
```yaml
services:
  monolith:         mysql-unified:3306/monolith_db
  order-orchestrator: mysql-unified:3306/order_db
  payment-service:  mysql-unified:3306/payment_db
  materialized-view: mysql-unified:3306/materialized_view_db
```

## 💾 Data Persistence

### **Volume Mapping**
- `mysql_unified_data`: MySQL 데이터 저장소
- `redis_data`: Redis 데이터 저장소
- `kafka_data`: Kafka 로그 저장소
- `zookeeper_data`: Zookeeper 데이터 저장소

### **Database Schema**
모든 데이터베이스 스키마와 초기 데이터는 `mysql/init/00_database_initialization.sql`에서 관리됩니다.

## 🔍 Database Access

### **MySQL 접속**
```bash
# Docker 컨테이너를 통한 접속
docker exec -it mysql-unified mysql -u root -p

# 직접 접속 (MySQL Client 필요)
mysql -h localhost -P 3306 -u root -p

# 특정 데이터베이스 접속
mysql -h localhost -P 3306 -u root -p monolith_db
```

### **GUI 툴 설정**
- **Host**: localhost
- **Port**: 3306  
- **Username**: root 또는 app_user
- **Password**: password

## 📈 Resource Usage

### **Before (다중 MySQL)**
- 메모리 사용량: ~800MB-1.2GB
- 컨테이너 수: 4개 MySQL + 기타
- 포트 사용: 3306, 3307, 3308, 3309

### **After (단일 MySQL)**
- 메모리 사용량: ~400MB-500MB (50% 절약)
- 컨테이너 수: 1개 MySQL + 기타  
- 포트 사용: 3306만

## 🛠️ Maintenance Commands

### **데이터베이스 백업**
```bash
# 전체 데이터베이스 백업
docker exec mysql-unified mysqldump -u root -ppassword --all-databases > backup.sql

# 특정 데이터베이스 백업  
docker exec mysql-unified mysqldump -u root -ppassword monolith_db > monolith_backup.sql
```

### **데이터베이스 복원**
```bash
# 백업 파일 복원
docker exec -i mysql-unified mysql -u root -ppassword < backup.sql
```

### **로그 모니터링**
```bash
# 전체 로그
docker-compose logs -f

# MySQL 로그만
docker-compose logs -f mysql-unified

# 실시간 로그 (마지막 100라인)
docker-compose logs -f --tail=100 mysql-unified
```

## 🔧 Troubleshooting

### **일반적인 문제들**

#### **Connection Refused Error**
```bash
# 컨테이너 상태 확인
docker-compose ps

# MySQL 헬스체크 상태 확인
docker inspect mysql-unified | jq '.[0].State.Health'
```

#### **Port Already in Use**
```bash
# 3306 포트 사용 프로세스 확인
lsof -i :3306

# 기존 MySQL 서비스 중지
sudo systemctl stop mysql
# 또는 macOS의 경우
brew services stop mysql
```

#### **Database Connection Issues**
1. `application.yml`의 URL이 `localhost:3306`인지 확인
2. 데이터베이스명이 정확한지 확인
3. Flyway 마이그레이션 상태 확인

### **성능 최적화**

#### **MySQL 성능 튜닝**
```sql
-- 연결 상태 확인
SHOW PROCESSLIST;

-- 성능 상태 확인
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Threads_running';

-- 슬로우 쿼리 설정
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';
SET GLOBAL long_query_time = 2;
```

## 🔄 Migration Guide

### **기존 환경에서 마이그레이션**

1. **기존 데이터 백업** (선택사항)
2. **기존 컨테이너 중지**: `docker-compose down`
3. **볼륨 정리**: `docker volume prune`
4. **새 환경 시작**: `docker-compose up -d`

### **애플리케이션 설정 변경사항**
모든 서비스의 `application.yml`에서 포트가 `3306`으로 변경되었습니다:
- `localhost:3307` → `localhost:3306`
- `localhost:3308` → `localhost:3306`  
- `localhost:3309` → `localhost:3306`

---

## 📞 Support

문제가 발생할 경우:
1. `docker-compose logs` 로그 확인
2. 컨테이너 상태 확인: `docker-compose ps`
3. 네트워크 연결 확인: `docker network ls`
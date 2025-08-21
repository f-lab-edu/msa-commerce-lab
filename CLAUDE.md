# MSA Commerce Lab - Claude Agents

## Available Agents

### 🏗️ Backend Hexagonal Specialist
**File**: `.claude/agents/backend-hexagonal-specialist.md`
**Specialization**: Spring Boot microservices with hexagonal architecture, domain-driven design, and Java best practices

**Use Cases**:
```bash
# Implement new domain features with clean architecture
> Use backend-hexagonal-specialist to implement order status tracking with event sourcing

# Review architecture compliance
> Use backend-hexagonal-specialist to review product domain architecture

# Design and implement APIs
> Use backend-hexagonal-specialist to implement product search API with filtering
```

### 🗄️ Database Migration Specialist  
**File**: `.claude/agents/database-migration-specialist.md`
**Specialization**: Database schema design, Flyway migrations, multi-database microservices architecture, and MySQL optimization

**Use Cases**:
```bash
# Schema evolution and migrations
> Use database-migration-specialist to add product inventory tracking with low-stock alerts

# Cross-service data consistency
> Use database-migration-specialist to implement order-payment data synchronization

# Performance optimization
> Use database-migration-specialist to optimize product search queries
```

### 🧪 Testing & Quality Specialist
**File**: `.claude/agents/testing-quality-specialist.md`
**Specialization**: Comprehensive testing strategy, quality assurance, test automation, and code quality metrics

**Use Cases**:
```bash
# Implement comprehensive testing
> Use testing-quality-specialist to implement testing strategy for product creation API

# Microservices integration testing
> Use testing-quality-specialist to implement integration testing between order and payment services

# Quality gates implementation
> Use testing-quality-specialist to implement automated quality gates
```

### 🚀 Build & Deployment Specialist
**File**: `.claude/agents/build-deployment-specialist.md`
**Specialization**: Gradle multi-module builds, CI/CD pipelines, containerization, and microservices deployment orchestration

**Use Cases**:
```bash
# Build system optimization
> Use build-deployment-specialist to optimize Gradle build performance

# CI/CD pipeline implementation  
> Use build-deployment-specialist to implement automated deployment pipeline

# Containerization strategy
> Use build-deployment-specialist to containerize microservices
```

## Project Architecture Overview

### Current Structure
- **Multi-Module Gradle**: Common, monolith, order-orchestrator, payment-service, materialized-view
- **Architecture**: Hexagonal architecture with DDD patterns
- **Databases**: Multi-database setup (db_platform, db_order, db_payment)
- **Testing**: JUnit 5, Mockito, Testcontainers, Jacoco coverage
- **Build**: Gradle with Kotlin DSL, custom plugins, SonarQube integration

### Key Domains
- **Product Management**: Product creation, inventory, categories
- **Order Processing**: Order orchestration, status management  
- **Payment Processing**: Payment gateway integration, transactions
- **User Management**: User profiles, addresses, authentication

## Agent Collaboration Patterns

### Feature Implementation Flow
1. **Backend Specialist** → Design domain model and business logic
2. **Database Specialist** → Create schema changes and migrations  
3. **Testing Specialist** → Implement comprehensive test coverage
4. **Build Specialist** → Configure deployment and CI/CD integration

### Architecture Review Flow
1. **Backend Specialist** → Review hexagonal architecture compliance
2. **Database Specialist** → Validate data model and performance
3. **Testing Specialist** → Ensure proper test coverage and quality
4. **Build Specialist** → Verify build and deployment configurations

## Quick Commands

### Agent Setup
```bash
# The agents are already set up and ready to use
> List available agents and their specializations
```

### Upgrading
```bash
> Use agent-architect to upgrade agents
```

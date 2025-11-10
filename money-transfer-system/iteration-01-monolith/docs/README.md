# 🏦 QuickPay Money Transfer System

## Iteration 1 - Monolithic Application

A complete money transfer system demonstrating traditional monolithic architecture with manual deployment processes.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Demo Accounts](#demo-accounts)
- [Known Limitations](#known-limitations)
- [Metrics & Observations](#metrics--observations)

---

## 🎯 Overview

QuickPay is a demonstration money transfer system built to showcase:
- Traditional monolithic architecture
- Manual deployment processes
- Testing bottlenecks
- Scalability challenges

This is **Iteration 1** - the baseline that will be transformed through subsequent iterations.

---

## ✨ Features

### Core Features
- ✅ User authentication
- ✅ Account balance inquiry
- ✅ Money transfer between accounts
- ✅ Transaction history
- ✅ Real-time balance updates

### Technical Features
- ✅ RESTful API
- ✅ Web-based UI (Thymeleaf)
- ✅ PostgreSQL database
- ✅ Basic fraud detection
- ✅ Async notifications
- ✅ Audit logging

---

## 🏗️ Architecture

### System Architecture
```
┌─────────────────┐
│   Web Browser   │
└────────┬────────┘
         │
┌────────▼────────────────────┐
│  Spring Boot Application    │
│  (Monolithic)                │
│                              │
│  ┌─────────────────────┐   │
│  │   Controllers       │   │
│  └──────────┬──────────┘   │
│             │               │
│  ┌──────────▼──────────┐   │
│  │   Services          │   │
│  │  • Transfer         │   │
│  │  • Account          │   │
│  │  • Fraud Detection  │   │
│  │  • Notification     │   │
│  │  • Audit            │   │
│  └──────────┬──────────┘   │
│             │               │
│  ┌──────────▼──────────┐   │
│  │   Repositories      │   │
│  └──────────┬──────────┘   │
└─────────────┼──────────────┘
              │
┌─────────────▼──────────────┐
│   PostgreSQL Database      │
│   • users                  │
│   • accounts               │
│   • transactions           │
│   • audit_logs             │
└────────────────────────────┘
```

### Deployment Model
```
┌────────────────────────────┐
│   Single Server (VM)       │
│   Location: Mumbai DC      │
│                            │
│   • Application (8080)     │
│   • Database (5432)        │
│   • No redundancy          │
│   • Manual scaling         │
└────────────────────────────┘
```

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Spring Boot 3.2.0, Java 17 |
| **Database** | PostgreSQL 14 |
| **Frontend** | Thymeleaf, HTML, CSS, JavaScript |
| **Build Tool** | Maven |
| **Testing** | JUnit 5, Mockito |
| **Logging** | SLF4J, Logback |

---

## 📦 Prerequisites

### Required
- ☕ Java 17 or higher
- 🗄️ PostgreSQL 14 or higher
- 📦 Maven 3.8+ (or use included wrapper)

### Optional
- 🐳 Docker & Docker Compose (for containerized deployment)
- 🔧 Git (for version control)

---

## 📥 Installation

### Option 1: Quick Start with Docker
```bash
# Clone repository
git clone <repository-url>
cd money-transfer-system/iteration-01-monolith

# Start with Docker Compose
chmod +x scripts/docker-start.sh
./scripts/docker-start.sh

# Access application
open http://localhost:8080
```

### Option 2: Manual Installation

#### Step 1: Setup Database

**Using Docker:**
```bash
chmod +x scripts/docker-database.sh
./scripts/docker-database.sh
```

**Using Local PostgreSQL:**
```bash
chmod +x scripts/setup-database.sh
./scripts/setup-database.sh
```

**Manual Setup:**
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database and user
CREATE DATABASE quickpay_db;
CREATE USER quickpay_user WITH PASSWORD 'quickpay_password';
GRANT ALL PRIVILEGES ON DATABASE quickpay_db TO quickpay_user;
```

#### Step 2: Configure Application

Edit `quickpay-monolith/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/quickpay_db
spring.datasource.username=quickpay_user
spring.datasource.password=quickpay_password
```

#### Step 3: Build Application
```bash
cd quickpay-monolith
./mvnw clean package -DskipTests
```

---

## 🚀 Running the Application

### Option 1: Using Script

**Linux/Mac:**
```bash
chmod +x scripts/start.sh
./scripts/start.sh
```

**Windows:**
```batch
scripts\start.bat
```

### Option 2: Manual Start
```bash
cd quickpay-monolith
java -jar target/quickpay-monolith-1.0.0.jar
```

### Option 3: Maven
```bash
cd quickpay-monolith
./mvnw spring-boot:run
```

### Verification

**Health Check:**
```bash
curl http://localhost:8080/api/health
```

**Access Application:**
```
http://localhost:8080
```

---

## 🧪 Testing

### Run All Tests
```bash
cd quickpay-monolith
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=TransferServiceTest
```

### Integration Tests
```bash
./mvnw verify
```

### Test Coverage
```bash
./mvnw jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 📚 API Documentation

### Authentication

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "ramesh@example.com",
  "password": "any"
}

Response: 200 OK
{
  "message": "Login successful",
  "email": "ramesh@example.com",
  "fullName": "Ramesh Kumar",
  "success": true
}
```

### Accounts

#### Get Balance
```http
GET /api/accounts/{accountNumber}/balance

Response: 200 OK
{
  "accountNumber": "ACC123456",
  "balance": 50000.00,
  "currency": "INR",
  "status": "ACTIVE"
}
```

### Transfers

#### Transfer Money
```http
POST /api/transfers
Content-Type: application/json

{
  "fromAccountNumber": "ACC123456",
  "toAccountNumber": "ACC987654",
  "amount": 5000.00,
  "description": "Birthday gift"
}

Response: 200 OK
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "message": "Transfer completed successfully",
  "success": true
}
```

#### Get Transaction History
```http
GET /api/transfers/history/{accountNumber}

Response: 200 OK
[
  {
    "transactionId": "550e8400-e29b-41d4-a716-446655440000",
    "fromAccountNumber": "ACC123456",
    "toAccountNumber": "ACC987654",
    "amount": 5000.00,
    "currency": "INR",
    "status": "COMPLETED",
    "description": "Birthday gift",
    "createdAt": "2024-12-10T15:30:00",
    "type": "DEBIT"
  }
]
```

---

## 👥 Demo Accounts

| Email | Account Number | Balance | Password |
|-------|---------------|---------|----------|
| ramesh@example.com | ACC123456 | ₹50,000 | any |
| priya@example.com | ACC987654 | ₹30,000 | any |
| amit@example.com | ACC555777 | ₹75,000 | any |

**Note:** For demo purposes, any password works. In production, use proper authentication.

---

## ⚠️ Known Limitations

### Architecture
- ❌ Single point of failure (one server)
- ❌ No horizontal scaling capability
- ❌ Tight coupling between components
- ❌ Single database (bottleneck)
- ❌ No caching layer

### Deployment
- ❌ Manual deployment (4-6 hours)
- ❌ Weekend-only deployments
- ❌ High deployment failure rate (30-40%)
- ❌ Requires 2-4 hours downtime
- ❌ Complex rollback process (8-12 hours)

### Testing
- ❌ Mostly manual testing (2-3 weeks)
- ❌ Low automation coverage (0-10%)
- ❌ Cannot run full regression
- ❌ Environment inconsistencies
- ❌ Slow feedback (3-7 days)

### Operations
- ❌ Cannot handle peak load (5x traffic)
- ❌ No auto-scaling
- ❌ Fixed infrastructure costs
- ❌ No disaster recovery
- ❌ Manual monitoring

---

## 📊 Metrics & Observations

### Baseline Metrics

| Category | Metric | Value |
|----------|--------|-------|
| **Deployment** | Frequency | 30 days |
| | Duration | 4-6 hours |
| | Success Rate | 60-70% |
| | Downtime | 2-4 hours |
| **Testing** | Test Cycle | 14-21 days |
| | Automation | 0-10% |
| | Feedback Time | 3-7 days |
| **Performance** | Transaction Time | 3-5 seconds |
| | Throughput | 50 TPS |
| | Availability | 99.5% |
| **Cost** | Infrastructure | ₹10L/month (fixed) |
| | Total Waste | ₹68L/month |
| **Business** | Time to Market | 3-4 months |
| | Satisfaction | 3.5/5 |

### Issues Observed
1. **Scalability:** Cannot handle 5x traffic spike
2. **Deployment:** High risk, long duration
3. **Testing:** Manual bottleneck
4. **Cost:** Over-provisioned infrastructure
5. **Team:** Burnout from weekend deployments

---

## 🛑 Stopping the Application

### Using Script
```bash
./scripts/stop.sh
```

### Manual
```bash
# Find process
ps aux | grep quickpay-monolith

# Kill process
kill -9 <PID>
```

### Docker
```bash
docker-compose down
```

---

## 📝 Next Steps

This iteration demonstrates the baseline. In subsequent iterations, we will:

- **Iteration 2:** Add CI/CD pipeline (automation)
- **Iteration 3:** Move to cloud + containers
- **Iteration 4:** Break into microservices
- **Iteration 5:** Implement shift-left testing
- **Iteration 6:** Add security & compliance
- **Iteration 7:** Full observability & advanced deployment

---

## 📞 Support

For questions or issues:
- Review logs: `logs/quickpay.log`
- Check health: `http://localhost:8080/api/health`
- Database logs: Check PostgreSQL logs

---

## 📄 License

This is a demonstration project for training purposes.

---

**Built with ❤️ for CI/CD, Cloud & Testing Workshop**
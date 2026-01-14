# Mini Core Banking System

A Pet project Spring Boot backend application demonstrating advanced **data consistency**, **concurrency control**, and **high-volume transaction handling** in the banking domain.

## 🎯 Purpose

This project showcases expertise in:
- **ACID compliance** with strict transactional guarantees
- **Concurrency control** using pessimistic locking to prevent double spending
- **Audit logging** for regulatory compliance
- **JWT-based authentication** with Spring Security
- **Scheduled background jobs** for interest calculation

## 🏗️ Architecture

**3-Layer Architecture:**
```
Controller Layer (REST APIs)
    ↓
Service Layer (@Transactional business logic)
    ↓
Repository Layer (JPA with pessimistic locking)
    ↓
Supabase PostgreSQL Database
```

## 🔑 Key Features

### 1. **User Registration & KYC**
- BCrypt password hashing
- KYC verification status tracking
- Automatic savings account creation

### 2. **Fund Transfers (CORE FEATURE)**
**Demonstrates concurrency control:**
- Pessimistic locking with `@Lock(LockModeType.PESSIMISTIC_WRITE)`
- `@Transactional` with `READ_COMMITTED` isolation
- Deadlock prevention via ordered lock acquisition
- Atomic operations (all-or-nothing)

### 3. **Savings Interest Calculation**
- Scheduled job runs daily at midnight
- Calculates daily interest: `(balance × annual_rate) / 365`
- Automatically posts interest to accounts

### 4. **Audit Trail**
- Every balance change is logged
- Immutable audit records
- Compliance-ready tracking

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.2.1 |
| Language | Java 21 |
| Database | Supabase (PostgreSQL) |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT |
| Build Tool | Maven |
| Password Encoding | BCrypt |

## 📊 Database Schema

**Tables:**
- `users` - Customer information and KYC data
- `accounts` - Bank accounts with BigDecimal balances
- `transactions` - Immutable transfer records
- `audit_logs` - Compliance audit trail

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- Supabase account (free tier)

### Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd CoreBanking
   ```

2. **Configure Supabase**
   - Create a Supabase project
   - Note your connection details (Flyway will create tables automatically!)

3. **Configure Application**
   
   Update `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://db.your-project.supabase.co:5432/postgres
       username: postgres
       password: your-password
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   
   **Flyway will automatically create all database tables on first run!**

5. **Test APIs**
   
   See [SETUP_GUIDE.md](docs/SETUP_GUIDE.md) for detailed testing instructions.

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Transfers (Protected)
- `POST /api/transfers` - Execute fund transfer

### Accounts (Protected)
- `GET /api/accounts/{id}` - Get account details
- `GET /api/accounts/{id}/audit` - Get audit trail
- `GET /api/accounts/user/{userId}` - Get user's accounts

## 🎓 Learning Outcomes

This project demonstrates understanding of:

1. **ACID Properties**
   - Atomicity via @Transactional
   - Consistency with constraints
   - Isolation with READ_COMMITTED
   - Durability through database commits

2. **Concurrency Patterns**
   - Pessimistic locking
   - Deadlock prevention
   - Race condition mitigation

3. **Best Practices**
   - BigDecimal for money (never float/double!)
   - Immutable audit logs
   - Service-layer transactions
   - Comprehensive exception handling

## 🚧 Future Enhancements

- [ ] Optimistic locking implementation for comparison
- [ ] Integration tests with Testcontainers
- [ ] JMeter performance benchmarks
- [ ] Daily transaction limits
- [ ] Multi-currency support
- [ ] WebSocket notifications for transfers
- [ ] Admin dashboard

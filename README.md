# Core Banking System

A Spring Boot backend for a banking application with transaction safety and concurrency control.

## Front-End
Available at: https://corebankingfe.vercel.app/
**Disclaimer**: Loading speed is slow due to server speed at free tier.
## Features

- User registration with JWT authentication
- Fund transfers between accounts
- Pessimistic locking to prevent race conditions
- Audit logging for compliance
- Automated interest calculation

## Tech Stack

- **Framework**: Spring Boot 3.2.1
- **Language**: Java 21
- **Database**: PostgreSQL (Supabase)
- **Security**: Spring Security + JWT
- **Build Tool**: Maven
- **Deployment**: Koyeb (https://app.koyeb.com/)

## Database Schema

- `users` - User accounts and KYC info
- `accounts` - Bank accounts
- `transactions` - Transfer records
- `audit_logs` - Balance change history

## Setup

1. Clone the repo
   ```bash
   git clone <your-repo-url>
   cd CoreBanking
   ```

2. Configure database in `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://your-host:5432/postgres
       username: your-username
       password: your-password
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

Flyway will create tables automatically on first run.

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Transfers (Protected)
- `POST /api/transfers` - Execute fund transfer

### Accounts (Protected)
- `GET /api/accounts/{id}` - Get account details
- `GET /api/accounts/{id}/audit` - View audit trail
- `GET /api/accounts/user/{userId}` - Get user's accounts

## Notes

- Flyway handles database migrations
- Daily interest calculation runs at midnight
- All monetary values use BigDecimal for precision

## Future Ideas

- Transaction limits
- Multi-currency support
- Admin dashboard
- Scheduled transfers

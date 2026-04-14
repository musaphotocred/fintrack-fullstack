# FinTrack — Backend Requirements Specification

> **Stack:** Spring Boot 3.x (Java 21) · MySQL 8 · AWS Elastic Beanstalk · AWS Lambda  
> **Version:** 1.0 | **Type:** Portfolio Project

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack & Versions](#2-tech-stack--versions)
3. [Repository Structure](#3-repository-structure)
4. [Database Schema](#4-database-schema)
5. [Flyway Migrations](#5-flyway-migrations)
6. [API Modules & Endpoints](#6-api-modules--endpoints)
7. [Authentication & Security](#7-authentication--security)
8. [Logging & Monitoring](#8-logging--monitoring)
9. [AWS Lambda Functions](#9-aws-lambda-functions)
10. [AWS Infrastructure](#10-aws-infrastructure)
11. [Error Handling](#11-error-handling)
12. [Testing Requirements](#12-testing-requirements)
13. [CI/CD Pipeline](#13-cicd-pipeline)
14. [Environment Variables](#14-environment-variables)
15. [Prompting Guide](#15-prompting-guide)

---

## 1. Project Overview

FinTrack is a personal finance management REST API. Authenticated users can manage bank accounts, log income and expense transactions, set monthly budgets per category, and request PDF statements. The backend exposes a versioned JSON REST API consumed by the Angular frontend. Heavy async tasks (PDF generation, email alerts) are offloaded to AWS Lambda.

**Business rules:**
- A user owns one or more **Accounts** (e.g. Cheque, Savings, Wallet).
- Every **Transaction** belongs to an Account and a Category.
- A **Budget** sets a monthly spending limit for a Category.
- A **Report** request triggers a Lambda that generates a PDF and stores it in S3; the API returns a pre-signed download URL.
- Budget utilisation is calculated live from transactions — no stored percentage column.

---

## 2. Tech Stack & Versions

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.x |
| Build | Maven | 3.9.x |
| Database | MySQL | 8.0 |
| DB Migrations | Flyway | 10.x |
| Auth | Spring Security + JJWT | 0.12.x |
| API Docs | SpringDoc OpenAPI | 2.x |
| Logging | SLF4J + Logback (JSON) | — |
| AWS SDK | AWS SDK for Java v2 | 2.x |
| Testing | JUnit 5 + Mockito | — |
| Containerisation | Docker | — |
| Deployment | AWS Elastic Beanstalk | t3.micro (free tier) |
| Serverless | AWS Lambda | Java 21 or Node 20 |
| Object Storage | AWS S3 | — |
| Email | AWS SES | — |
| Monitoring | AWS CloudWatch | — |

---

## 3. Repository Structure

```
fintrack-api/
├── pom.xml
├── Dockerfile
├── .ebextensions/
│   └── environment.config          # EB environment properties
├── .elasticbeanstalk/
│   └── config.yml                  # EB CLI config
├── src/
│   ├── main/
│   │   ├── java/com/fintrack/
│   │   │   ├── FinTrackApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── AppConfig.java
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── RefreshTokenService.java
│   │   │   │   ├── RefreshTokenEntity.java
│   │   │   │   └── dto/
│   │   │   ├── user/
│   │   │   │   ├── UserEntity.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── dto/
│   │   │   ├── account/
│   │   │   ├── transaction/
│   │   │   ├── category/
│   │   │   ├── budget/
│   │   │   ├── report/
│   │   │   └── common/
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   └── BusinessException.java
│   │   │       ├── logging/
│   │   │       │   └── RequestLoggingAspect.java
│   │   │       ├── dto/
│   │   │       │   ├── ApiResponse.java
│   │   │       │   └── PagedResponse.java
│   │   │       └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/migration/
│   │           ├── V1__create_users.sql
│   │           ├── V2__create_accounts.sql
│   │           ├── V3__create_categories.sql
│   │           ├── V4__create_transactions.sql
│   │           ├── V5__create_budgets.sql
│   │           ├── V6__create_reports.sql
│   │           ├── V7__create_refresh_tokens.sql
│   │           └── V8__seed_categories.sql
│   └── test/
│       └── java/com/fintrack/
│           ├── auth/
│           ├── account/
│           ├── transaction/
│           └── budget/
```

---

## 4. Database Schema

All tables use `BIGINT AUTO_INCREMENT` primary keys, `created_at` and `updated_at` timestamps managed by Spring Auditing, and soft deletes via `deleted_at` (nullable).

### 4.1 users
```sql
id            BIGINT AUTO_INCREMENT PRIMARY KEY,
email         VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
full_name     VARCHAR(255) NOT NULL,
role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
created_at    DATETIME NOT NULL,
updated_at    DATETIME NOT NULL,
deleted_at    DATETIME NULL
```

### 4.2 refresh_tokens
```sql
id          BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id     BIGINT NOT NULL REFERENCES users(id),
token       VARCHAR(512) NOT NULL UNIQUE,
expires_at  DATETIME NOT NULL,
revoked     BOOLEAN NOT NULL DEFAULT FALSE,
created_at  DATETIME NOT NULL
```

### 4.3 accounts
```sql
id           BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id      BIGINT NOT NULL REFERENCES users(id),
name         VARCHAR(100) NOT NULL,
type         ENUM('CHEQUE','SAVINGS','WALLET','CREDIT') NOT NULL,
currency     VARCHAR(3) NOT NULL DEFAULT 'ZAR',
balance      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
created_at   DATETIME NOT NULL,
updated_at   DATETIME NOT NULL,
deleted_at   DATETIME NULL
```

### 4.4 categories
```sql
id          BIGINT AUTO_INCREMENT PRIMARY KEY,
name        VARCHAR(100) NOT NULL,
icon        VARCHAR(50) NOT NULL,
type        ENUM('INCOME','EXPENSE','BOTH') NOT NULL,
is_system   BOOLEAN NOT NULL DEFAULT TRUE,
user_id     BIGINT NULL REFERENCES users(id),
created_at  DATETIME NOT NULL
```

### 4.5 transactions
```sql
id             BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id        BIGINT NOT NULL REFERENCES users(id),
account_id     BIGINT NOT NULL REFERENCES accounts(id),
category_id    BIGINT NOT NULL REFERENCES categories(id),
type           ENUM('INCOME','EXPENSE') NOT NULL,
amount         DECIMAL(15,2) NOT NULL,
description    VARCHAR(255) NULL,
reference      VARCHAR(100) NULL,
transaction_date DATE NOT NULL,
created_at     DATETIME NOT NULL,
updated_at     DATETIME NOT NULL,
deleted_at     DATETIME NULL
```

### 4.6 budgets
```sql
id           BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id      BIGINT NOT NULL REFERENCES users(id),
category_id  BIGINT NOT NULL REFERENCES categories(id),
month        DATE NOT NULL,              -- stored as first day of month
limit_amount DECIMAL(15,2) NOT NULL,
created_at   DATETIME NOT NULL,
updated_at   DATETIME NOT NULL,
UNIQUE KEY uq_budget (user_id, category_id, month)
```

### 4.7 reports
```sql
id           BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id      BIGINT NOT NULL REFERENCES users(id),
type         ENUM('MONTHLY_STATEMENT','ANNUAL_SUMMARY') NOT NULL,
status       ENUM('PENDING','PROCESSING','COMPLETE','FAILED') NOT NULL DEFAULT 'PENDING',
period_start DATE NOT NULL,
period_end   DATE NOT NULL,
s3_key       VARCHAR(512) NULL,
error_msg    VARCHAR(255) NULL,
created_at   DATETIME NOT NULL,
updated_at   DATETIME NOT NULL
```

---

## 5. Flyway Migrations

- Migration files live in `src/main/resources/db/migration/`.
- Naming convention: `V{n}__{description}.sql` — double underscore.
- Never modify an already-applied migration; create a new version instead.
- V8 seeds the following system categories: `Salary`, `Freelance`, `Business Income`, `Groceries`, `Rent`, `Transport`, `Utilities`, `Entertainment`, `Healthcare`, `Education`, `Savings`, `Clothing`, `Restaurants`, `Other Income`, `Other Expense`.

---

## 6. API Modules & Endpoints

**Base path:** `/api/v1`  
**Auth header:** `Authorization: Bearer <access_token>` (required on all endpoints except auth)  
**All responses wrapped in:**
```json
{
  "success": true,
  "message": "OK",
  "data": { }
}
```
**Paginated list responses wrapped in:**
```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

### 6.1 Auth — `/api/v1/auth`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/register` | Public | Create account |
| POST | `/login` | Public | Returns access + refresh token |
| POST | `/refresh` | Public | Exchange refresh token for new access token |
| POST | `/logout` | User | Revoke refresh token |

**POST /register — Request:**
```json
{
  "fullName": "Musa Maluleke",
  "email": "musa@example.com",
  "password": "Min8Chars1!"
}
```
**POST /login — Response data:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "fullName": "Musa Maluleke",
    "email": "musa@example.com",
    "role": "USER"
  }
}
```

**Rules:**
- Passwords hashed with BCrypt (strength 12).
- Access token TTL: 15 minutes. Refresh token TTL: 7 days.
- On logout, set `refresh_tokens.revoked = true`.
- On refresh, validate token is not expired and not revoked; issue a new pair and revoke the old.

---

### 6.2 User — `/api/v1/users`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/me` | User | Get own profile |
| PUT | `/me` | User | Update full name |
| PUT | `/me/password` | User | Change password (requires current password) |
| DELETE | `/me` | User | Soft-delete own account |

---

### 6.3 Accounts — `/api/v1/accounts`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | User | List all accounts with live balance |
| POST | `/` | User | Create account |
| GET | `/{id}` | User | Get single account |
| PUT | `/{id}` | User | Update name or type |
| DELETE | `/{id}` | User | Soft-delete account |

**POST / — Request:**
```json
{
  "name": "FNB Cheque",
  "type": "CHEQUE",
  "currency": "ZAR",
  "initialBalance": 5000.00
}
```

**Rules:**
- `balance` is maintained by incrementing/decrementing on each transaction save/update/delete.
- Users can only access their own accounts — enforce with `user_id = currentUser.id` on every query.

---

### 6.4 Categories — `/api/v1/categories`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | User | List all — system categories + own custom ones |
| POST | `/` | User | Create custom category |
| PUT | `/{id}` | User | Update own custom category only |
| DELETE | `/{id}` | User | Delete own custom category only |

**Rules:**
- System categories (`is_system = true`) cannot be modified or deleted by any user.
- `type` filters: `?type=INCOME`, `?type=EXPENSE`, `?type=BOTH`.

---

### 6.5 Transactions — `/api/v1/transactions`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | User | Paginated list with filters |
| POST | `/` | User | Create transaction |
| GET | `/{id}` | User | Get single |
| PUT | `/{id}` | User | Update transaction |
| DELETE | `/{id}` | User | Soft-delete transaction |
| GET | `/summary` | User | Aggregated totals for dashboard |

**GET / — Query params:**
```
?page=0&size=20
&accountId=1
&categoryId=2
&type=EXPENSE
&startDate=2025-01-01
&endDate=2025-03-31
&search=groceries          ← matches description or reference
&sort=transaction_date,desc
```

**POST / — Request:**
```json
{
  "accountId": 1,
  "categoryId": 5,
  "type": "EXPENSE",
  "amount": 850.00,
  "description": "Weekly groceries",
  "reference": "POS-00123",
  "transactionDate": "2025-04-10"
}
```

**GET /summary — Response data:**
```json
{
  "totalIncome": 25000.00,
  "totalExpenses": 14320.00,
  "netBalance": 10680.00,
  "expenseByCategory": [
    { "categoryId": 5, "categoryName": "Groceries", "total": 3200.00, "percentage": 22.3 }
  ],
  "period": { "start": "2025-04-01", "end": "2025-04-30" }
}
```
Summary accepts `?startDate=&endDate=` query params. Defaults to current month.

**Rules:**
- On transaction create/update, recalculate and persist `accounts.balance`.
- On transaction delete, reverse the balance effect before soft-deleting.
- Amount must be > 0. Type must match category type (or category type = BOTH).

---

### 6.6 Budgets — `/api/v1/budgets`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | User | List budgets for a given month with live utilisation |
| POST | `/` | User | Create or upsert budget |
| PUT | `/{id}` | User | Update limit amount |
| DELETE | `/{id}` | User | Delete budget |

**GET / — Query params:**
```
?month=2025-04        ← year-month format, defaults to current month
```

**Response item:**
```json
{
  "id": 1,
  "categoryId": 5,
  "categoryName": "Groceries",
  "month": "2025-04",
  "limitAmount": 3000.00,
  "spentAmount": 1850.00,
  "remainingAmount": 1150.00,
  "utilisationPercent": 61.7,
  "status": "ON_TRACK"
}
```

**Budget status logic:**
- `ON_TRACK` — utilisation < 75%
- `WARNING` — utilisation >= 75% and < 100%
- `EXCEEDED` — utilisation >= 100%

**Rules:**
- `spentAmount` is computed live from transactions (not stored).
- One budget per user + category + month — upsert on POST.

---

### 6.7 Reports — `/api/v1/reports`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | User | List user's report history |
| POST | `/` | User | Request a new report — triggers Lambda async |
| GET | `/{id}` | User | Poll report status |
| GET | `/{id}/download` | User | Get pre-signed S3 URL (only if status = COMPLETE) |

**POST / — Request:**
```json
{
  "type": "MONTHLY_STATEMENT",
  "periodStart": "2025-04-01",
  "periodEnd": "2025-04-30"
}
```

**POST / — Response data:**
```json
{
  "reportId": 12,
  "status": "PENDING",
  "message": "Report is being generated. Poll GET /reports/12 for status."
}
```

**Flow:**
1. API saves a report row with `status = PENDING`.
2. API publishes a message to an SQS queue (or invokes Lambda directly via SDK).
3. Lambda picks up the job, generates a PDF, uploads to S3, updates the report row to `COMPLETE` with the `s3_key`.
4. Frontend polls `GET /reports/{id}` every 3 seconds until status is `COMPLETE`.
5. `GET /reports/{id}/download` generates and returns a pre-signed S3 URL (TTL 5 minutes).

---

## 7. Authentication & Security

### 7.1 JWT Configuration
- **Algorithm:** HS256
- **Access token claims:** `sub` (user email), `userId`, `role`, `iat`, `exp`
- **Access token TTL:** 15 minutes
- **Refresh token TTL:** 7 days (stored in DB, not just stateless)
- **Secret:** injected from environment variable `JWT_SECRET` (min 256-bit key)

### 7.2 Spring Security Filter Chain
```
Request → JwtAuthFilter → UsernamePasswordAuthenticationFilter → Controller
```
- `JwtAuthFilter` extends `OncePerRequestFilter`.
- Extracts token from `Authorization: Bearer <token>` header.
- Validates signature, expiry, and that the user still exists and is not deleted.
- Sets `SecurityContextHolder` on success.

**Public endpoints (permit all):**
```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET  /swagger-ui/**
GET  /v3/api-docs/**
GET  /actuator/health
```

### 7.3 Password Policy
- Minimum 8 characters, at least one uppercase, one digit, one special character.
- Validated via custom `@ValidPassword` annotation + `ConstraintValidator`.
- Stored as BCrypt hash, strength 12.

### 7.4 CORS Configuration
```java
allowedOrigins: ${ALLOWED_ORIGINS}   // injected from env
allowedMethods: GET, POST, PUT, DELETE, OPTIONS
allowedHeaders: *
allowCredentials: true
maxAge: 3600
```

### 7.5 Additional Security Headers
Configure via Spring Security's `headers()` DSL:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000`

### 7.6 Rate Limiting
Use Bucket4j in-memory rate limiting on the auth endpoints:
- `/auth/login` — max 10 requests per minute per IP
- `/auth/register` — max 5 requests per minute per IP
- All other endpoints — max 100 requests per minute per user

### 7.7 Input Validation
- All request DTOs annotated with Bean Validation (`@NotBlank`, `@Email`, `@DecimalMin`, etc.).
- `@Validated` on all controllers.
- Validation errors returned as structured 400 responses (see Error Handling).

---

## 8. Logging & Monitoring

### 8.1 Structured JSON Logging
Configure `logback-spring.xml` to output JSON in production profile:
```json
{
  "timestamp": "2025-04-10T09:23:11.432Z",
  "level": "INFO",
  "logger": "com.fintrack.transaction.TransactionService",
  "message": "Transaction created",
  "traceId": "abc123",
  "userId": 42,
  "duration_ms": 18
}
```
Include `traceId` (generate per-request UUID in a filter, store in `MDC`).

### 8.2 Request / Response Logging (AOP)
Create `RequestLoggingAspect` using `@Around` on all `@RestController` methods:
- Log method name, endpoint, HTTP method, userId, and duration in milliseconds.
- Do NOT log request/response bodies (to avoid logging sensitive data like passwords).
- Log at `INFO` for < 500ms, `WARN` for >= 500ms, `ERROR` for exceptions.

### 8.3 CloudWatch Integration
- Elastic Beanstalk ships `stdout`/`stderr` to CloudWatch Logs automatically.
- Log group: `/fintrack/api/{environment}`
- Lambda logs ship to their own group: `/aws/lambda/fintrack-pdf-generator` etc.

### 8.4 Spring Actuator Endpoints
Expose only:
```yaml
management.endpoints.web.exposure.include: health,info,metrics
management.endpoint.health.show-details: when-authorized
```

---

## 9. AWS Lambda Functions

### 9.1 fintrack-pdf-generator

**Runtime:** Java 21 (or Node 20)  
**Trigger:** Invoked directly via AWS SDK from ReportService, OR via SQS queue  
**Memory:** 512 MB | **Timeout:** 60 seconds

**Input event:**
```json
{
  "reportId": 12,
  "userId": 42,
  "type": "MONTHLY_STATEMENT",
  "periodStart": "2025-04-01",
  "periodEnd": "2025-04-30",
  "callbackUrl": "https://api.fintrack.com/api/v1/internal/reports/12/complete"
}
```

**Behaviour:**
1. Query MySQL RDS for the user's transactions in the period.
2. Generate a PDF using iText or Apache PDFBox.
3. Upload the PDF to S3 bucket `fintrack-reports` under key `reports/{userId}/{reportId}.pdf`.
4. Call the internal callback URL (or update RDS directly) to set `status = COMPLETE` and `s3_key`.
5. On failure, set `status = FAILED` with `error_msg`.

**PDF contents:**
- FinTrack logo / header
- User name, period
- Summary table: total income, total expenses, net
- Transactions table: date, description, category, amount
- Budget vs actual bar summary per category

---

### 9.2 fintrack-budget-alert

**Runtime:** Node 20  
**Trigger:** CloudWatch EventBridge rule — runs daily at 08:00 SAST (06:00 UTC)  
**Memory:** 256 MB | **Timeout:** 30 seconds

**Behaviour:**
1. Query all active budgets where `utilisationPercent >= 80`.
2. For each, send an email via AWS SES to the budget owner.
3. Email subject: `FinTrack Budget Alert — {categoryName} is {utilisationPercent}% used`
4. Log all sent alerts to CloudWatch.

**Environment variables:**
```
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
SES_FROM_ADDRESS
```

---

## 10. AWS Infrastructure

### 10.1 Elastic Beanstalk
- **Platform:** Docker or Java 21 Corretto
- **Environment type:** Single instance (free tier)
- **Instance type:** t3.micro
- **Health check path:** `/actuator/health`
- **Environment variables:** injected via EB console or `eb setenv`

**.ebextensions/environment.config:**
```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SERVER_PORT: 5000
    SPRING_PROFILES_ACTIVE: prod
```

### 10.2 RDS MySQL
- **Engine:** MySQL 8.0
- **Instance:** db.t3.micro (free tier)
- **Storage:** 20 GB gp2
- **Multi-AZ:** false (free tier)
- **Publicly accessible:** false — accessible only from EB security group

### 10.3 S3
- **Bucket:** `fintrack-reports-{accountId}` (unique name)
- **Access:** private — no public access
- **Pre-signed URL TTL:** 5 minutes

### 10.4 IAM Roles
- **EB instance role** — policies: `AmazonRDSFullAccess`, `AmazonS3FullAccess`, `AmazonSQSFullAccess`, `AWSLambdaRole`, `CloudWatchLogsFullAccess`
- **Lambda execution role** — policies: `AmazonRDSDataFullAccess`, `AmazonS3FullAccess`, `AmazonSESFullAccess`, `CloudWatchLogsFullAccess`

### 10.5 Dockerfile (fintrack-api)
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 11. Error Handling

### 11.1 Global Exception Handler
Create `GlobalExceptionHandler` annotated with `@RestControllerAdvice`.

**Standard error response shape:**
```json
{
  "success": false,
  "message": "Resource not found",
  "errors": null,
  "timestamp": "2025-04-10T09:23:11Z",
  "path": "/api/v1/accounts/99"
}
```

**Validation error response shape (400):**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "must be a valid email address",
    "password": "must be at least 8 characters"
  },
  "timestamp": "2025-04-10T09:23:11Z",
  "path": "/api/v1/auth/register"
}
```

### 11.2 HTTP Status Mapping

| Exception | Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `BusinessException` | 422 |
| `MethodArgumentNotValidException` | 400 |
| `AccessDeniedException` | 403 |
| `AuthenticationException` | 401 |
| `HttpMessageNotReadableException` | 400 |
| `Exception` (fallback) | 500 |

---

## 12. Testing Requirements

- **Minimum coverage:** 70% on all `*Service.java` classes.
- **Unit tests:** JUnit 5 + Mockito — mock all repository and external dependencies.
- **Integration tests:** Use `@SpringBootTest` with H2 in-memory database for slice tests on `*Controller.java`.
- **Test naming:** `methodName_scenario_expectedBehaviour()` e.g. `createTransaction_withInvalidAmount_returns400()`

**Required test classes:**
```
AuthServiceTest
JwtServiceTest
AccountServiceTest
TransactionServiceTest
BudgetServiceTest
ReportServiceTest
AuthControllerIntegrationTest
TransactionControllerIntegrationTest
```

---

## 13. CI/CD Pipeline

Pipeline file: `.github/workflows/backend-deploy.yml`

### Trigger
```yaml
on:
  push:
    branches: [main]
    paths: ['fintrack-api/**']
```

### Jobs

#### Job 1 — test
```
1. Checkout code
2. Set up Java 21
3. Cache Maven dependencies
4. Run: mvn verify
5. Upload test results as artifact
```

#### Job 2 — build (needs: test)
```
1. Run: mvn package -DskipTests
2. Build Docker image: fintrack-api:${{ github.sha }}
3. Push to AWS ECR
```

#### Job 3 — deploy (needs: build)
```
1. Configure AWS credentials from GitHub secrets
2. Generate new EB application version from ECR image
3. Deploy to Elastic Beanstalk environment
4. Wait for environment health to turn Green
5. Run smoke test: curl GET /actuator/health → assert 200
```

#### Job 4 — deploy-lambdas (needs: test, runs in parallel with build)
```
1. Package fintrack-pdf-generator
2. Deploy to Lambda via AWS CLI: aws lambda update-function-code
3. Package fintrack-budget-alert
4. Deploy to Lambda via AWS CLI
```

### Required GitHub Secrets
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
ECR_REPOSITORY
EB_APPLICATION_NAME
EB_ENVIRONMENT_NAME
```

---

## 14. Environment Variables

### Spring Boot (`application-prod.yml` reads from env)

| Variable | Description |
|---|---|
| `DB_HOST` | RDS endpoint |
| `DB_PORT` | 3306 |
| `DB_NAME` | fintrack |
| `DB_USER` | fintrack_user |
| `DB_PASSWORD` | RDS password |
| `JWT_SECRET` | Min 256-bit base64 secret |
| `JWT_ACCESS_TTL_MINUTES` | 15 |
| `JWT_REFRESH_TTL_DAYS` | 7 |
| `ALLOWED_ORIGINS` | Frontend URL (e.g. https://fintrack.s3-website.amazonaws.com) |
| `AWS_REGION` | e.g. af-south-1 |
| `AWS_S3_BUCKET` | fintrack-reports-{id} |
| `AWS_LAMBDA_PDF_FUNCTION` | fintrack-pdf-generator |
| `SES_FROM_ADDRESS` | noreply@yourdomain.com |
| `SPRING_PROFILES_ACTIVE` | prod |

---

## 15. Prompting Guide

Use the following prompts in sequence to build the backend with an AI coding agent. Run them one section at a time. Review and test before moving to the next.

---

**Prompt 1 — Project scaffold**
```
Create a Spring Boot 3.3 project using Java 21 and Maven with the following dependencies:
Spring Web, Spring Security, Spring Data JPA, Spring Validation, Spring Actuator,
MySQL Driver, Flyway, JJWT (0.12.x), SpringDoc OpenAPI 2.x, Lombok, Bucket4j.
Use the package structure defined in BACKEND_REQUIREMENTS.md section 3.
Create application.yml, application-dev.yml, and application-prod.yml with profiles.
Dev profile uses H2 in-memory. Prod profile reads all values from environment variables
as defined in section 14. Do not hardcode any credentials.
```

---

**Prompt 2 — Flyway migrations**
```
Create all Flyway SQL migration files V1 through V8 as defined in
BACKEND_REQUIREMENTS.md sections 4 and 5. Place them in
src/main/resources/db/migration/. Use MySQL 8 syntax. V8 seeds the system
categories listed in section 5. All tables must include created_at and updated_at
DATETIME columns. Use ENUM types exactly as specified.
```

---

**Prompt 3 — Common layer**
```
Create the common layer for the FinTrack API:
1. ApiResponse<T> generic wrapper with fields: success, message, data, timestamp, path.
2. PagedResponse<T> wrapper with fields: content, page, size, totalElements, totalPages.
3. ResourceNotFoundException extending RuntimeException.
4. BusinessException extending RuntimeException.
5. GlobalExceptionHandler using @RestControllerAdvice mapping all exceptions to the
   HTTP status codes and response shapes defined in BACKEND_REQUIREMENTS.md section 11.
6. RequestLoggingAspect using Spring AOP @Around on all @RestController methods,
   logging method, endpoint, userId, and duration_ms. Log WARN if > 500ms.
   Do not log request or response bodies.
7. MDC filter to generate a UUID traceId per request and include it in all log output.
Configure logback-spring.xml for JSON structured logging in the prod profile.
```

---

**Prompt 4 — Auth module**
```
Implement the full authentication module for FinTrack as defined in
BACKEND_REQUIREMENTS.md section 7. Include:
1. UserEntity and UserRepository (Spring Data JPA).
2. JwtService: generate access token (15 min) and refresh token (7 days) using JJWT.
   Read JWT_SECRET from environment. Include userId and role as custom claims.
3. JwtAuthFilter extending OncePerRequestFilter.
4. RefreshTokenEntity, RefreshTokenRepository, RefreshTokenService (create, validate, revoke).
5. AuthService with register (BCrypt strength 12), login, refresh, logout logic.
6. AuthController with endpoints: POST /api/v1/auth/register, /login, /refresh, /logout.
7. SecurityConfig: permit public endpoints listed in section 7.2, require auth on all others.
   Add security headers from section 7.5.
8. @ValidPassword custom annotation for password policy in section 7.3.
9. Rate limiting via Bucket4j on /auth/login (10/min) and /auth/register (5/min) per IP.
Use the request/response DTOs and response shapes defined in section 6.1.
```

---

**Prompt 5 — Account, Category, Transaction modules**
```
Implement the Account, Category, and Transaction modules for FinTrack using
BACKEND_REQUIREMENTS.md sections 6.3, 6.4, and 6.5 as the specification.
For each module create: Entity, Repository, Service, Controller, and all DTOs.
Rules to enforce:
- Users can only access their own data. Enforce this at the service layer using
  the authenticated user's ID from the SecurityContext.
- Account balance must be recalculated on every transaction create, update, and delete.
- Transaction type must be compatible with the category type.
- GET /transactions supports all query params: page, size, accountId, categoryId,
  type, startDate, endDate, search, sort — implement using JPA Specifications.
- GET /transactions/summary returns aggregated totals for a date range (default current month).
- Soft deletes: set deleted_at = NOW(). Exclude soft-deleted rows from all queries.
Use the response shapes from section 6 and wrap all responses in ApiResponse<T>.
```

---

**Prompt 6 — Budget module**
```
Implement the Budget module for FinTrack using BACKEND_REQUIREMENTS.md section 6.6.
Create BudgetEntity, BudgetRepository, BudgetService, BudgetController, and DTOs.
Key requirements:
- spentAmount is computed live via a JPQL query summing transactions for the
  budget's user + category + month range. It is NOT stored in the database.
- Budget response must include: limitAmount, spentAmount, remainingAmount,
  utilisationPercent, and status (ON_TRACK / WARNING / EXCEEDED) as per the
  thresholds in section 6.6.
- POST / upserts: if a budget for the same user+category+month already exists, update it.
- GET / filters by ?month=YYYY-MM, defaulting to current month.
```

---

**Prompt 7 — Report module**
```
Implement the Report module for FinTrack using BACKEND_REQUIREMENTS.md section 6.7.
Create: ReportEntity, ReportRepository, ReportService, ReportController, and DTOs.
Flow:
1. POST /api/v1/reports saves a row with status=PENDING, then invokes the Lambda
   function named by env var AWS_LAMBDA_PDF_FUNCTION using AWS SDK v2
   InvokeRequest (InvocationType.EVENT for async). Pass the JSON payload defined
   in section 9.1.
2. GET /api/v1/reports/{id} returns the current report row including status.
3. GET /api/v1/reports/{id}/download generates a pre-signed S3 URL using AWS SDK v2
   S3Presigner with TTL of 5 minutes. Returns 404 if status is not COMPLETE.
4. Create an internal endpoint POST /api/v1/internal/reports/{id}/complete
   (secured with a shared secret header X-Internal-Secret matching env var
   INTERNAL_SECRET) for the Lambda to call back when done. Accepts:
   { "s3Key": "reports/42/12.pdf" } and sets status=COMPLETE.
```

---

**Prompt 8 — Tests**
```
Write unit tests for the following FinTrack service classes using JUnit 5 and Mockito,
following the test naming convention methodName_scenario_expectedBehaviour():
- AuthServiceTest: test register (success, duplicate email), login (success, wrong password),
  refresh (success, expired token, revoked token), logout.
- AccountServiceTest: test create, findById (not found), delete (balance recalculation).
- TransactionServiceTest: test create (success, invalid amount, type mismatch),
  update (balance recalculation), delete (balance reversal).
- BudgetServiceTest: test upsert, getForMonth (status calculation for all three statuses).
Also write integration tests for AuthController and TransactionController using
@SpringBootTest with H2. Test the full HTTP request/response cycle including auth headers.
Minimum 70% line coverage on all service classes.
```

---

**Prompt 9 — Dockerfile and CI/CD**
```
Create the following for FinTrack backend:
1. A multi-stage Dockerfile as defined in BACKEND_REQUIREMENTS.md section 10.5.
2. .ebextensions/environment.config setting SERVER_PORT=5000 and SPRING_PROFILES_ACTIVE=prod.
3. GitHub Actions workflow file at .github/workflows/backend-deploy.yml implementing
   the 4-job pipeline defined in section 13:
   - Job 1: test (mvn verify, upload results)
   - Job 2: build (mvn package, Docker build, push to ECR)
   - Job 3: deploy to Elastic Beanstalk, wait for Green health, smoke test /actuator/health
   - Job 4: deploy both Lambda functions via aws lambda update-function-code
   Use the GitHub secrets listed in section 13. Jobs 2 and 4 both need Job 1 to pass.
   Jobs 3 needs Job 2 to pass.
```

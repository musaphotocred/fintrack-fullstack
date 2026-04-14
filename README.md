# FinTrack — Portfolio Project

> A full-stack personal finance management application built to demonstrate
> enterprise-grade development across the full stack.

---

## Project Architecture

```
fintrack/
├── fintrack-api/          # Spring Boot backend
├── fintrack-ui/           # Angular 19 frontend
├── fintrack-lambdas/      # AWS Lambda functions
│   ├── pdf-generator/
│   └── budget-alert/
├── .github/
│   └── workflows/
│       ├── backend-deploy.yml
│       └── frontend-deploy.yml
├── BACKEND_REQUIREMENTS.md
├── FRONTEND_REQUIREMENTS.md
└── README.md
```

---

## Requirements Documents

| Document | Description |
|---|---|
| [BACKEND_REQUIREMENTS.md](./BACKEND_REQUIREMENTS.md) | Spring Boot API · MySQL · Lambda · Elastic Beanstalk · CI/CD |
| [FRONTEND_REQUIREMENTS.md](./FRONTEND_REQUIREMENTS.md) | Angular 19 · Tailwind CSS · S3/CloudFront · CI/CD |

---

## Tech Stack Summary

| Layer | Technology |
|---|---|
| Frontend | Angular 19, Tailwind CSS v4, TypeScript, RxJS, Chart.js |
| Backend | Spring Boot 3.3, Java 21, Spring Security, JWT, Flyway |
| Database | MySQL 8 on AWS RDS (free tier) |
| Serverless | AWS Lambda (PDF generator + Budget alert) |
| Hosting | AWS Elastic Beanstalk (API) + S3/CloudFront (UI) |
| CI/CD | GitHub Actions |
| Logging | SLF4J/Logback JSON → CloudWatch |
| Auth | JWT (access 15min + refresh 7d), BCrypt, Rate Limiting |

---

## Build Order

Follow this sequence when building with an AI agent:

### Backend (BACKEND_REQUIREMENTS.md)
1. Project scaffold + Maven dependencies
2. Flyway migrations (V1–V8)
3. Common layer (ApiResponse, GlobalExceptionHandler, AOP logging)
4. Auth module (JWT, Spring Security, rate limiting)
5. Account, Category, Transaction modules
6. Budget module
7. Report module + Lambda invocation
8. Unit and integration tests
9. Dockerfile + GitHub Actions CI/CD

### Frontend (FRONTEND_REQUIREMENTS.md)
1. Angular scaffold + Tailwind config
2. Core layer (interceptors, guards, TokenStorage, AuthService)
3. Shared UI components (Shell, Toast, Modal, Spinner, Badge)
4. Auth pages (Login, Register)
5. Dashboard (stat cards, charts, budget preview)
6. Transactions page (table, filters, form modal)
7. Accounts, Budgets, Reports, Settings pages
8. Jest tests
9. Dockerfile + GitHub Actions CI/CD

---

## AWS Free Tier Services Used

| Service | Usage | Free Tier Limit |
|---|---|---|
| Elastic Beanstalk | API hosting on t3.micro | 750 hrs/month |
| RDS MySQL | db.t3.micro | 750 hrs/month + 20GB |
| S3 | Frontend hosting + PDF storage | 5GB + 20k GET requests |
| CloudFront | CDN for frontend | 1TB transfer + 10M requests |
| Lambda | PDF generator + budget alerts | 1M requests/month |
| SES | Budget alert emails | 62k emails/month |
| CloudWatch | Logs + metrics | 5GB ingestion |

---

## Key Portfolio Talking Points

- **Layered architecture** — clean separation of Controller → Service → Repository throughout the Spring Boot API.
- **Security** — JWT with refresh token rotation, BCrypt, rate limiting, CORS, security headers.
- **AOP logging** — cross-cutting request logging without polluting business logic.
- **Async processing** — Lambda-based PDF generation decoupled from the API with status polling.
- **Database migrations** — Flyway versioned SQL migrations, never manual schema changes.
- **Angular Signals** — modern reactive state management without NgRx overhead.
- **Tailwind design system** — consistent, custom UI with zero component library dependency.
- **CI/CD** — full pipeline from `git push` to live deployment on both frontend and backend.
- **AWS free tier** — entire stack runs within free tier limits, demonstrating cost-conscious infrastructure decisions.

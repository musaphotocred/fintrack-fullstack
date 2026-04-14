# FinTrack — Frontend Requirements Specification

> **Stack:** Angular 19 · Tailwind CSS v4 · TypeScript · RxJS  
> **Version:** 1.0 | **Type:** Portfolio Project

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack & Versions](#2-tech-stack--versions)
3. [Repository Structure](#3-repository-structure)
4. [Tailwind Design System](#4-tailwind-design-system)
5. [Application Modules](#5-application-modules)
6. [Pages & Components](#6-pages--components)
7. [State Management](#7-state-management)
8. [API Integration Layer](#8-api-integration-layer)
9. [Authentication & Route Guards](#9-authentication--route-guards)
10. [Forms & Validation](#10-forms--validation)
11. [Error Handling & Feedback](#11-error-handling--feedback)
12. [Routing](#12-routing)
13. [Testing Requirements](#13-testing-requirements)
14. [Build & Deployment](#14-build--deployment)
15. [CI/CD Pipeline](#15-cicd-pipeline)
16. [Prompting Guide](#16-prompting-guide)

---

## 1. Project Overview

FinTrack's Angular frontend is a single-page application that consumes the Spring Boot REST API. Users register, log in, and then manage their financial accounts, transactions, and budgets through a clean dashboard-style UI. The app uses Tailwind CSS for all styling — no component library. All UI is built with custom Tailwind utility classes to demonstrate full CSS competency.

**Key UX flows:**
1. Register / Login → land on Dashboard.
2. Dashboard shows account balances, spending summary chart, budget progress bars, recent transactions.
3. Transactions page — paginated list with filters, inline add/edit, soft delete.
4. Budgets page — monthly budget cards with live utilisation bars.
5. Reports page — request PDF generation, poll status, download when ready.
6. Settings page — update profile, change password.

---

## 2. Tech Stack & Versions

| Layer | Technology | Version |
|---|---|---|
| Framework | Angular | 19.x |
| Language | TypeScript | 5.x |
| Styling | Tailwind CSS | 4.x |
| Reactive | RxJS | 7.x |
| HTTP | Angular HttpClient | built-in |
| Charts | Chart.js via ng2-charts | 6.x |
| Icons | Heroicons (SVG inline) | 2.x |
| Forms | Angular Reactive Forms | built-in |
| Auth storage | localStorage (JWT) | — |
| Build | Angular CLI | 19.x |
| Testing | Jest + Angular Testing Library | — |
| Linting | ESLint + Prettier | — |
| Hosting | AWS S3 + CloudFront | — |

---

## 3. Repository Structure

```
fintrack-ui/
├── angular.json
├── tailwind.config.ts
├── tsconfig.json
├── package.json
├── nginx.conf                    # For Docker / S3 SPA routing
├── .github/
│   └── workflows/
│       └── frontend-deploy.yml
└── src/
    ├── index.html
    ├── main.ts
    ├── styles.css                # Tailwind directives + global overrides
    └── app/
        ├── app.config.ts         # provideRouter, provideHttpClient, provideAnimations
        ├── app.routes.ts         # Root route definitions
        ├── core/
        │   ├── interceptors/
        │   │   ├── auth.interceptor.ts          # Attach Bearer token
        │   │   └── error.interceptor.ts         # Global HTTP error handling
        │   ├── guards/
        │   │   ├── auth.guard.ts                # Redirect to /login if no token
        │   │   └── guest.guard.ts               # Redirect to /dashboard if logged in
        │   ├── services/
        │   │   └── token-storage.service.ts     # localStorage wrapper
        │   └── models/
        │       └── api-response.model.ts
        ├── features/
        │   ├── auth/
        │   │   ├── auth.routes.ts
        │   │   ├── login/
        │   │   │   ├── login.component.ts
        │   │   │   └── login.component.html
        │   │   └── register/
        │   │       ├── register.component.ts
        │   │       └── register.component.html
        │   ├── dashboard/
        │   │   ├── dashboard.routes.ts
        │   │   ├── dashboard.component.ts
        │   │   └── dashboard.component.html
        │   ├── transactions/
        │   │   ├── transactions.routes.ts
        │   │   ├── transactions-list/
        │   │   ├── transaction-form/
        │   │   └── transaction-detail/
        │   ├── accounts/
        │   │   ├── accounts.routes.ts
        │   │   ├── accounts-list/
        │   │   └── account-form/
        │   ├── budgets/
        │   │   ├── budgets.routes.ts
        │   │   └── budgets-list/
        │   ├── reports/
        │   │   ├── reports.routes.ts
        │   │   └── reports-list/
        │   └── settings/
        │       ├── settings.routes.ts
        │       └── settings.component.ts
        └── shared/
            ├── components/
            │   ├── layout/
            │   │   ├── shell.component.ts          # App shell with sidebar + header
            │   │   ├── sidebar.component.ts
            │   │   └── topbar.component.ts
            │   ├── ui/
            │   │   ├── button.component.ts
            │   │   ├── input.component.ts
            │   │   ├── badge.component.ts
            │   │   ├── spinner.component.ts
            │   │   ├── modal.component.ts
            │   │   ├── toast.component.ts
            │   │   └── empty-state.component.ts
            │   └── charts/
            │       ├── donut-chart.component.ts
            │       └── bar-chart.component.ts
            ├── directives/
            │   └── click-outside.directive.ts
            ├── pipes/
            │   ├── currency-zar.pipe.ts
            │   └── relative-date.pipe.ts
            └── services/
                ├── auth.service.ts
                ├── account.service.ts
                ├── transaction.service.ts
                ├── category.service.ts
                ├── budget.service.ts
                ├── report.service.ts
                └── toast.service.ts
```

---

## 4. Tailwind Design System

**No external component libraries.** All UI must be built with Tailwind utility classes directly in templates.

### 4.1 tailwind.config.ts
```typescript
import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#EFF6FF',
          100: '#DBEAFE',
          500: '#3B82F6',
          600: '#2563EB',
          700: '#1D4ED8',
          900: '#1A2B4A',
        },
        success: { 50: '#F0FDF4', 500: '#22C55E', 700: '#15803D' },
        warning: { 50: '#FFFBEB', 500: '#F59E0B', 700: '#B45309' },
        danger:  { 50: '#FEF2F2', 500: '#EF4444', 700: '#B91C1C' },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      borderRadius: {
        DEFAULT: '0.5rem',
        lg: '0.75rem',
        xl: '1rem',
      },
      boxShadow: {
        card: '0 1px 3px 0 rgb(0 0 0 / 0.07), 0 1px 2px -1px rgb(0 0 0 / 0.07)',
        dropdown: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
      },
    },
  },
  plugins: [],
};

export default config;
```

### 4.2 Core Component Patterns

These Tailwind class combinations must be used consistently across the entire app.

**Card container:**
```html
<div class="bg-white rounded-xl shadow-card border border-gray-100 p-6">
```

**Primary button:**
```html
<button class="inline-flex items-center gap-2 px-4 py-2 bg-brand-600 text-white text-sm font-medium rounded-lg hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
```

**Secondary button:**
```html
<button class="inline-flex items-center gap-2 px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2 transition-colors">
```

**Danger button:**
```html
<button class="inline-flex items-center gap-2 px-4 py-2 bg-danger-500 text-white text-sm font-medium rounded-lg hover:bg-danger-700 focus:outline-none focus:ring-2 focus:ring-danger-500 focus:ring-offset-2 transition-colors">
```

**Text input:**
```html
<input class="block w-full px-3 py-2 text-sm text-gray-900 bg-white border border-gray-300 rounded-lg placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition">
```

**Invalid input state:**
```html
<input class="... border-danger-500 focus:ring-danger-500">
<p class="mt-1 text-xs text-danger-500">Error message here</p>
```

**Form label:**
```html
<label class="block text-sm font-medium text-gray-700 mb-1">
```

**Select dropdown:**
```html
<select class="block w-full px-3 py-2 text-sm text-gray-900 bg-white border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition">
```

**Badge — status variants:**
```html
<!-- Green -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-success-50 text-success-700">Active</span>
<!-- Amber -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-warning-50 text-warning-700">Warning</span>
<!-- Red -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-danger-50 text-danger-700">Exceeded</span>
<!-- Gray -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-700">Pending</span>
```

**Table:**
```html
<div class="overflow-x-auto rounded-xl border border-gray-200">
  <table class="min-w-full divide-y divide-gray-200">
    <thead class="bg-gray-50">
      <tr>
        <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Column</th>
      </tr>
    </thead>
    <tbody class="bg-white divide-y divide-gray-100">
      <tr class="hover:bg-gray-50 transition-colors">
        <td class="px-4 py-3 text-sm text-gray-700">Value</td>
      </tr>
    </tbody>
  </table>
</div>
```

**Page header:**
```html
<div class="mb-6 flex items-center justify-between">
  <div>
    <h1 class="text-2xl font-bold text-gray-900">Page Title</h1>
    <p class="mt-1 text-sm text-gray-500">Subtitle or description</p>
  </div>
  <button class="...primary button...">Action</button>
</div>
```

**Sidebar nav item (active):**
```html
<a class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium bg-brand-50 text-brand-700">
```

**Sidebar nav item (inactive):**
```html
<a class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-100 hover:text-gray-900 transition-colors">
```

**Toast notification:**
```html
<!-- Success -->
<div class="fixed bottom-4 right-4 z-50 flex items-center gap-3 px-4 py-3 bg-white rounded-xl shadow-dropdown border-l-4 border-success-500 min-w-[280px]">
<!-- Error -->
<div class="... border-l-4 border-danger-500 ...">
```

**Modal overlay:**
```html
<div class="fixed inset-0 z-40 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4">
  <div class="bg-white rounded-xl shadow-dropdown w-full max-w-lg p-6">
```

**Progress bar (budget utilisation):**
```html
<div class="w-full bg-gray-100 rounded-full h-2">
  <div class="h-2 rounded-full transition-all duration-500"
       [style.width.%]="utilisationPercent"
       [class]="utilisationPercent >= 100 ? 'bg-danger-500' : utilisationPercent >= 75 ? 'bg-warning-500' : 'bg-success-500'">
  </div>
</div>
```

### 4.3 Typography Scale

| Use | Classes |
|---|---|
| Page title | `text-2xl font-bold text-gray-900` |
| Section heading | `text-lg font-semibold text-gray-900` |
| Card title | `text-base font-semibold text-gray-800` |
| Body text | `text-sm text-gray-700` |
| Muted text | `text-sm text-gray-500` |
| Caption | `text-xs text-gray-400` |
| Amount — positive | `text-base font-semibold text-success-700` |
| Amount — negative | `text-base font-semibold text-danger-700` |
| Amount — neutral | `text-base font-semibold text-gray-900` |

---

## 5. Application Modules

All feature modules are **lazy-loaded** via the Angular Router. No eagerly loaded feature modules.

| Module | Route | Guard |
|---|---|---|
| Auth | `/auth/login`, `/auth/register` | `GuestGuard` |
| Dashboard | `/dashboard` | `AuthGuard` |
| Transactions | `/transactions` | `AuthGuard` |
| Accounts | `/accounts` | `AuthGuard` |
| Budgets | `/budgets` | `AuthGuard` |
| Reports | `/reports` | `AuthGuard` |
| Settings | `/settings` | `AuthGuard` |

The root `/` redirects to `/dashboard`.

---

## 6. Pages & Components

### 6.1 App Shell (always visible after login)

**ShellComponent** wraps all authenticated pages with:
- **Sidebar** (fixed left, 240px wide on desktop)
  - FinTrack logo at top
  - Nav links: Dashboard, Transactions, Accounts, Budgets, Reports, Settings
  - User avatar + name + logout button at bottom
  - Collapses to icon-only on screens < `lg` breakpoint
- **Topbar** (top bar with page title and mobile hamburger)
- **Main content area** with `<router-outlet>`

Sidebar is hidden and replaced with a bottom drawer on mobile (`< md`).

---

### 6.2 Auth Pages

#### LoginComponent
- Full-page centred layout with FinTrack logo above the card.
- Reactive form: email (required, valid email), password (required).
- Primary submit button "Sign in" — shows spinner while loading.
- Error banner below form if credentials invalid: `"Invalid email or password"`.
- Link to Register page.
- On success: store tokens, navigate to `/dashboard`.

#### RegisterComponent
- Same layout as Login.
- Reactive form: fullName (required, min 2 chars), email (required, valid email), password (required, min 8 chars, show strength indicator), confirmPassword (must match password).
- **Password strength indicator:** small bar below password input coloured red/amber/green based on strength score (length + uppercase + digit + special).
- On success: auto-login with returned tokens, navigate to `/dashboard`.

---

### 6.3 Dashboard

**Layout:** 2-column grid on desktop, single column on mobile.

**Row 1 — Summary stat cards (4 cards in a row):**

| Card | Value | Colour accent |
|---|---|---|
| Total Balance | Sum of all account balances | Brand blue |
| Monthly Income | Sum INCOME transactions this month | Green |
| Monthly Expenses | Sum EXPENSE transactions this month | Red |
| Net This Month | Income − Expenses | Dynamic (green if positive, red if negative) |

Each card:
```
[Icon]  Label
        R 12,500.00
        ↑ 8.2% vs last month  ← optional, calculate if possible
```

**Row 2 — Charts:**
- **Expenses by Category** — Donut chart (Chart.js) showing category breakdown for current month. Clicking a segment navigates to Transactions filtered by that category.
- **Income vs Expenses** — Bar chart showing last 6 months with two bars per month (income = blue, expenses = red).

**Row 3 — Two columns:**
- **Budget Progress** (left) — list of current month budgets showing name, progress bar, `R spent / R limit`. "Manage Budgets →" link.
- **Recent Transactions** (right) — last 5 transactions with category icon, description, date, amount. "View All →" link.

---

### 6.4 Accounts Page

**AccountsListComponent:**
- Grid of account cards (2 per row on desktop, 1 on mobile).
- Each card shows: account name, type badge, currency, balance (large, coloured).
- "Add Account" button → opens `AccountFormComponent` as a modal.
- Each card has an overflow menu (3-dot) with: Edit, Delete.
- Delete shows a confirmation modal before calling API.

**AccountFormComponent (modal):**
- Reactive form: name (required), type (select: CHEQUE/SAVINGS/WALLET/CREDIT), currency (default ZAR), initialBalance (number, min 0).
- Used for both create and edit. Edit pre-populates fields.

---

### 6.5 Transactions Page

**TransactionsListComponent:**

**Filter bar (top):**
- Date range picker (two date inputs: From / To, defaults to current month)
- Account dropdown filter
- Category dropdown filter
- Type toggle (All / Income / Expense)
- Search input (debounced 300ms)
- "Add Transaction" button

**Transaction table columns:**
```
Date | Description | Category | Account | Type | Amount | Actions
```
- Type badge: green "Income" / red "Expense".
- Amount: green for income, red for expense.
- Actions: Edit (pencil icon) / Delete (trash icon).
- Table is paginated: show 20 rows, pagination controls at bottom.
- Clicking a row opens the edit modal.

**TransactionFormComponent (modal):**
- Fields: account (select), category (select, filtered by type), type (toggle Income/Expense), amount (number, min 0.01), description (text), reference (text, optional), transactionDate (date picker).
- Category options reload when type toggle changes.
- On save, refresh the transaction list and update account balances in state.

---

### 6.6 Budgets Page

**BudgetsListComponent:**

**Month selector** at top — `< April 2025 >` — navigating months updates the API query.

**Budget cards grid (2 per row on desktop):**

Each card:
```
[Category icon]  Category name        [Status badge]
R 1,850 / R 3,000
[████████░░░░░░░░░░]  61.7%

Remaining: R 1,150
```
- Progress bar colours: green < 75%, amber 75–99%, red ≥ 100%.
- Status badge: ON TRACK / WARNING / EXCEEDED.
- "Edit limit" icon → inline edit of limit amount.
- Delete (trash icon) → confirm then delete.

**"Add Budget" button** → modal with category (select, only shows EXPENSE categories without an existing budget for the month) and limit amount.

**Empty state:** if no budgets set for the month, show a centred illustration-style empty state with "No budgets set for this month. Add one to start tracking your spending." and the Add Budget button.

---

### 6.7 Reports Page

**ReportsListComponent:**

**"Generate Report" button** → opens modal:
- Type select: Monthly Statement / Annual Summary
- Period start (date picker)
- Period end (date picker)
- Submit button → calls POST /api/v1/reports

**Reports history table:**
```
Date Requested | Type | Period | Status | Action
```
- Status badge with colours: PENDING (gray), PROCESSING (amber), COMPLETE (green), FAILED (red).
- PENDING/PROCESSING rows show a spinner and auto-poll every 3 seconds via `interval(3000)` + `switchMap`.
- COMPLETE rows show a "Download PDF" button which calls GET /reports/{id}/download and opens the pre-signed URL in a new tab.
- FAILED rows show a "Retry" button.

---

### 6.8 Settings Page

**Two sections:**

**Profile section:**
- Form: fullName (text), email (read-only display only).
- "Save Changes" button.

**Password section:**
- Form: currentPassword, newPassword (with strength indicator), confirmNewPassword.
- "Change Password" button.

On success: toast notification "Changes saved successfully."

---

## 7. State Management

Use Angular Signals for local component state and RxJS `BehaviorSubject` in services for shared state. No NgRx — keep it simple and demonstrable.

### 7.1 AuthService
```typescript
// Signals
currentUser = signal<User | null>(null);
isAuthenticated = computed(() => !!this.currentUser());

// Methods
login(credentials): Observable<AuthResponse>
register(data): Observable<AuthResponse>
logout(): void                          // clears storage, resets signal
refreshToken(): Observable<AuthResponse>
loadUserFromStorage(): void             // called on app init
```

### 7.2 Feature Services (pattern — same for all)
```typescript
@Injectable({ providedIn: 'root' })
export class TransactionService {
  private _transactions = signal<Transaction[]>([]);
  private _loading = signal(false);
  private _pagination = signal<Pagination | null>(null);

  transactions = this._transactions.asReadonly();
  loading = this._loading.asReadonly();
  pagination = this._pagination.asReadonly();

  getAll(filters: TransactionFilters): Observable<PagedResponse<Transaction>>
  create(dto: CreateTransactionDto): Observable<Transaction>
  update(id: number, dto: UpdateTransactionDto): Observable<Transaction>
  delete(id: number): Observable<void>
}
```

### 7.3 ToastService
```typescript
// Signals
toasts = signal<Toast[]>([]);

success(message: string, duration = 3000): void
error(message: string, duration = 5000): void
info(message: string, duration = 3000): void
dismiss(id: string): void
```
`ToastComponent` lives outside the router outlet, subscribes to `ToastService.toasts`, and renders fixed-position toast stack.

---

## 8. API Integration Layer

### 8.1 Environment Config
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:5000/api/v1',
};

// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiBaseUrl: 'https://your-eb-url.elasticbeanstalk.com/api/v1',
};
```

### 8.2 Auth Interceptor
`auth.interceptor.ts` — functional interceptor using `inject(TokenStorageService)`:
- Read access token from storage.
- Clone request, add `Authorization: Bearer <token>` header.
- If a 401 response is received, attempt one silent token refresh via `AuthService.refreshToken()`.
- If refresh succeeds, retry the original request with the new token.
- If refresh fails, call `AuthService.logout()` and navigate to `/auth/login`.

### 8.3 Error Interceptor
`error.interceptor.ts`:
- Intercept all HTTP error responses.
- 0 (network error) → toast "Network error. Please check your connection."
- 403 → toast "You don't have permission to do that."
- 404 → return the error (let components handle).
- 422 → return the error (let forms display field errors).
- 500 → toast "Something went wrong on our end. Please try again."
- Log all errors to console in dev mode.

### 8.4 API Service Pattern

All service methods return `Observable<T>` using `HttpClient`. Extract `data` from the `ApiResponse` wrapper using `map(res => res.data)`.

```typescript
getTransactions(filters: TransactionFilters): Observable<PagedResponse<Transaction>> {
  const params = buildHttpParams(filters);
  return this.http
    .get<ApiResponse<PagedResponse<Transaction>>>(`${this.base}/transactions`, { params })
    .pipe(map(res => res.data));
}
```

---

## 9. Authentication & Route Guards

### 9.1 AuthGuard
```typescript
export const authGuard: CanActivateFn = () => {
  const token = inject(TokenStorageService).getAccessToken();
  if (!token) {
    inject(Router).navigate(['/auth/login']);
    return false;
  }
  return true;
};
```

### 9.2 GuestGuard
```typescript
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (auth.isAuthenticated()) {
    inject(Router).navigate(['/dashboard']);
    return false;
  }
  return true;
};
```

### 9.3 Token Storage Service
```typescript
setTokens(accessToken: string, refreshToken: string): void
getAccessToken(): string | null
getRefreshToken(): string | null
clearTokens(): void
```
All methods interact with `localStorage`. Keys: `ft_access_token`, `ft_refresh_token`.

### 9.4 App Initializer
In `app.config.ts`, add an `APP_INITIALIZER` that calls `AuthService.loadUserFromStorage()` so that on page refresh, the user signal is restored before any route guard runs.

---

## 10. Forms & Validation

All forms use **Reactive Forms** (no Template-Driven). Every form field shows validation errors only after the field has been touched or the form has been submitted.

### 10.1 Custom Validators
```typescript
// passwords-match.validator.ts
export function passwordsMatchValidator(group: AbstractControl) {
  const pass = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return pass === confirm ? null : { passwordsMismatch: true };
}

// password-strength.validator.ts
export function passwordStrengthValidator(control: AbstractControl) {
  const val = control.value ?? '';
  const hasUpper = /[A-Z]/.test(val);
  const hasDigit = /\d/.test(val);
  const hasSpecial = /[^A-Za-z0-9]/.test(val);
  const hasLength = val.length >= 8;
  return hasUpper && hasDigit && hasSpecial && hasLength ? null : { weakPassword: true };
}
```

### 10.2 Error Message Pattern
Each input's error message is shown in a `<p>` tag below the field using `@if`:
```html
@if (form.get('email')?.invalid && form.get('email')?.touched) {
  @if (form.get('email')?.errors?.['required']) {
    <p class="mt-1 text-xs text-danger-500">Email is required.</p>
  }
  @if (form.get('email')?.errors?.['email']) {
    <p class="mt-1 text-xs text-danger-500">Enter a valid email address.</p>
  }
}
```

### 10.3 Form Loading State
All submit buttons must:
1. Show a `SpinnerComponent` (animated SVG) while the API call is in-flight.
2. Be `[disabled]="form.invalid || loading()"`.
3. Show the original label text when not loading.

---

## 11. Error Handling & Feedback

### 11.1 Toast Notifications
Use for non-blocking feedback:
- Transaction created → success toast
- Transaction deleted → success toast with "Undo" option (5 second window, calls restore endpoint)
- API error → error toast (from error interceptor)
- Report ready → success toast with "Download" action button

### 11.2 Confirmation Modals
Use for destructive actions (delete account, delete transaction, delete budget):
```
[Warning icon]
Delete Transaction?
This action cannot be undone.
[Cancel]  [Delete]
```
The `ModalComponent` is generic — accepts title, message, confirmLabel, confirmClass, and an output event emitter `(confirmed)`.

### 11.3 Loading States
- **Full page:** spinner centred in the content area while initial data loads.
- **Table rows:** skeleton rows (gray animated pulse blocks) while paginated data loads.
- **Cards:** skeleton card shimmer while data loads.
- **Buttons:** inline spinner replaces button text while submitting.

### 11.4 Empty States
Every list/table that can be empty must render an `EmptyStateComponent` with:
- An SVG illustration (inline, simple)
- A heading: e.g. "No transactions yet"
- A sub-text: e.g. "Add your first transaction to start tracking your finances."
- An optional CTA button

---

## 12. Routing

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () => import('./features/auth/auth.routes'),
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard',    loadChildren: () => import('./features/dashboard/dashboard.routes') },
      { path: 'transactions', loadChildren: () => import('./features/transactions/transactions.routes') },
      { path: 'accounts',     loadChildren: () => import('./features/accounts/accounts.routes') },
      { path: 'budgets',      loadChildren: () => import('./features/budgets/budgets.routes') },
      { path: 'reports',      loadChildren: () => import('./features/reports/reports.routes') },
      { path: 'settings',     loadChildren: () => import('./features/settings/settings.routes') },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
```

Router must be configured with `withComponentInputBinding()` and `withViewTransitions()`.

---

## 13. Testing Requirements

- **Framework:** Jest (configured via `jest-preset-angular`)
- **Minimum coverage:** 70% on all services and guards
- **Style:** Arrange → Act → Assert with descriptive test names

**Required test files:**

```
auth.service.spec.ts              — login, register, logout, token refresh
auth.guard.spec.ts                — redirects unauthenticated users
auth.interceptor.spec.ts          — attaches token, handles 401 + refresh
transaction.service.spec.ts       — getAll, create, update, delete
budget.service.spec.ts            — getForMonth, status calculation
token-storage.service.spec.ts     — set, get, clear tokens
login.component.spec.ts           — form validation, submit, error display
register.component.spec.ts        — password strength, match validation
```

---

## 14. Build & Deployment

### 14.1 Production Build
```bash
ng build --configuration=production
```
Output to `dist/fintrack-ui/browser/`.

### 14.2 S3 Deployment
- Upload `dist/` contents to S3 bucket configured for static website hosting.
- `index.html` is the error document (handles Angular SPA client-side routing).
- CloudFront distribution in front of S3 — default root object `index.html`.
- CloudFront custom error: 403 and 404 → return `index.html` with 200 status (SPA routing fix).

### 14.3 nginx.conf (for Docker alternative)
```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location ~* \.(js|css|png|svg|ico|woff2)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
  }

  add_header X-Frame-Options "DENY";
  add_header X-Content-Type-Options "nosniff";
  add_header Referrer-Policy "strict-origin-when-cross-origin";
}
```

---

## 15. CI/CD Pipeline

Pipeline file: `.github/workflows/frontend-deploy.yml`

### Trigger
```yaml
on:
  push:
    branches: [main]
    paths: ['fintrack-ui/**']
```

### Jobs

#### Job 1 — test
```
1. Checkout
2. Set up Node 20
3. Cache node_modules
4. npm ci
5. npm run test -- --coverage --watchAll=false
6. Upload coverage report as artifact
```

#### Job 2 — build (needs: test)
```
1. npm run build -- --configuration=production
2. Upload dist/ as artifact
```

#### Job 3 — deploy (needs: build)
```
1. Download dist/ artifact
2. Configure AWS credentials
3. Sync to S3: aws s3 sync dist/fintrack-ui/browser/ s3://${{ secrets.S3_BUCKET }} --delete
4. Invalidate CloudFront: aws cloudfront create-invalidation --distribution-id ${{ secrets.CF_DISTRIBUTION_ID }} --paths "/*"
```

### Required GitHub Secrets
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
S3_BUCKET
CF_DISTRIBUTION_ID
```

---

## 16. Prompting Guide

Use these prompts in sequence. Each builds on the previous. Test each section before moving on.

---

**Prompt 1 — Project scaffold**
```
Create an Angular 19 project called fintrack-ui using standalone components (no NgModules).
Configure:
1. Tailwind CSS v4 with the config defined in FRONTEND_REQUIREMENTS.md section 4.1.
2. The full folder structure from section 3.
3. app.config.ts with provideRouter (withComponentInputBinding, withViewTransitions),
   provideHttpClient (withInterceptors), and provideAnimations.
4. app.routes.ts with the full lazy-loaded route tree from section 12.
5. environments/environment.ts and environment.prod.ts from section 8.1.
6. styles.css with @tailwind base, components, utilities and Inter font import from Google Fonts.
7. Install: ng2-charts, chart.js.
Do not generate any feature component code yet — scaffold only.
```

---

**Prompt 2 — Core layer**
```
Implement the core layer for FinTrack Angular app as defined in FRONTEND_REQUIREMENTS.md:
1. TokenStorageService — get/set/clear tokens from localStorage using keys ft_access_token
   and ft_refresh_token.
2. AuthService — signal-based currentUser and isAuthenticated computed. Methods: login,
   register, logout, refreshToken, loadUserFromStorage. All methods use HttpClient and
   return Observables. On login/register success, store tokens via TokenStorageService
   and set currentUser signal.
3. auth.interceptor.ts (functional) — attach Bearer token, handle 401 with silent refresh
   as described in section 8.2.
4. error.interceptor.ts (functional) — handle network errors, 403, 500 with toast
   notifications as described in section 8.3. Return errors for 404 and 422.
5. AuthGuard and GuestGuard as defined in section 9.
6. APP_INITIALIZER in app.config.ts calling AuthService.loadUserFromStorage().
Register both interceptors in app.config.ts.
```

---

**Prompt 3 — Shared UI components**
```
Build the shared UI component library for FinTrack using Tailwind CSS only (no component library).
All components must be standalone Angular components.
Create:
1. SpinnerComponent — animated SVG circle spinner, accepts @Input size: 'sm'|'md'|'lg'.
2. ButtonComponent — wraps a <button> with primary/secondary/danger variants, loading state
   that shows SpinnerComponent, disabled state. Inputs: variant, loading, disabled, type.
3. BadgeComponent — status badge. Input: status type with variants defined in section 4.2.
4. ModalComponent — generic modal with overlay. Inputs: title, message, confirmLabel,
   confirmClass. Output: (confirmed) EventEmitter. Uses the overlay pattern in section 4.2.
5. ToastComponent + ToastService — fixed bottom-right stack. Toast types: success, error, info.
   Auto-dismiss after configurable duration. Animate in/out with Angular animations.
6. EmptyStateComponent — inputs: heading, subText, ctaLabel. Output: (ctaClick).
7. ShellComponent + SidebarComponent + TopbarComponent — full app shell layout as described
   in section 6.1. Use routerLink for nav items. Active state via routerLinkActive. Sidebar
   collapses to icon-only below lg breakpoint.
Use the exact Tailwind class patterns from FRONTEND_REQUIREMENTS.md section 4.2.
```

---

**Prompt 4 — Auth pages**
```
Build the Auth feature module for FinTrack:
1. LoginComponent — layout, reactive form, validation, loading state, error banner,
   link to register, as defined in FRONTEND_REQUIREMENTS.md section 6.2.
   On success: store tokens via AuthService, navigate to /dashboard.
2. RegisterComponent — layout, reactive form, password strength indicator bar,
   confirmPassword validator, as defined in section 6.2.
3. passwordStrengthValidator and passwordsMatchValidator from section 10.1.
4. auth.routes.ts with /login and /register paths.
Use Angular Signals for loading state. Use Reactive Forms. Apply Tailwind classes
from section 4.2 exactly. Show inline validation errors per the pattern in section 10.2.
```

---

**Prompt 5 — Dashboard**
```
Build the Dashboard feature for FinTrack as defined in FRONTEND_REQUIREMENTS.md section 6.3.
1. DashboardComponent fetches data from TransactionService.getSummary() and
   BudgetService.getForMonth() on init.
2. Four stat cards (Total Balance, Monthly Income, Monthly Expenses, Net This Month)
   using the card pattern from section 4.2. Amounts formatted with CurrencyZarPipe (R symbol).
3. Donut chart using Chart.js (ng2-charts) — expense breakdown by category for current month.
   Clicking a donut segment navigates to /transactions?categoryId=X.
4. Bar chart using Chart.js — income vs expenses for last 6 months.
5. Budget progress list — shows current month budgets with progress bars using the pattern
   from section 4.2. Colour-coded by status.
6. Recent transactions list — last 5 transactions from TransactionService.
Use skeleton shimmer loading states while data is fetching.
Create CurrencyZarPipe and RelativeDatePipe as defined in section 3.
```

---

**Prompt 6 — Transactions page**
```
Build the Transactions feature for FinTrack as defined in FRONTEND_REQUIREMENTS.md section 6.5.
1. TransactionsListComponent with:
   - Filter bar: date range, account dropdown, category dropdown, type toggle, search input
     (300ms debounce using RxJS debounceTime + distinctUntilChanged).
   - Paginated table (20 rows) using the table pattern from section 4.2.
   - Skeleton rows loading state.
   - Empty state when no results.
2. TransactionFormComponent as a modal for create and edit.
   - Category dropdown options reload when type toggle changes.
   - Pre-populates all fields when editing.
3. Delete confirmation modal before calling delete API.
4. On create/edit/delete: refresh list, show toast notification.
5. TransactionService with signals as defined in section 7.2.
All API calls map through ApiResponse wrapper. All amounts formatted with CurrencyZarPipe.
```

---

**Prompt 7 — Accounts, Budgets, Reports, Settings**
```
Build the remaining four feature modules for FinTrack:

ACCOUNTS (section 6.4):
- AccountsListComponent: card grid, add/edit/delete with modal and confirmation.
- AccountFormComponent: modal with reactive form.
- AccountService with HttpClient.

BUDGETS (section 6.6):
- BudgetsListComponent: month navigator, budget cards with progress bars, status badges,
  inline edit, delete. Add budget modal. Empty state for months with no budgets.
- BudgetService with HttpClient.

REPORTS (section 6.7):
- ReportsListComponent: generate report modal, history table with status badges.
- Auto-polling for PENDING/PROCESSING reports using RxJS interval(3000) + switchMap.
  Stop polling when status becomes COMPLETE or FAILED.
- Download PDF button calls GET /reports/{id}/download and window.open(url, '_blank').
- ReportService with HttpClient.

SETTINGS (section 6.8):
- Two-section form: profile update and password change.
- Use same validation patterns as auth forms.
- Show toast on save success.

For each module: create the service, the route file, and all components.
Apply all Tailwind patterns from section 4.2.
```

---

**Prompt 8 — Tests**
```
Write Jest unit tests for the following FinTrack Angular files. Use Arrange/Act/Assert
structure with descriptive test names. Mock all HTTP calls with HttpClientTestingModule.
Use Angular's TestBed.

1. auth.service.spec.ts — test login (success stores tokens, sets user signal),
   logout (clears storage, resets signal), loadUserFromStorage (restores user from token).
2. auth.guard.spec.ts — redirects to /login when no token, allows access when token exists.
3. auth.interceptor.spec.ts — attaches Authorization header, retries with new token on 401,
   calls logout on refresh failure.
4. transaction.service.spec.ts — getAll returns mapped data, create emits new transaction,
   delete calls correct endpoint.
5. token-storage.service.spec.ts — set/get/clear tokens in localStorage.
6. login.component.spec.ts — renders form, shows error on invalid email, calls AuthService
   on valid submit, shows spinner while loading.

Minimum 70% coverage on all services.
```

---

**Prompt 9 — CI/CD and deployment config**
```
Create the following for FinTrack frontend:
1. .github/workflows/frontend-deploy.yml implementing the 3-job pipeline from
   FRONTEND_REQUIREMENTS.md section 15: test, build, deploy to S3 + CloudFront invalidation.
2. nginx.conf from section 14.3 — SPA routing fallback, cache headers, security headers.
3. Dockerfile for the Angular app:
   Stage 1: node:20-alpine — npm ci, ng build --configuration=production
   Stage 2: nginx:alpine — copy dist/ to nginx html dir, copy nginx.conf
   Expose port 80.
Use the GitHub secrets listed in section 15.
```

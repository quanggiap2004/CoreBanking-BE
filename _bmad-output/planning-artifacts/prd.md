---
stepsCompleted:
- step-01-init
- step-02-discovery
- step-03-success
- step-04-journeys
- step-05-domain
- step-08-scoping
- step-09-functional
- step-10-nonfunctional
- step-11-polish
- step-12-complete
inputDocuments:
- project-overview.md
- architecture.md
- README.md
workflowType: 'create'
project: 'CoreBanking'
version: '1.0'
author: 'BMad Planner Agent'
date: '2026-01-28'
---

# Product Requirements Document (PRD): CoreBanking Limits & Deposits

## Executive Summary

**Product Vision:**
Transform the current CoreBanking MVP into a robust, secure banking platform that demonstrates enterprise-grade capability through Realistic Simulation. The goal is to impress potential employers not just with features, but with "Systems Thinking"—handling limits, concurrency, and security like a real bank.

**Core Differentiator:**
Realistic Risk Management. Unlike typical portfolio CRUD apps, this system implements ACID-compliant financial controls, rate limiting, and administrative overrides, simulating the actual constraints of a banking environment.

---

## Success Criteria

### User Success (The Admin/Demo User)
*   **Empowerment:** Can demonstrate a full "Deposit -> Transfer" flow in under 2 minutes without hitting backend errors.
*   **Control:** Can instantly upgrade a user's limit and see the effect immediately (e.g., failed transfer now succeeds).
*   **Realism:** The deposit flow feels like a real bank interaction (select source -> enter amount), even if backend is mocked.

### Business Success (Your Portfolio Goal)
*   **Impression:** Potential employers see "Enterprise Thinking" (Rate limits, Security, Audit logs).
*   **Reliability:** Zero "500 Internal Server Errors" during the demo. Graceful "422 Unprocessable Entity" when limits are hit.

### Technical Success (The Fintech Standard)
*   **Safety:** 100% prevention of negative deposits or transfers exceeding limits.
*   **Performance:** Rate limits enforce max 5 requests/minute on public endpoints to prevent abuse.
*   **Maintainability:** Zero new tables added for the MVP (using simple column strategy).

---

## Product Scope

### MVP Strategy: "Demonstrable Realism"
We are building a portfolio piece that acts like a bank, prioritizing robustness over feature breadth.

### MVP Feature Set (Phase 1)
**Core User Journeys Supported:**
1.  **Demo Power-User:** Deposit large funds -> Fail (Limit). Deposit small funds -> Success. Transfer -> Success.
2.  **Safe Admin:** Update User Limit -> Unblock VIP user.
3.  **Malicious Script:** Flood API -> Blocked by Rate Limiter.

**Must-Have Capabilities:**
*   **Database:** Add `transaction_limit` (BigDecimal) to `users` table. Default: 1000.00.
*   **API:**
    *   `POST /api/deposits`: Validates amount > 0. Increases Balance. Log "DEPOSIT" transaction.
    *   `POST /api/transfers`: Add check: `amount <= user.transactionLimit`.
    *   `PATCH /api/users/{id}/limits`: Admin only. Update limit.
*   **Security:** Rate Limit Filter (Bucket4j or simple generic filter) on `/api/deposits`.

### Phase 2: Lending & System Integrity (Active Development)
We are moving beyond simple transfers to complex financial products.

**1. The Safety Shield (Idempotency):**
*   **Goal:** Prevent accidental double-charges during network glitches.
*   **Mechanism:** Clients send a unique `X-Idempotency-Key` UUID. The Backend caches the result of the first request. If the same Key arrives again, it returns the cached success response *without* moving money again.

**2. The Lending Engine (Amortization):**
*   **Goal:** Demonstrate complex financial math and background processing.
*   **Core Feature:** An Amortized Personal Loan.
*   **The Math:** Users pay a fixed local **EMI (Equated Monthly Installment)**.
    *   Formula: `EMI = [P x R x (1+R)^N]/[(1+R)^N-1]`
    *   Internally, the `Interest` portion shrinks and `Principal` portion grows every month.
*   **Lifecycle:** `APPLY` -> `APPROVED` -> `ACTIVE` -> (Monthly Auto-Deduct) -> `PAID_OFF` or `OVERDUE`.

### Post-MVP (Phase 3 - Nice to Have)
*   Global Daily Limits (e.g., max $5k per day regardless of transaction size).
*   Multiple mock external bank sources (Chase, Wells Fargo).
*   Notification/Email when limit is hit/Loan approved.

---

## User Journeys

### 1. The Demo Power-User Journey
**Persona:** Alex, a recruiter reviewing your portfolio.
**Opening Scene:** Alex opens the CoreBanking frontend to test if it's "production-grade."
**Action:**
1.  He registers a new user "AlexTest".
2.  Notices the "Deposit" button. Clicks it.
3.  Selects "Mocked Wells Fargo". Enters a massive amount: `$1,000,000`.
4.  **Climax:** The system rejects it! "Error: Deposit limit exceeded".
5.  Alex nods, "Okay, they have validation." He tries `$1,000`. Success.
6.  He attempts a transfer of `$1,001` to another user.
7.  **Resolution:** System rejects it. "Error: Transaction limit is $1,000." Alex is impressed by the guardrails.

### 2. The Safe Admin Journey
**Persona:** You (Acting as bank Operation Manager).
**Opening Scene:** A VIP client (AlexTest) complains he can't move his money.
**Action:**
1.  You log in with `admin/admin`.
2.  Navigate to User Management API.
3.  Call `PATCH /api/users/{alex_id}/limits` with `{ "limit": 50000.00 }`.
4.  **Climax:** Response `200 OK`.
5.  **Resolution:** AlexTest retries the transfer, and it succeeds.

---

## Project-Type Requirements (API Backend)

### Endpoint Specifications
*   **Deposit API:**
    *   `POST /api/deposits`
    *   **Body:** `{ "source": "external_mock", "amount": 1000.00, "currency": "USD" }`
    *   **Response:** `200 OK` (Balance Updated) or `422` (Limit Exceeded).
*   **Limit Management:**
    *   `PATCH /api/users/{userId}/limits`
    *   **Header:** `Authorization: Bearer <AdminJWT>`
    *   **Body:** `{ "transactionLimit": 5000.00 }`

### Security Model
*   **RBAC:** `ROLE_USER` (Deposit/Transfer), `ROLE_ADMIN` (Override Limits).
*   **Rate Limiting:** Max 5 requests/minute per IP for `/api/deposits`.

---

## Functional Requirements

### Transaction Limits Management
*   **FR-001:** Users have a default transaction limit applied upon registration (initially $1,000).
*   **FR-002:** The system rejects any transfer where `amount > user.transactionLimit`.
*   **FR-003:** Admins can update the `transactionLimit` for any specific user via API.
*   **FR-004:** Users can view their current transaction limit in their profile.

### External Deposits (Simulation)
*   **FR-005:** Users can initiate a deposit by specifying an amount to a mocked source.
*   **FR-006:** The system validates that the deposit amount is positive (Amount > 0).
*   **FR-007:** Successful deposits immediately increase the user's account balance.
*   **FR-008:** The system records a "DEPOSIT" transaction type in the transaction history.

### Security & Integrity
*   **FR-009:** Deposit requests are rate-limited to prevent abuse (e.g., max 5/min).
*   **FR-010:** Admin endpoints (Limit Update) are protected by RBAC (Require `ROLE_ADMIN`).
*   **FR-011:** All rejected transactions (Limit Exceeded) are logged for audit purposes but do not affect balance.

### System Integrity (Phase 2)
*   **FR-012:** All critical financial API requests (Transfer, Deposit, Loan Repayment) MUST require a unique `X-Idempotency-Key` header.
*   **FR-013:** If a request with an existing `X-Idempotency-Key` is received within 24 hours, the system MUST return the original response without re-processing the transaction.

### Lending Engine (Phase 2)
*   **FR-014 (Application):** Users can apply for a Personal Loan (Term: 12-60 months).
*   **FR-015 (Approval):** System calculates a mock "Credit Score" (based on balance history). If > 700, auto-approve; else, flag for manual review (manual review simulated by 'PENDING' status).
*   **FR-016 (EMI Calculation):** Upon approval, system MUST generate an Amortization Schedule using the EMI formula: `EMI = P * r * (1+r)^n / ((1+r)^n - 1)`.
*   **FR-017 (Repayment):** A scheduled background job (daily/monthly simulation) MUST deduct the EMI from the user's main account on the due date.
*   **FR-018 (Overdue):** If Account Balance < EMI on due date, loan status updates to `OVERDUE` and a `MISSED_PAYMENT` event is logged.
*   **FR-019 (Early Repayment):** If a user pays > EMI, the excess amount filters primarily to `Principal` reduction, recalculating the `Loan End Date` (keeping EMI constant).

---

## Non-Functional Requirements

### Performance
*   **NFR-001 (Latency):** 95% of Deposit/Transfer API requests must complete in < 500ms.
*   **NFR-002 (Concurrency):** System must support 50 concurrent transactions without deadlocks.

### Security
*   **NFR-003 (Authorization):** `PATCH /limits` must strictly enforce `ROLE_ADMIN`.
*   **NFR-004 (Input validation):** All monetary inputs must reject negative values.
*   **NFR-005 (Rate Limiting):** Public deposit endpoint must return `429 Too Many Requests` if > 5 requests/min.

### Data Integrity
*   **NFR-006 (Precision):** All monetary values must be stored using `BigDecimal`.
*   **NFR-007 (Audit):** Every failed transaction due to Limits must be persisted with status `FAILED`.
*   **NFR-008 (Loan Precision):** Loan calculations (Interest, EMI) MUST use `BigDecimal` with `RoundingMode.HALF_EVEN` to 2 decimal places to prevent penny-rounding errors.
*   **NFR-009 (Scheduler Reliability):** The Loan Repayment Job implementation must use pessimistic locking (`SELECT ... FOR UPDATE`) on the Account row to prevent race conditions during auto-deduction.

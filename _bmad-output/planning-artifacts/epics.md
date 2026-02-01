---
stepsCompleted: []
inputDocuments: [project-overview.md, architecture.md, README.md, prd.md]
---

# CoreBanking - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for CoreBanking, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Users have a default transaction limit applied upon registration (initially $1,000).
FR2: The system rejects any transfer where `amount > user.transactionLimit`.
FR3: Admins can update the `transactionLimit` for any specific user via API.
FR4: Users can view their current transaction limit in their profile.
FR5: Users can initiate a deposit by specifying an amount to a mocked source.
FR6: The system validates that the deposit amount is positive (Amount > 0).
FR7: Successful deposits immediately increase the user's account balance.
FR8: The system records a "DEPOSIT" transaction type in the transaction history.
FR9: Deposit requests are rate-limited to prevent abuse (e.g., max 5/min).
FR10: Admin endpoints (Limit Update) are protected by RBAC (Require `ROLE_ADMIN`).
FR11: All rejected transactions (Limit Exceeded) are logged for audit purposes but do not affect balance.
FR12: All critical financial API requests (Transfer, Deposit, Loan Repayment) MUST require a unique `X-Idempotency-Key` header.
FR13: If a request with an existing `X-Idempotency-Key` is received within 24 hours, the system MUST return the original response without re-processing the transaction.
FR14 (Application): Users can apply for a Personal Loan (Term: 12-60 months).
FR15 (Approval): System calculates a mock "Credit Score" (based on balance history). If > 700, auto-approve; else, flag for manual review (manual review simulated by 'PENDING' status).
FR16 (EMI Calculation): Upon approval, system MUST generate an Amortization Schedule using the EMI formula: `EMI = P * r * (1+r)^n / ((1+r)^n - 1)`.
FR17 (Repayment): A scheduled background job (daily/monthly simulation) MUST deduct the EMI from the user's main account on the due date.
FR18 (Overdue): If Account Balance < EMI on due date, loan status updates to `OVERDUE` and a `MISSED_PAYMENT` event is logged.
FR19 (Early Repayment): If a user pays > EMI, the excess amount filters primarily to `Principal` reduction, recalculating the `Loan End Date` (keeping EMI constant).

### NonFunctional Requirements

NFR1 (Latency): 95% of Deposit/Transfer API requests must complete in < 500ms.
NFR2 (Concurrency): System must support 50 concurrent transactions without deadlocks.
NFR3 (Authorization): `PATCH /limits` must strictly enforce `ROLE_ADMIN`.
NFR4 (Input validation): All monetary inputs must reject negative values.
NFR5 (Rate Limiting): Public deposit endpoint must return `429 Too Many Requests` if > 5 requests/min.
NFR6 (Precision): All monetary values must be stored using `BigDecimal`.
NFR7 (Audit): Every failed transaction due to Limits must be persisted with status `FAILED`.
NFR8 (Loan Precision): Loan calculations (Interest, EMI) MUST use `BigDecimal` with `RoundingMode.HALF_EVEN` to 2 decimal places to prevent penny-rounding errors.
NFR9 (Scheduler Reliability): The Loan Repayment Job implementation must use pessimistic locking (`SELECT ... FOR UPDATE`) on the Account row to prevent race conditions during auto-deduction.

### Additional Requirements

From Architecture:
- Application follows Spring Boot Layered Architecture (Controller -> Service -> Repository).
- Security uses Stateless JWT authentication.
- Database locking utilizes Pessimistic Write locks (`SELECT ... FOR UPDATE`) on Account rows.
- Database migration is managed by Flyway.
- Error handling uses `GlobalExceptionHandler`.
- Deployment to Koyeb via Docker/Buildpack.

### FR Coverage Map

{{requirements_coverage_map}}

## Epic List

{{epics_list}}

<!-- Repeat for each epic in epics_list (N = 1, 2, 3...) -->

## Epic {{N}}: {{epic_title_N}}

{{epic_goal_N}}

<!-- Repeat for each story (M = 1, 2, 3...) within epic N -->

### Story {{N}}.{{M}}: {{story_title_N_M}}

As a {{user_type}},
I want {{capability}},
So that {{value_benefit}}.

**Acceptance Criteria:**

<!-- for each AC on this story -->

**Given** {{precondition}}
**When** {{action}}
**Then** {{expected_outcome}}
**And** {{additional_criteria}}

<!-- End story repeat -->

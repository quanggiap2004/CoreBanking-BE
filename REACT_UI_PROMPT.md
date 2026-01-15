# Build a ReactJS UI for Mini Core Banking System

## 📋 Project Overview

You are building a **modern, professional ReactJS frontend** for a **Mini Core Banking System** that handles:
- User registration and authentication (JWT-based)
- Account management (Savings & Current accounts)
- Fund transfers between accounts (with concurrency control)
- Transaction history and audit trails
- Real-time balance updates

The backend is a **Spring Boot REST API** running at `http://localhost:8080` with JWT authentication.

---

## 🎯 Core Requirements

### Technology Stack
- **Framework**: React 18+ with Vite (or Create React App)
- **Routing**: React Router v6
- **State Management**: React Context API + hooks (or Redux if complex)
- **HTTP Client**: Axios
- **Styling**: TailwindCSS with modern, professional banking UI
- **Form Validation**: React Hook Form + Yup validation
- **UI Components**: Headless UI or Radix UI for accessible components
- **Icons**: Heroicons or Lucide React
- **Notifications**: React Hot Toast or Sonner

### Design Principles
1. **Premium Banking Aesthetic**: 
   - Clean, professional, trustworthy design
   - Use gradient accents (blues, teals, purples)
   - Card-based layouts with subtle shadows
   - Smooth animations and transitions

2. **Security-First UX**:
   - Clear authentication states
   - Token expiration handling
   - Sensitive data masking (account numbers, balances)
   - Confirmation dialogs for transfers

3. **Responsive Design**:
   - Mobile-first approach
   - Tablet and desktop optimized
   - Touch-friendly interactive elements

---

## 🔐 Authentication & Authorization

### JWT Token Management

**Login Flow**:
1. User submits credentials to `POST /api/auth/login`
2. Receive JWT token in response
3. Store token in `localStorage` (or `sessionStorage`)
4. Include token in all protected API requests: `Authorization: Bearer <token>`

**Token Storage**:
```javascript
// After successful login
localStorage.setItem('authToken', response.data.token);
localStorage.setItem('userId', response.data.userId);
localStorage.setItem('username', response.data.username);

// For protected requests
const token = localStorage.getItem('authToken');
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
```

**Protected Routes**:
- All routes except `/login` and `/register` require authentication
- Redirect to `/login` if token is missing or expired
- Implement a `PrivateRoute` component wrapper

**Logout**:
- Clear `localStorage`
- Redirect to `/login`

---

## 📡 API Endpoints Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints (Public)

#### 1. Register User
```http
POST /auth/register
Content-Type: application/json

Request Body:
{
  "username": "john_doe",          // Required, 3-50 chars
  "password": "securePass123",     // Required, min 6 chars
  "fullName": "John Doe",          // Required
  "email": "john@example.com",     // Required, valid email
  "phone": "+1234567890",          // Optional
  "address": "123 Main St, NY",    // Optional
  "idDocumentNumber": "DL12345"    // Optional
}

Response (200 OK):
{
  "token": null,
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "message": "Registration successful. Please login."
}
```

#### 2. Login
```http
POST /auth/login
Content-Type: application/json

Request Body:
{
  "username": "john_doe",
  "password": "securePass123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "message": "Login successful"
}
```

---

### Account Endpoints (Protected)

> **Authentication Required**: Include `Authorization: Bearer <token>` header

#### 3. Get Account Details
```http
GET /accounts/{accountId}

Response (200 OK):
{
  "id": 1,
  "accountNumber": "ACC1738658758412",
  "balance": 5000.0000,
  "accountType": "SAVINGS",           // SAVINGS | CURRENT
  "status": "ACTIVE",                 // ACTIVE | FROZEN | CLOSED
  "interestRate": 0.0350,             // 3.5% for savings
  "createdAt": "2026-01-14T07:06:47.412345"
}
```

#### 4. Get User's Accounts
```http
GET /accounts/user/{userId}

Response (200 OK):
[
  {
    "id": 1,
    "accountNumber": "ACC1738658758412",
    "balance": 5000.0000,
    "accountType": "SAVINGS",
    "status": "ACTIVE",
    "interestRate": 0.0350,
    "createdAt": "2026-01-14T07:06:47.412345"
  },
  {
    "id": 2,
    "accountNumber": "ACC1738658799876",
    "balance": 1200.5000,
    "accountType": "CURRENT",
    "status": "ACTIVE",
    "interestRate": null,
    "createdAt": "2026-01-14T08:15:22.987654"
  }
]
```

#### 5. Get Account Audit Trail
```http
GET /accounts/{accountId}/audit

Response (200 OK):
[
  {
    "id": 15,
    "accountId": 1,
    "transactionId": 8,
    "previousBalance": 6000.0000,
    "newBalance": 5000.0000,
    "changeAmount": -1000.0000,
    "actionType": "TRANSFER",         // DEPOSIT | WITHDRAWAL | TRANSFER | INTEREST
    "initiatedBy": "john_doe",
    "createdAt": "2026-01-14T10:30:15.123456"
  },
  {
    "id": 14,
    "accountId": 1,
    "transactionId": null,
    "previousBalance": 5999.4200,
    "newBalance": 6000.0000,
    "changeAmount": 0.5800,
    "actionType": "INTEREST",
    "initiatedBy": "SYSTEM",
    "createdAt": "2026-01-14T00:00:00.000000"
  }
]
```

---

### Transfer Endpoints (Protected)

#### 6. Execute Fund Transfer
```http
POST /transfers
Content-Type: application/json
Authorization: Bearer <token>

Request Body:
{
  "sourceAccountNumber": "123456789012",        // Source account number
  "destinationAccountNumber": "987654321098",   // Destination account number
  "amount": 500.00,                             // Min: 0.01, Max: 100000.00
  "transferType": "INTERNAL",                   // INTERNAL | INTERBANK_MOCK
  "description": "Payment for rent"             // Optional
}

Response (200 OK):
{
  "transactionRef": "TXN1738658899123",
  "status": "COMPLETED",
  "message": "Transfer successful",
  "newSourceBalance": 4500.0000,
  "newDestinationBalance": 1700.5000
}

Error Response (400 Bad Request):
{
  "timestamp": "2026-01-14T10:45:30.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds",
  "path": "/api/transfers"
}
```

---

## 🗂️ Data Models

### User
```typescript
interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phone?: string;
  address?: string;
  idDocumentNumber?: string;
  kycVerified: boolean;
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
  createdAt: string; // ISO 8601
  updatedAt: string;
}
```

### Account
```typescript
interface Account {
  id: number;
  accountNumber: string;
  balance: number;
  accountType: 'SAVINGS' | 'CURRENT';
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
  interestRate?: number; // Only for savings accounts
  createdAt: string;
}
```

### Transaction
```typescript
interface Transaction {
  id: number;
  transactionRef: string;
  sourceAccountId?: number;
  destinationAccountId?: number;
  amount: number;
  transactionType: 'DEBIT' | 'CREDIT' | 'TRANSFER';
  transferType?: 'INTERNAL' | 'INTERBANK_MOCK' | 'INTEREST';
  description?: string;
  status: 'COMPLETED' | 'PENDING' | 'FAILED';
  createdAt: string;
}
```

### AuditLog
```typescript
interface AuditLog {
  id: number;
  accountId: number;
  transactionId?: number;
  previousBalance: number;
  newBalance: number;
  changeAmount: number;
  actionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'INTEREST';
  initiatedBy: string;
  createdAt: string;
}
```

---

## 🎨 Required Pages & Features

### 1. **Login Page** (`/login`)
- Username and password fields
- "Remember me" checkbox (optional)
- "Register" link
- Error message display for failed login
- Loading state during authentication

### 2. **Registration Page** (`/register`)
- Multi-step form or single page:
  - Step 1: Basic info (username, password, confirm password)
  - Step 2: Personal details (fullName, email, phone)
  - Step 3: Optional KYC (address, ID document)
- Form validation with real-time feedback
- Success message → redirect to login

### 3. **Dashboard** (`/dashboard`)
- **Header**: Username, logout button
- **Accounts Overview**:
  - Card for each account showing:
    - Account number (masked: `****5678`)
    - Account type badge
    - Current balance (large, prominent)
    - Status indicator
  - "View Details" button per account
- **Recent Transactions**: Last 5 transactions across all accounts
- **Quick Actions**: "New Transfer" button

### 4. **Account Details Page** (`/accounts/:accountId`)
- Account information card:
  - Full account number (with copy button)
  - Balance
  - Account type and status
  - Interest rate (if savings)
  - Creation date
- **Transaction History Tab**:
  - Paginated or infinite scroll list
  - Filter by date range
  - Each transaction shows:
    - Date & time
    - Type (debit/credit)
    - Amount (color-coded: red for debit, green for credit)
    - Description
    - Reference number
- **Audit Trail Tab**:
  - Compliance view of balance changes
  - Shows previous balance → new balance
  - Action type and initiator

### 5. **Transfer Money Page** (`/transfer`)
- **Form Fields**:
  - Source account (dropdown if user has multiple accounts)
  - Destination account ID (input with validation)
  - Amount (with currency formatting)
  - Transfer type (radio: Internal / Interbank Mock)
  - Description (optional textarea)
- **Validation**:
  - Amount > 0.01 and ≤ 100,000
  - Source ≠ Destination
  - Check sufficient balance (client-side hint)
- **Confirmation Modal**:
  - Summary of transfer details
  - "Confirm" and "Cancel" buttons
- **Success/Error Handling**:
  - Success: Show transaction ref, new balances
  - Error: Display backend error message (e.g., "Insufficient funds")

### 6. **Profile Page** (`/profile`) - Optional Enhancement
- Display user information
- Update email, phone, address
- Change password form
- View KYC status

---

## 🚀 Implementation Checklist

### Phase 1: Setup & Authentication
- [ ] Initialize React project with Vite
- [ ] Install dependencies (axios, react-router, tailwindcss, etc.)
- [ ] Setup TailwindCSS with custom banking theme
- [ ] Create authentication context (AuthContext)
- [ ] Implement login page
- [ ] Implement registration page
- [ ] Setup axios interceptor for JWT token
- [ ] Create PrivateRoute component
- [ ] Handle token expiration (401 responses)

### Phase 2: Core Features
- [ ] Create dashboard layout with navigation
- [ ] Fetch and display user accounts
- [ ] Build account details page
- [ ] Implement transaction history component
- [ ] Build transfer money form
- [ ] Add form validation
- [ ] Implement confirmation modal
- [ ] Handle transfer success/error states

### Phase 3: Polish & Enhancement
- [ ] Add loading skeletons
- [ ] Implement error boundaries
- [ ] Add toast notifications
- [ ] Format numbers (currency, dates)
- [ ] Add animations and transitions
- [ ] Implement responsive design
- [ ] Add audit trail view
- [ ] Optimize performance

### Phase 4: Testing & Deployment
- [ ] Test all user flows
- [ ] Test error scenarios
- [ ] Test responsive design
- [ ] Add environment variables for API URL
- [ ] Build production bundle
- [ ] Deploy to Vercel/Netlify

---

## 🎨 Design Guidelines

### Color Palette (Suggested)
```css
/* Primary (Banking Blue) */
--primary-50: #eff6ff;
--primary-500: #3b82f6;
--primary-700: #1d4ed8;

/* Success (Green) */
--success: #10b981;

/* Danger (Red) */
--danger: #ef4444;

/* Neutral */
--gray-50: #f9fafb;
--gray-900: #111827;
```

### Component Examples

**Account Card**:
```jsx
<div className="bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl p-6 text-white shadow-lg">
  <p className="text-sm opacity-80">Savings Account</p>
  <p className="text-2xl font-bold mt-2">$5,000.00</p>
  <p className="text-xs mt-4 font-mono">**** **** **** 5678</p>
</div>
```

**Transaction Item**:
```jsx
<div className="flex items-center justify-between p-4 border-b hover:bg-gray-50">
  <div>
    <p className="font-medium">Transfer to Account 2</p>
    <p className="text-sm text-gray-500">Jan 14, 2026 • 10:30 AM</p>
  </div>
  <p className="text-lg font-semibold text-red-600">-$500.00</p>
</div>
```

**Transfer Button**:
```jsx
<button className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium shadow-md transition-all transform hover:scale-105">
  Send Money
</button>
```

---

## ⚠️ Error Handling

### Common Scenarios

1. **Insufficient Funds**:
   - Show error toast: "Insufficient funds in source account"
   - Highlight amount field in red

2. **Invalid Account**:
   - Show error: "Destination account not found"

3. **Network Error**:
   - Show retry button
   - Persist form data

4. **Token Expiration**:
   - Auto-redirect to login
   - Show message: "Session expired, please login again"

5. **Validation Errors**:
   - Display inline field errors
   - Disable submit button until valid

---

## 🔧 Utility Functions

### Currency Formatter
```javascript
export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 4
  }).format(amount);
};
```

### Date Formatter
```javascript
export const formatDate = (isoString) => {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(isoString));
};
```

### Account Number Masker
```javascript
export const maskAccountNumber = (accountNumber) => {
  // "ACC1738658758412" → "**** **** **** 8412"
  return accountNumber.replace(/(.{3})(.*)(.{4})/, '****  ****  ****  $3');
};
```

---

## 🚦 Testing Credentials

Use the sample data from the backend:

**User 1**:
- Username: `alice_wonder`
- Password: `password123`
- Account IDs: Check via API after login

**User 2**:
- Username: `bob_builder`
- Password: `password123`

---

## 📦 Sample Dependencies (package.json)

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "axios": "^1.6.0",
    "react-hook-form": "^7.48.0",
    "yup": "@hookform/resolvers": "^3.3.0",
    "@headlessui/react": "^1.7.0",
    "@heroicons/react": "^2.1.0",
    "react-hot-toast": "^2.4.0",
    "clsx": "^2.0.0"
  },
  "devDependencies": {
    "vite": "^5.0.0",
    "tailwindcss": "^3.3.0",
    "autoprefixer": "^10.4.0",
    "postcss": "^8.4.0"
  }
}
```

---

## 🎯 Success Criteria

Your ReactJS application is complete when:

1. ✅ Users can register and login
2. ✅ JWT tokens are properly stored and used
3. ✅ Dashboard displays all user accounts
4. ✅ Users can view account details and transaction history
5. ✅ Fund transfers work with proper validation
6. ✅ Success/error messages are clear and helpful
7. ✅ UI is responsive and looks professional
8. ✅ All forms have proper validation
9. ✅ Numbers are formatted correctly (currency, dates)
10. ✅ Application handles errors gracefully

---

## 🚀 Next Steps After MVP

1. Add transaction search and filtering
2. Implement transaction export (CSV/PDF)
3. Add multi-factor authentication
4. Create admin panel
5. Add real-time notifications (WebSocket)
6. Implement daily transaction limits
7. Add beneficiary management
8. Create scheduled transfers

---

## 📞 Need Help?

- Backend API is at: `http://localhost:8080`
- API documentation: Check the `README.md` in the backend project
- Test the API with Postman/Insomnia before building UI
- Use browser DevTools Network tab to debug API calls

---

**Good luck building an amazing banking UI! 🏦✨**

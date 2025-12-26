# FinCalc Pro - ChatGPT App Store Submission Guide

## What Has Been Done

### 1. Backend (Java Spring Boot 3.5.3)

#### Architecture
- **Hexagonal Architecture** (Ports & Adapters) following SOLID principles
- Separation of concerns: Domain, Application, Adapter layers
- Dependency Inversion: All layers depend on abstractions (interfaces)

#### Core Components Created

| Component | Location | Purpose |
|-----------|----------|---------|
| `LoanCalculation` | `domain/model/` | Immutable record for loan calculations |
| `CompoundInterestCalculation` | `domain/model/` | Investment growth calculations |
| `TaxEstimation` | `domain/model/` | 2024 US federal + state tax calculations |
| `LoanCalculatorService` | `domain/service/` | Implements loan use case (SRP) |
| `CompoundInterestService` | `domain/service/` | Implements investment use case (SRP) |
| `TaxEstimatorService` | `domain/service/` | Implements tax use case (SRP) |
| `McpToolHandler` | `application/` | Bridges MCP protocol with domain |
| `McpController` | `adapter/in/web/` | SSE/JSON-RPC endpoints |

#### AOP & Cross-Cutting Concerns

| Component | Purpose |
|-----------|---------|
| `LoggingAspect` | Automatic logging of service calls with timing |
| `ExceptionHandlingAspect` | Centralized exception transformation |
| `MetricsAspect` | Tool usage statistics collection |
| `RateLimitFilter` | 60 requests/minute per IP |
| `SecurityHeadersFilter` | XSS, Content-Type, Frame protection |

#### Custom Bean Validation Constraints

| Annotation | Validator Class | Purpose |
|------------|-----------------|---------|
| `@ValidMoney` | `ValidMoneyValidator` | Validates monetary amounts (positive/zero) |
| `@ValidInterestRate` | `ValidInterestRateValidator` | Validates interest rate ranges |
| `@ValidLoanTerm` | `ValidLoanTermValidator` | Validates loan/investment term in years |
| `@ValidFilingStatus` | `ValidFilingStatusValidator` | Validates US tax filing status |

#### Externalized Configuration

| File | Purpose |
|------|---------|
| `application.yml` | Business rules (max loan years, rate limits) |
| `ValidationMessages.properties` | i18n validation messages |
| `FinCalcProperties.java` | Type-safe configuration binding |

#### Health & Monitoring

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Server health + calculation verification |
| `/api/metrics/tools` | Tool usage statistics |
| `/mcp/sessions/count` | Active MCP sessions |

### 2. Frontend (React 18 + TypeScript + Vite)

- Interactive demo with 3 tabbed calculators
- Form inputs with real-time API calls
- Professional styling with gradients and animations
- Mobile-responsive design

### 3. Testing

- **113 unit tests** covering:
  - Domain models (LoanCalculationTest, CompoundInterestCalculationTest, TaxEstimationTest)
  - Domain services (LoanCalculatorServiceTest, CompoundInterestServiceTest, TaxEstimatorServiceTest)
  - Application layer (McpToolHandlerTest)
  - Integration tests (McpControllerIntegrationTest)

### 4. DevOps

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build with Java 21 |
| `docker-compose.yml` | Local container deployment |
| `.dockerignore` | Build optimization |

---

## Next Steps to Submit

### Step 1: Get a Permanent Public URL

**Option A: Quick Deploy (Recommended for Testing)**
```bash
# Start ngrok
ngrok http 8002

# Your URL will be: https://xxx.ngrok-free.dev
```

**Option B: Cloud Deployment (Production)**

| Platform | Command |
|----------|---------|
| Railway | `railway init && railway up` |
| Render | Connect GitHub repo |
| Fly.io | `flyctl launch` |
| AWS | `docker build -t fincalc-pro . && docker push` |

### Step 2: Verify Endpoints

Test these URLs (replace with your domain):

```bash
# Health check
curl https://YOUR_DOMAIN/actuator/health

# MCP SSE connection
curl -H "Accept: text/event-stream" https://YOUR_DOMAIN/mcp

# Widget demo
open https://YOUR_DOMAIN/widget/index.html
```

### Step 3: Submit to ChatGPT App Store

1. Go to: https://community.openai.com/t/chatgpt-app-store-is-open-for-submissions/1369611

2. Fill in the submission form:

| Field | Value |
|-------|-------|
| **App Name** | FinCalc Pro |
| **Category** | Productivity / Finance |
| **MCP Endpoint** | `https://YOUR_DOMAIN/mcp` |
| **Transport** | SSE (Server-Sent Events) |
| **Description** | Professional financial calculators for ChatGPT - mortgage payments, investment growth, and tax estimation |

3. Provide tool descriptions:

**Tool 1: calculate_loan_payment**
> Calculate monthly payment, total payment, and total interest for mortgages and loans. Supports home loans, car loans, personal loans.

**Tool 2: calculate_compound_interest**
> Calculate future value of investments with compound interest and optional monthly contributions. Perfect for retirement planning and savings goals.

**Tool 3: estimate_taxes**
> Estimate US federal and state income taxes for 2024. Calculates tax liability, effective rate, and take-home pay.

### Step 4: Prepare for Review

Ensure you have:
- [ ] Public HTTPS URL working
- [ ] All 3 tools responding correctly
- [ ] Health endpoint returning `{"status":"UP"}`
- [ ] Widget demo accessible
- [ ] No errors in server logs

---

## File Structure Summary

```
fincalc-pro-java/
├── src/main/java/com/fincalc/
│   ├── FinCalcProApplication.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── LoanCalculation.java
│   │   │   ├── CompoundInterestCalculation.java
│   │   │   └── TaxEstimation.java
│   │   ├── port/in/
│   │   │   ├── CalculateLoanPaymentUseCase.java
│   │   │   ├── CalculateCompoundInterestUseCase.java
│   │   │   └── EstimateTaxesUseCase.java
│   │   ├── service/
│   │   │   ├── LoanCalculatorService.java
│   │   │   ├── CompoundInterestService.java
│   │   │   └── TaxEstimatorService.java
│   │   └── validation/constraint/
│   │       ├── ValidMoney.java
│   │       ├── ValidMoneyValidator.java
│   │       ├── ValidInterestRate.java
│   │       ├── ValidInterestRateValidator.java
│   │       ├── ValidLoanTerm.java
│   │       ├── ValidLoanTermValidator.java
│   │       ├── ValidFilingStatus.java
│   │       └── ValidFilingStatusValidator.java
│   ├── application/
│   │   └── McpToolHandler.java
│   └── adapter/
│       ├── in/web/
│       │   ├── McpController.java
│       │   └── dto/
│       │       ├── JsonRpcRequest.java
│       │       └── JsonRpcResponse.java
│       └── config/
│           ├── WebConfig.java
│           ├── FinCalcProperties.java
│           ├── RateLimitFilter.java
│           ├── SecurityHeadersFilter.java
│           ├── GlobalExceptionHandler.java
│           ├── LoggingAspect.java
│           ├── ExceptionHandlingAspect.java
│           ├── MetricsAspect.java
│           ├── MetricsController.java
│           └── McpHealthIndicator.java
├── src/main/resources/
│   ├── application.yml
│   └── ValidationMessages.properties
├── src/test/java/com/fincalc/
│   ├── domain/model/
│   ├── domain/service/
│   ├── application/
│   └── adapter/in/web/
├── widget/
│   ├── src/
│   │   ├── App.tsx
│   │   └── styles.css
│   ├── package.json
│   └── vite.config.ts
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Commands Reference

```bash
# Build
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build Docker
docker build -t fincalc-pro .
docker run -p 8002:8002 fincalc-pro

# Build widget
cd widget && pnpm install && pnpm run build
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8002 in use | `lsof -ti:8002 \| xargs kill -9` |
| Widget blank | Rebuild: `cd widget && pnpm run build` |
| 404 on /mcp/messages | Need to establish SSE connection first (GET /mcp) |
| Tests fail | Run `./mvnw clean test -X` for debug output |

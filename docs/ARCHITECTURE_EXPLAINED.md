# Numerai Finance - Architecture Guide

## How ChatGPT Chooses Your App

### Tool Selection Mechanism

When a user asks ChatGPT a question, it analyzes the query and matches it against available tools based on:

1. **Tool Name** - Descriptive names like `calculate_loan_payment` help ChatGPT match user intent
2. **Tool Description** - The description you provide tells ChatGPT what the tool does
3. **Input Schema** - Parameter names and descriptions guide ChatGPT on what data to extract

```
User: "What's my monthly payment for a $300K mortgage at 6.5%?"
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    ChatGPT Decision Process                  │
│                                                             │
│  1. Parse user intent: "calculate mortgage payment"         │
│  2. Search available tools for matching capabilities        │
│  3. Find: calculate_loan_payment (description matches)      │
│  4. Extract parameters from user query:                     │
│     - principal: 300000                                     │
│     - annualRate: 6.5                                       │
│     - years: ? (not provided, may ask user or use default)  │
│  5. Call the tool with extracted parameters                 │
└─────────────────────────────────────────────────────────────┘
```

### What Happens If Your App Doesn't Respond?

| Scenario | ChatGPT Behavior |
|----------|------------------|
| **Timeout (>30s)** | ChatGPT tells user the tool is unavailable, may retry |
| **Error Response** | ChatGPT shows error message from your API |
| **Invalid Data** | ChatGPT attempts to interpret or asks for clarification |
| **Server Down** | ChatGPT falls back to its own knowledge (less accurate) |

**Your app's reliability matters!** That's why we have:
- Health checks (`/actuator/health`) - Railway monitors this
- Rate limiting - Prevents overload
- Proper error messages - Help ChatGPT explain issues to users

---

## The Complete Flow

```
User: "Calculate my mortgage payment for $300,000 at 6.5% for 30 years"
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                        ChatGPT (The AI)                        │
│                                                                │
│  1. Understands: user wants a loan calculation                 │
│  2. Sees available tool: "calculate_loan_payment"              │
│  3. Extracts parameters from natural language                  │
│  4. Calls Numerai Finance via MCP                              │
└────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                     MCP Protocol Layer                         │
│                                                                │
│  JSON-RPC request over SSE:                                    │
│  {                                                             │
│    "method": "tools/call",                                     │
│    "params": {                                                 │
│      "name": "calculate_loan_payment",                         │
│      "arguments": { "principal": 300000, "annualRate": 6.5 }   │
│    }                                                           │
│  }                                                             │
└────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                   Numerai Finance Server                       │
│                                                                │
│  McpController → McpToolHandler → LoanCalculatorService        │
│                                          │                     │
│                                          ▼                     │
│                              LoanCalculation.calculate()       │
│                              - Validates inputs                │
│                              - Applies amortization formula    │
│                              - Returns exact results           │
└────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────┐
│                        ChatGPT Response                        │
│                                                                │
│  "For a $300,000 mortgage at 6.5% over 30 years:              │
│   - Monthly Payment: $1,896.20                                 │
│   - Total Payment: $682,632.00                                 │
│   - Total Interest: $382,632.00"                               │
└────────────────────────────────────────────────────────────────┘
```

---

## Why ChatGPT Needs Numerai Finance

| ChatGPT Alone | With Numerai Finance |
|---------------|----------------------|
| Might estimate "around $1,900" | Exact: $1,896.20 |
| No breakdown of costs | Full amortization details |
| Could hallucinate numbers | Validated calculations |
| No current tax brackets | 2024 federal + state rates |

---

## Architecture Layers

### Layer 1: Adapter (Infrastructure)
```
McpController          - SSE/JSON-RPC endpoints
RateLimitFilter        - 60 req/min per IP
SecurityHeadersFilter  - XSS protection
CspFilter              - ChatGPT embedding allowed
```

### Layer 2: Application (Orchestration)
```
McpToolHandler         - Routes tool calls to services
                       - Formats responses for ChatGPT
```

### Layer 3: Domain (Business Logic)
```
LoanCalculatorService       → LoanCalculation
CompoundInterestService     → CompoundInterestCalculation
TaxEstimatorService         → TaxEstimation
```

---

## Tool Definitions

ChatGPT discovers available tools via the MCP protocol:

```json
{
  "name": "calculate_loan_payment",
  "description": "Calculate monthly mortgage or loan payments with amortization",
  "inputSchema": {
    "type": "object",
    "properties": {
      "principal": { "type": "number", "description": "Loan amount in dollars" },
      "annualRate": { "type": "number", "description": "Annual interest rate %" },
      "years": { "type": "integer", "description": "Loan term in years" }
    },
    "required": ["principal", "annualRate", "years"]
  }
}
```

The better your descriptions, the more accurately ChatGPT matches user requests to your tools.

---

## Production Features

| Feature | Purpose |
|---------|---------|
| Health Checks | Railway monitors `/actuator/health` |
| Rate Limiting | Prevents abuse (60 req/min) |
| CSP Headers | Allows ChatGPT iframe embedding |
| Structured Logging | JSON logs for debugging |
| Tracing | Request tracking across services |

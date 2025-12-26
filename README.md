# Numerai Finance

**Professional Financial Calculator for ChatGPT** - Smart money decisions with accurate calculations.

[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/template)

## What It Does

Numerai Finance is a professional financial calculator that helps you make smarter money decisions:

- **Loan Payments** - Calculate monthly payments, total interest, and amortization for mortgages, auto loans, and personal loans
- **Investment Growth** - Project future value with compound interest and recurring contributions for retirement and savings goals
- **Tax Estimates** - Estimate federal and state income taxes with 2025 brackets for 50+ countries
- **Market Rates** - Access current mortgage rates, prime rate, and Federal Reserve data from FRED

## Why Use This Instead of ChatGPT Alone?

| Raw ChatGPT | Numerai Finance |
|-------------|-----------------|
| May estimate "around $1,900" | Exact: **$1,896.20** |
| Tax brackets from training (outdated) | **2025 official rates** |
| Static rate guesses | **Live Federal Reserve data** |
| No audit trail | Full calculation logging |
| English only | **10 languages** |
| US-focused | **50+ countries** |

## Global Coverage

### Countries (50+)
Americas, Europe, Asia-Pacific, Middle East, Africa including:
- **Americas**: US, Canada, Mexico, Brazil, Argentina, Chile, Colombia, Peru
- **Europe**: UK, Germany, France, Italy, Spain, Netherlands, Switzerland, Sweden, Norway, Poland, and more
- **Asia-Pacific**: Japan, South Korea, China, India, Australia, New Zealand, Singapore, Hong Kong, Thailand, Malaysia, Indonesia, Philippines, Vietnam
- **Middle East**: UAE, Saudi Arabia, Qatar, Kuwait, Israel, Egypt
- **Africa**: South Africa, Nigeria, Kenya, Ghana, Morocco

### Currencies (85+)
USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, KRW, SGD, HKD, MXN, BRL, ZAR, AED, SAR, and 70+ more with proper formatting.

### Languages (10)
English, Spanish, French, German, Portuguese, Japanese, Chinese, Arabic, Hindi, Korean

### Rate Providers (50+)
Central banks worldwide: Federal Reserve, ECB, Bank of England, Bank of Japan, RBI, and 45+ more.

## Quick Start

```bash
# Clone and run
git clone https://github.com/mohamedosmantech/numerai-finance.git
cd numerai-finance
./mvnw spring-boot:run
```

Server: `http://localhost:8002`

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `/mcp` | MCP/ChatGPT connection (SSE) |
| `/api/rates` | Live market rates |
| `/admin` | Admin Dashboard UI |
| `/swagger-ui.html` | Interactive API docs |
| `/actuator/health` | Health check |

## MCP Tools

| Tool | Description |
|------|-------------|
| `calculate_loan_payment` | Monthly payment, total interest, amortization |
| `calculate_compound_interest` | Future value with compounding and contributions |
| `estimate_taxes` | Federal + state tax calculation |
| `get_current_rates` | Live mortgage and Fed rates |

## Admin Dashboard

Access at `/admin` (login required) to manage:
- **Countries & Tax Systems** - 50+ countries with 2025 tax brackets
- **Currencies & Formatting** - 85+ currencies with symbols/separators
- **Rate Providers** - 50+ central banks and data sources
- **Localized Messages** - 100+ messages in 10 languages

## Deployment

### Railway (Recommended)
```bash
railway login
railway init
railway up
```

### Docker
```bash
docker build -t numerai-finance .
docker run -p 8002:8002 numerai-finance
```

### Environment Variables
```
DATABASE_URL=postgresql://...
FRED_API_KEY=your_key
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_password
```

## Architecture

Hexagonal (Ports & Adapters) architecture:

```
src/main/java/com/fincalc/
├── domain/           # Business logic
│   ├── model/        # Country, Currency, TaxSystem, etc.
│   └── port/         # Interfaces (in/out)
├── application/      # Use cases
└── adapter/          # Implementations
    ├── in/web/       # REST controllers
    └── out/          # Database, APIs
```

## Tech Stack

- Java 21 + Spring Boot 3.5
- PostgreSQL + Liquibase migrations
- Thymeleaf admin dashboard
- FRED API integration
- MCP protocol (SSE)

## ChatGPT App Submission

Live URL: `https://numerai-finance-production.up.railway.app`

MCP Endpoint: `https://numerai-finance-production.up.railway.app/mcp`

## License

MIT

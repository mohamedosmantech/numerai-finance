# Numerai Finance

**Smart Financial Intelligence for ChatGPT** - Professional-grade financial calculations that AI alone can't reliably provide.

## Why Numerai Finance?

| Raw ChatGPT | Numerai Finance |
|-------------|-----------------|
| May estimate "around $1,900" | Exact: $1,896.20 |
| Tax brackets from training (outdated) | 2025 official rates |
| Static rate guesses | Live Federal Reserve data |
| No audit trail | Full calculation logging |
| English only | 6 languages |
| Can't customize | Admin dashboard |

## Features

- **Mortgage & Loan Calculator** - Exact payments, amortization
- **Investment Growth Projector** - Compound interest with contributions
- **Smart Tax Estimator** - 2025 US federal + state taxes
- **Live Market Rates** - Real-time from Federal Reserve
- **Admin Dashboard** - Manage countries, currencies, messages via web UI
- **Database Migrations** - Liquibase for schema versioning

## Global Support

**Countries**: US, UK, Canada, Germany, Australia (extensible)
**Currencies**: USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, and more
**Languages**: English, Spanish, French, German, Chinese, Arabic

## Quick Start

```bash
./mvnw spring-boot:run
```
Server: `http://localhost:8002`

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `/mcp` | MCP/ChatGPT connection |
| `/api/rates` | Live market rates |
| `/api/admin/config/*` | Admin REST API |
| `/admin` | Admin Dashboard UI |
| `/h2-console` | Database Console (dev) |
| `/swagger-ui.html` | Interactive API docs |

## Admin Dashboard

Access at `/admin` to manage:
- **Countries & Tax Systems** - Add countries, configure 2025 tax brackets
- **Currencies & Formatting** - 15+ currencies with custom symbols/separators
- **Rate Providers** - FRED, ECB, BOE, BOC, RBA integrations
- **Localized Messages** - 6 languages (EN, ES, FR, DE, ZH, AR)
- **Response Templates** - Customize tool output formats

## Architecture

**Hexagonal Architecture** with extensibility built-in:

```
domain/
├── model/config/    # Country, Currency, RateProvider, LocalizedMessage
├── port/in/         # Use case interfaces
└── port/out/        # ConfigurationPort, MarketRatePort

adapter/
├── in/web/          # Controllers (MCP, Rates, Admin)
└── out/             # FRED API, InMemory/Database adapters
```

## Deployment

```bash
# Railway (recommended)
railway init && railway up

# Docker
docker build -t numerai-finance .
docker run -p 8002:8002 numerai-finance
```

## Documentation

- [API Reference](docs/API.md)
- [Architecture](docs/ARCHITECTURE_EXPLAINED.md)
- [Submission Guide](docs/SUBMISSION_GUIDE.md)
- [Postman Collection](docs/postman-collection.json)

## License

MIT

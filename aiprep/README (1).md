# AI Interview Platform

An AI-powered interview preparation platform built with Spring Boot. It helps users practice technical and behavioral interviews, solve coding problems, get resume feedback, and follow a personalized learning roadmap — all backed by Google's Gemini AI.

**Live demo:** https://ai-interview-platform-production-9682.up.railway.app/login

## Features

- **Mock Interviews** — AI-driven interview sessions with real-time feedback
- **Coding Practice** — Solve coding problems with code execution powered by [Piston](https://github.com/engineer-man/piston)
- **Resume Analysis** — Upload a resume (PDF) and get AI-generated feedback
- **Learning Roadmap** — Personalized skill roadmaps for interview prep
- **Authentication** — Email/password login with OTP verification, plus optional Google OAuth2 login
- **Admin Dashboard** — Manage users and platform activity
- **Premium Plans** — Subscription/payment support via Razorpay
- **Free Tier Limits** — Configurable daily free interview cap for non-premium users

## Tech Stack

| Layer          | Technology                          |
|----------------|--------------------------------------|
| Language       | Java 21                             |
| Framework      | Spring Boot 3.4.5                   |
| Web            | Spring Web, Thymeleaf               |
| Security       | Spring Security, OAuth2 Client, JWT (jjwt) |
| Persistence    | Spring Data JPA, Hibernate, PostgreSQL |
| Mail           | Spring Mail (SMTP, OTP delivery)    |
| AI             | Google Gemini API                   |
| Code Execution | Piston public API (sandboxed)       |
| PDF Parsing    | Apache PDFBox                       |
| Payments       | Razorpay                            |
| Caching        | Caffeine                            |
| Build Tool     | Maven                               |

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 12+
- A Gmail (or other SMTP) account for sending OTP emails
- A Google Gemini API key
- (Optional) Razorpay account for payments
- (Optional) Google OAuth2 credentials for social login

## Getting Started (Local Development)

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/ai-interview-platform.git
   cd ai-interview-platform/aiprep
   ```

2. **Create a local PostgreSQL database**
   ```bash
   createdb ai_interview
   ```

3. **Set environment variables** (see [Configuration](#configuration) below), or rely on the defaults in `application.properties` for a quick local run.

4. **Run the app**
   ```bash
   ./mvnw spring-boot:run
   ```

5. Visit `http://localhost:8080/login`

## Configuration

All configuration is exposed via environment variables (see `src/main/resources/application.properties`). None are hardcoded requirements — sensible local defaults are provided, but **production deployments must override all secrets**.

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC URL for PostgreSQL, e.g. `jdbc:postgresql://host:5432/dbname` | `jdbc:postgresql://localhost:5432/ai_interview` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | — |
| `DDL_AUTO` | Hibernate schema management mode | `update` |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP account username | — |
| `MAIL_PASSWORD` | SMTP account password / app password | — |
| `JWT_SECRET` | Secret key used to sign JWTs | random (regenerated each restart if unset) |
| `JWT_EXPIRATION_MS` | JWT expiry in milliseconds | `3600000` (1 hour) |
| `GEMINI_API_KEY` | Google Gemini API key | — |
| `PISTON_BASE_URL` | Base URL for the Piston code execution API | `https://emkc.org/api/v2/piston` |
| `RAZORPAY_KEY_ID` | Razorpay key ID | — |
| `RAZORPAY_KEY_SECRET` | Razorpay key secret | — |
| `RAZORPAY_PREMIUM_AMOUNT_PAISE` | Premium plan price, in paise | `49900` |
| `FREE_INTERVIEWS_PER_DAY` | Free-tier daily interview limit | `3` |
| `COOKIE_SECURE` | Whether auth cookies require HTTPS | `true` |
| `PORT` | Server port | `8080` |

> ⚠️ **Important:** Set `JWT_SECRET` explicitly in production. If left unset, a new random signing key is generated on every restart, invalidating all existing sessions/tokens.

## Deployment (Railway)

This project is deployed on [Railway](https://railway.app). Key setup notes:

1. Provision a **PostgreSQL** service in your Railway project.
2. In your app service's **Variables**, set:
   ```
   DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   DB_USERNAME=${{Postgres.PGUSER}}
   DB_PASSWORD=${{Postgres.PGPASSWORD}}
   ```
   (Replace `Postgres` with the actual name of your Postgres service if different.)
3. Set the remaining secrets (`JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `GEMINI_API_KEY`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`) as environment variables on the app service.
4. Railway automatically provides `PORT`, which the app already respects via `server.port=${PORT:8080}`.
5. Deploy — Railway builds and runs the Spring Boot JAR automatically on push.

> Note: Railway's auto-generated `DATABASE_URL` uses the `postgres://user:pass@host:port/db` format, which is **not** compatible with Spring's JDBC driver. Always construct `DB_URL` in the `jdbc:postgresql://host:port/db` format as shown above, with credentials passed separately via `DB_USERNAME` / `DB_PASSWORD`.

## Project Structure

```
aiprep/
├── src/main/java/com/aiprep/interview/
│   ├── controller/       # REST & page controllers (Coding, User, Admin, Resume, Roadmap, Payment, Interview)
│   ├── util/             # Utilities (JWT, etc.)
│   └── InterviewPlatformApplication.java
├── src/main/resources/
│   ├── templates/        # Thymeleaf views (login, register, dashboard, interview, coding, resume, roadmap, pricing, admin)
│   ├── static/           # CSS/JS assets
│   └── application.properties
└── pom.xml
```

## Security Notes

- Never commit real secrets (API keys, SMTP passwords, JWT secrets, DB credentials) to source control.
- Rotate any credentials that may have been exposed in commit history or logs.
- Use your hosting provider's environment variable / secrets manager for all values listed in [Configuration](#configuration).

## License

Add your license of choice here (e.g., MIT).

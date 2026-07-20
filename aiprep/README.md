# AI Interview Preparation Platform

A full-stack Spring Boot + Thymeleaf application for practicing job interviews with real AI
question generation and evaluation, plus resume analysis, coding practice, a study roadmap
generator, an admin panel, and subscriptions.

## Features implemented

- **Auth**: register/login with two-step email OTP, forgot/reset password, Google OAuth2 login
  (optional), JWT (httpOnly cookie), BCrypt password hashing, role-based access (USER/ADMIN),
  per-IP rate limiting on login/register/forgot-password.
- **AI Mock Interview Engine**: pick role / experience level / interview type, AI (OpenAI)
  generates questions, you answer by typing or by voice (browser speech-to-text), AI scores you
  on technical correctness, confidence, communication, and keyword coverage, with suggestions
  and a sample answer. Questions can also be read aloud (browser text-to-speech).
- **Resume Analyzer**: upload a PDF, text is extracted (Apache PDFBox) and sent to AI for an
  ATS score, missing keywords, weak points, and improvement suggestions.
- **Coding Interview Practice**: write code in Java/Python/JavaScript/C++, run it in a real
  sandboxed execution service (the public Piston API - not on our own server, since running
  arbitrary user code safely needs real container-level isolation), then get an AI code review.
- **AI Roadmap Generator**: current skills + target role/company → a markdown study plan.
- **Interview history dashboard**: every interview and its score is stored and listed.
- **Admin panel**: list/ban/unban users, platform-wide analytics (total users, interviews,
  premium users, resumes analyzed, coding submissions, average score).
- **Subscriptions/payments**: free tier with a daily interview quota, Premium tier (unlimited)
  via Razorpay checkout + server-side signature verification.
- **Frontend**: server-rendered Thymeleaf pages (Tailwind CDN) calling the same REST API a
  future separate frontend could use.

## Local setup

1. Install Java 21, Maven, and PostgreSQL.
2. `createdb ai_interview`
3. Copy `.env.example` to `.env`, fill in real values, and export them into your shell
   (or use a tool like `direnv`, or your IDE's env-var run configuration).
4. Run: `./mvnw spring-boot:run`
5. Visit `http://localhost:8080`.

The app **starts and runs with only the database configured** - OpenAI, Google login, and
Razorpay are optional and degrade gracefully:
- No `OPENAI_API_KEY` → AI features return a clear "AI service unavailable" error instead of crashing.
- No Google OAuth configured → the "Continue with Google" button simply won't work yet; email/password login is unaffected.
- No Razorpay keys → the Premium upgrade button returns a clear "payments not configured" message.

### Enabling Google login
1. Create an OAuth 2.0 Client ID at https://console.cloud.google.com/apis/credentials
   (application type: Web application; authorized redirect URI:
   `http://localhost:8080/login/oauth2/code/google` for local dev, or your real domain in production).
2. Set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`.
3. Run with the `oauth` profile active: `./mvnw spring-boot:run -Dspring-boot.run.profiles=oauth`
   (or set `SPRING_PROFILES_ACTIVE=oauth` on your hosting platform).

### Enabling payments
1. Create a Razorpay account and get your test/live Key ID and Key Secret from the dashboard.
2. Set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`.
3. The `/pricing` page will now create real orders and verify payments server-side.

### Getting an OpenAI key
Sign up at https://platform.openai.com, create a key under API Keys, set `OPENAI_API_KEY`.
`gpt-4o-mini` (the default) is inexpensive and fast enough for this use case.

## Database

See **DATABASE_SETUP.md** (in the separate database package) for the full Postgres schema
(`schema.sql`), a `docker-compose.yml` for local development, and step-by-step instructions for
provisioning managed Postgres on Render, Railway, or Fly.io. Short version: the app creates all
tables itself on first run (`spring.jpa.hibernate.ddl-auto=update`) — `schema.sql` is there so
you can see/version the real structure and optionally lock it down later.

## Deployment

- **Backend**: `docker build -t ai-interview .` and deploy to Render, Railway, or Fly.io.
  Set all the same environment variables from `.env.example` in that platform's dashboard.
- **Database**: Neon or Supabase Postgres.
- **CORS**: update the allowed origin patterns in `SecurityConfig.corsConfigurationSource()`
  once you have a real deployed URL.
- Set `JWT_SECRET` in production (a long random string) so tokens survive restarts and work
  across multiple instances - without it, a random key is generated per run (fine for local dev only).
- Set cookie `Secure` flags to `true` (see the `TODO` comments in `UserController` and
  `OAuth2LoginSuccessHandler`) once you're serving over HTTPS.

## Architecture notes / things to know before an interview about this project

- **Code execution isn't run on our own server.** User-submitted code is sent to Piston, a
  dedicated sandboxed execution service, rather than executed locally - running arbitrary code
  from end users safely requires real container isolation, resource limits, and no network
  access, which we don't want to reimplement ourselves for a portfolio project. For high-traffic
  production use, self-host a Piston instance instead of hitting the public one.
- **Free-tier quota** resets daily per user (checked lazily on each request, plus a nightly
  scheduled job as a cleanup safety net).
- **Rate limiting** on auth endpoints is a simple in-memory per-IP counter (Caffeine cache) -
  fine for a single instance; a multi-instance production deployment would need a shared store
  (e.g. Redis) instead.

## Not included (possible next steps)

WebSocket real-time interview chat, AI emotion/tone detection, a public leaderboard,
company-specific question banks, and PDF interview report export were in the original feature
wishlist but are considered "nice to have" stretch goals beyond this build - ask if you'd like
any of them added next.

# Database setup — AI Interview Preparation Platform

The app uses **PostgreSQL**. You have two ways to get the tables created — pick one:

## Option A (default, zero effort): let the app create the schema itself
`application.properties` has `spring.jpa.hibernate.ddl-auto=update`, so the very first time
the app connects to an **empty** Postgres database, Hibernate creates every table for you
automatically, based on the entity classes. You don't have to run any SQL by hand.

`schema.sql` in this folder is provided purely so you (or an interviewer) can **see the real
table structure** without reading through 9 entity classes, and so you can optionally take
manual control of the schema later (see "Locking the schema" below).

## Option B: run schema.sql yourself first
If you'd rather create the tables explicitly before the app ever touches the database:

```bash
psql "postgresql://USER:PASSWORD@HOST:PORT/DBNAME" -f schema.sql
```

Then, once the tables exist, it's a good idea to change one setting so Hibernate never tries
to alter your schema again — see "Locking the schema" below.

---

## Running Postgres locally (for development)

This folder includes a `docker-compose.yml` that starts a local Postgres instance and loads
`schema.sql` into it automatically:

```bash
docker compose up -d
```

This gives you:
- host: `localhost`, port: `5432`
- database: `ai_interview`
- user: `postgres`, password: `postgres`

Point the app at it (in `.env` or your shell):
```
DB_URL=jdbc:postgresql://localhost:5432/ai_interview
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

To reset the local database completely (wipes all data): `docker compose down -v`

---

## Production database on Render / Railway / Fly.io

You said you're deploying to a PaaS. All three give you a **managed Postgres instance** with
a connection string — you don't run docker-compose in production, only locally.

### Render
1. Dashboard → **New +** → **PostgreSQL**. Pick a name/region/plan (the free tier works for
   testing but is deleted after 90 days — fine for a demo, not for a real launch).
2. Once created, open it and copy the **Internal Database URL** (if your web service is also
   on Render, use the internal URL — it's faster and free egress) or **External Database URL**
   (if connecting from elsewhere).
3. Render gives you one JDBC-incompatible URL like `postgres://user:pass@host:port/db`. Convert
   it to the JDBC form the app expects:
   ```
   DB_URL=jdbc:postgresql://host:port/db
   DB_USERNAME=user
   DB_PASSWORD=pass
   ```
4. Set those (plus `JWT_SECRET`, `OPENAI_API_KEY`, etc. from `.env.example`) as **environment
   variables** on your Render web service, not in a committed file.

### Railway
1. **New** → **Database** → **Add PostgreSQL**.
2. Click the Postgres service → **Variables** tab → copy `PGHOST`, `PGPORT`, `PGDATABASE`,
   `PGUSER`, `PGPASSWORD`.
3. On your app service's **Variables** tab, set:
   ```
   DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   DB_USERNAME=${{Postgres.PGUSER}}
   DB_PASSWORD=${{Postgres.PGPASSWORD}}
   ```
   (Railway lets services reference each other's variables directly like this.)

### Fly.io
1. `fly postgres create` (creates a managed Postgres app + volume).
2. `fly postgres attach <postgres-app-name> --app <your-app-name>` — this automatically injects
   a `DATABASE_URL` env var into your app in the form `postgres://user:pass@host:port/db`.
3. Convert to JDBC form as in the Render section above and set `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`
   (Fly doesn't parse `DATABASE_URL` for you — this app reads the three separate `DB_*` vars).

All three: also set every other variable from `.env.example` (`JWT_SECRET`, `OPENAI_API_KEY`,
`COOKIE_SECURE=true`, etc.) on the platform's dashboard as real environment variables — never
commit `.env` with real secrets to git.

---

## Locking the schema for production (recommended once live)

`ddl-auto=update` is convenient but in a real production app it's safer to stop Hibernate from
being able to silently modify your live schema. Once your tables exist (via Option A once, or
Option B), change this in `application.properties` (or override via env var):

```
spring.jpa.hibernate.ddl-auto=validate
```

`validate` makes Hibernate check the schema matches the entities on startup and refuse to boot
if it doesn't — it will never `ALTER`/`DROP` anything for you. From then on, any schema change
goes through a reviewed SQL migration (or you re-run an updated `schema.sql`).

## Creating your first admin user

There's no separate admin sign-up flow — register a normal account through the app, then run:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

against your database (locally via `docker compose exec db psql -U postgres -d ai_interview`,
or via the `psql` connection string your PaaS gives you for production).

# Local setup (Windows)

How to get a Windows machine ready to build and run Ingredient Shopper. Versions are pinned in [plan §2](../specs/000-overview/plan.md#2-technology-stack).

| Tool | Version | Why |
|---|---|---|
| **Java JDK** (Eclipse Temurin) | 25 LTS | Backend and API tests |
| **.NET SDK** | 10 LTS | Frontend |
| **PostgreSQL** | 18 | Database |
| Maven | *nothing to install* | The Maven **wrapper** (`mvnw`) in each Java project downloads the right version itself |
| Git, `gh` | any recent | Already set up if you're reading this from a clone |

All commands below are for **PowerShell**. After installing anything, **open a new terminal** (or restart VS Code) so it picks up the new `PATH`.

---

## 1. Java 25 (Eclipse Temurin)

```powershell
winget install --id EclipseAdoptium.Temurin.25.JDK
```

Check it (in a **new** terminal):

```powershell
java -version        # should say: openjdk version "25.0.x"
echo $env:JAVA_HOME  # should print the JDK folder
```

If `JAVA_HOME` is empty, set it. The Maven wrapper uses it:

```powershell
$jdk = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory | Where-Object Name -like "jdk-25*" | Select-Object -First 1).FullName
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk, "User")
```

*Optional:* an IDE for Java. **IntelliJ IDEA Community** (free) is the most common choice for Spring Boot. VS Code with the "Extension Pack for Java" also works.

---

## 2. .NET 10 SDK

```powershell
dotnet --list-sdks   # should include a 10.0.x line
```

If it's missing:

```powershell
winget install --id Microsoft.DotNet.SDK.10
```

---

## 3. PostgreSQL 18

Install it **interactively**, so the installer asks for a password:

```powershell
winget install --id PostgreSQL.PostgreSQL.18 --interactive
```

In the installer:
- **Password for the `postgres` superuser:** choose one and remember it. It's only used for admin tasks like step 4.
- **Port:** keep **5432**.
- **Components:** the defaults are fine. You don't need Stack Builder.

Add PostgreSQL's tools (`psql`) to your `PATH`:

```powershell
$pg = "C:\Program Files\PostgreSQL\18\bin"
[Environment]::SetEnvironmentVariable("Path", [Environment]::GetEnvironmentVariable("Path", "User") + ";$pg", "User")
```

Check it (in a **new** terminal):

```powershell
psql --version       # should say: psql (PostgreSQL) 18.x
```

---

## 4. Create the databases

We use **two** databases, both owned by an app user called `shopper`:

| Database | Used by |
|---|---|
| `shopper` | Running the app locally |
| `shopper_test` | The API tests (they create lots of test data) |

Connect as the superuser (it asks for the password from step 3):

```powershell
psql -U postgres
```

Then run, choosing your own password for `shopper`:

```sql
CREATE ROLE shopper LOGIN PASSWORD 'choose-a-password';
CREATE DATABASE shopper      OWNER shopper;
CREATE DATABASE shopper_test OWNER shopper;
\q
```

Check the app user can connect:

```powershell
psql -U shopper -d shopper -c "select 'connected' as status;"
```

*The tables are created automatically by **Flyway** when the backend starts (from T-005 onwards). You never create them by hand.*

---

## 5. Configuration (`.env`)

```powershell
Copy-Item .env.example .env
```

Then edit `.env`:

| Variable | What to put |
|---|---|
| `DB_PASSWORD` | The `shopper` password from step 4 |
| `ADMIN_USERNAME` | Any username for the store's single admin account |
| `ADMIN_PASSWORD_BCRYPT` | A **bcrypt hash** of the admin password (see below), **not** the password itself |
| `SECRET_CODE_HMAC_KEY` | A long random string (see below) |
| `ANTHROPIC_API_KEY` | Only needed from feature 006. Get one at [console.anthropic.com](https://console.anthropic.com). Leave the placeholder until then |

**Generating the bcrypt hash** (Python is already installed on this machine):

```powershell
pip install bcrypt
python -c "import bcrypt, getpass; print(bcrypt.hashpw(getpass.getpass('Admin password: ').encode(), bcrypt.gensalt()).decode())"
```

**Generating the HMAC key:**

```powershell
python -c "import secrets; print(secrets.token_urlsafe(48))"
```

> ⚠️ **Never commit `.env`.** It's git-ignored, and `git status` should never list it.
>
> **No quotes around values.** The backend reads `.env` as a `.properties` file, where quotes become *part of the value*. Paste the bcrypt hash exactly as printed (it starts with `$2b$`).
>
> **Real environment variables win over `.env`.** For example, `$env:SERVER_PORT="8081"` before starting the backend overrides the port for that terminal.

---

## 6. Check everything

In a **new** terminal, from the repo root:

```powershell
java -version                                               # 25.x
dotnet --list-sdks                                          # includes 10.0.x
psql --version                                              # 18.x
psql -U shopper -d shopper_test -c "select 1;"              # returns 1
git check-ignore .env                                       # prints ".env" (ignored ✓)
```

If all five work, you're ready.

---

## Running the app

### Backend

From `backend/`, in PowerShell:

```powershell
.\mvnw.cmd spring-boot:run      # starts on SERVER_PORT (default 8081)
.\mvnw.cmd verify               # build + generate from the contract + unit tests
```

Check it's up: open <http://localhost:8081/actuator/health> and you should see `"status":"UP"`. That includes the database check.

- The first build downloads Maven and all dependencies, so it takes a few minutes. Later builds are fast.
- The API interfaces are **generated** from `contracts/openapi.yaml` into `backend/target/generated-sources/openapi` on every build. Never edit them.
- Use `mvnw.cmd` from PowerShell. The `./mvnw` script (for Git Bash, macOS and Linux) downloads Maven with `curl`, which can hit the certificate problems below on Windows.

### Frontend

From `frontend/Shopper.Web`:

```powershell
dotnet run          # serves on http://localhost:5000 and opens a browser
```

From `frontend/`:

```powershell
dotnet test         # builds everything (including the generated API client) and runs the tests
```

- The backend address comes from **`frontend/Shopper.Web/wwwroot/appsettings.json`** (`ApiBaseUrl`, default `http://localhost:8081`).
- ⚠️ That file is **downloaded by the browser**, so anyone can read it. **Never put secrets in it.**
- The typed API client is **generated** from `contracts/openapi.yaml` into `frontend/Shopper.Web/Api/Generated/` before each build (NSwag, ADR-014). It's git-ignored and never edited. It only regenerates when the contract changes.
- The backend only accepts browser calls from `CORS_ALLOWED_ORIGINS` (default `http://localhost:5000`). If you serve the frontend on another port, update that in `.env`.

### API tests

Black-box tests that call the **running** backend over HTTP and check every response against the contract (see [api-tests/README.md](../api-tests/README.md)). Start the backend first, then from `api-tests/`:

```powershell
.\mvnw.cmd test                                     # against http://localhost:8081
.\mvnw.cmd test "-DBASE_URL=http://localhost:9000"    # against another address
```

*Tip:* API tests create lots of data. Point the backend at the `shopper_test` database while running them, so your `shopper` database stays tidy for trying the app by hand:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/shopper_test"; cd backend; .\mvnw.cmd spring-boot:run
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `java`, `psql` or `dotnet` is "not recognised" | Open a **new** terminal (or restart VS Code) after installing or changing `PATH` |
| `mvnw` says `JAVA_HOME` is not set | Step 1, "If `JAVA_HOME` is empty" |
| `psql: password authentication failed` | You're using the wrong user's password. `postgres` and `shopper` have different ones |
| Port 5432 is already in use | Another PostgreSQL is running. Stop it, or use another port and update `DB_URL` in `.env` |
| `curl` fails with `CRYPT_E_NO_REVOCATION_CHECK` | A Windows `curl` quirk. Add `--ssl-no-revoke` |
| **Maven/Java fails with `PKIX path building failed`** | Your network inspects HTTPS and re-signs it with its own certificate (common on company networks). Windows trusts that certificate, but Java uses its own list. Tell Java to use the Windows list, **on your machine only** (never commit this): `[Environment]::SetEnvironmentVariable("JAVA_TOOL_OPTIONS", "-Djavax.net.ssl.trustStoreType=Windows-ROOT", "User")`, then open a new terminal. This covers both Maven's downloads and the running app's outbound calls (e.g. the Claude API in 006). Java will print "Picked up JAVA_TOOL_OPTIONS…" at startup, which is harmless |
| **`npx` fails with a certificate error** | The same cause. `$env:NODE_OPTIONS="--use-system-ca"` (or set it as a User variable like the line above) |
| **The port is already in use** (the health check gives an unexpected 404, or the backend fails to start) | Another program is on that port. The default is **8081**, because 8080 is often taken. Find the culprit with `Get-NetTCPConnection -LocalPort 8081 -State Listen`, or pick another port with `SERVER_PORT` in `.env`. If you change it, update the frontend's `ApiBaseUrl` to match (T-006) |

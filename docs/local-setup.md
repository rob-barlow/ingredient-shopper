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
> *How the backend reads `.env`, and so whether the bcrypt value needs quotes, is settled in **T-004**, which will update this section and `.env.example` if needed.*

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

*Added as the pieces exist:*
- **Backend:** T-004
- **Frontend:** T-006
- **API tests:** T-007

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `java`, `psql` or `dotnet` is "not recognised" | Open a **new** terminal (or restart VS Code) after installing or changing `PATH` |
| `mvnw` says `JAVA_HOME` is not set | Step 1, "If `JAVA_HOME` is empty" |
| `psql: password authentication failed` | You're using the wrong user's password. `postgres` and `shopper` have different ones |
| Port 5432 is already in use | Another PostgreSQL is running. Stop it, or use another port and update `DB_URL` in `.env` |
| `curl` fails with `CRYPT_E_NO_REVOCATION_CHECK` | A Windows `curl` quirk. Add `--ssl-no-revoke` |

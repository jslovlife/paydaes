# EXPLAIN.md — How to Build and Run the Paydaes POC

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture Decisions](#2-architecture-decisions)
3. [Prerequisites](#3-prerequisites)
4. [Windows vs macOS/Linux — Command Differences](#4-windows-vs-macoslinux--command-differences)
5. [IDE Setup (Important — Read Before Building)](#5-ide-setup-important--read-before-building)
6. [Quick Start (TL;DR)](#6-quick-start-tldr)
7. [Step-by-Step Setup](#7-step-by-step-setup)
8. [Building the Project](#8-building-the-project)
9. [Running the Services](#9-running-the-services)
10. [Running the Tests](#10-running-the-tests)
11. [API Walkthrough](#11-api-walkthrough)
12. [Configuration Reference](#12-configuration-reference)
13. [Resetting the Environment](#13-resetting-the-environment)
14. [Troubleshooting](#14-troubleshooting)

---

## 1. Project Overview

This is a **multi-module Maven project** that implements a multi-tenant SaaS HR platform.

```
poc/
├── entities/    # Shared JPA entities, repositories, DAOs, DTOs
├── tms/         # Tenant Management Service  — port 8081
├── corehr/      # CoreHR Service             — port 8082
├── scripts/     # Helper shell scripts
├── docker-compose.yml
├── keystore.jceks                       # AES-256 keystore (generate once — see below)
└── PaydaesTMS.postman_collection.json
```

### How the services relate

```
 HTTP Request (with X-Client-Id + X-Company-Id headers)
      │
      ▼
 [CoreHR :8082]  ──── on first tenant request ──►  [TMS :8081]
      │                                                  │
      │   fetches decrypted DB connection details        │
      │◄─────────────────────────────────────────────────┘
      │
      ├── routes to ──► [Client CommonDB]   (leave types, public holidays)
      └── routes to ──► [Company DB]        (employees)
```

- **TMS** stores connection metadata (host, port, db name) with **encrypted** credentials for every client and company.
- **CoreHR** never stores credentials. On the first request for a given tenant, it calls TMS, gets decrypted credentials, builds a HikariCP pool, and caches it for 30 minutes.

---

## 2. Architecture Decisions

### MySQL over H2
MySQL was chosen to simulate a realistic multi-tenant production environment where each client or company could have a physically separate database server.

### AES-256-GCM Credential Encryption
TMS encrypts every username and password using AES-256-GCM (authenticated encryption). The key lives in a JCEKS keystore — never hardcoded. A random 12-byte IV per encryption ensures identical passwords never produce identical ciphertext.

### Dynamic Datasource Routing (CoreHR)
CoreHR uses Spring's `AbstractRoutingDataSource` with a `ConcurrentHashMap` cache. Each datasource is identified by a `DataSourceKey` (`COMPANY:<id>` or `COMMON_DB:<id>`). The first request per tenant is the only cold start; subsequent requests use the cached pool. Cache entries expire after a configurable TTL to support credential rotation without restart.

### Tenant Context via `ThreadLocal`
A `TenantFilter` reads `X-Client-Id` and `X-Company-Id` HTTP headers on every request and stores them in `TenantContext` (a `ThreadLocal` wrapper). The default datasource is the company DB.

### CommonDB Routing via AOP
Services that operate on the client's shared commondb (leave types, public holidays) are annotated with `@UseCommonDb`. A `DataSourceRoutingAspect` intercepts these methods **before** `@Transactional` opens a DB connection, switching to the `COMMON_DB` datasource. The `finally` block restores the company DB when the method returns. This ordering is critical — switching inside the method body would be too late.

### Separate Connection Tables
`client_db_connections` and `company_db_connections` are kept as separate tables. A single table with an `is_common` flag was considered but rejected — it complicates foreign key constraints (one points to `clients`, the other to `companies`).

---

## 3. Prerequisites

| Tool | Minimum Version | Check |
|------|----------------|-------|
| Java JDK | 21 | `java -version` |
| Docker Desktop | Docker 24 | `docker --version` |
| Maven (or use the wrapper) | 3.9 | see Section 4 |

> **No need to install Maven separately.** The project ships with a Maven Wrapper (`mvnw` on Mac/Linux, `mvnw.cmd` on Windows).

### Windows — additional recommendation
Install **Git for Windows** (https://git-scm.com/download/win). It includes **Git Bash**, which lets you run `.sh` scripts and Unix-style commands without WSL. All shell examples in this guide can be run in Git Bash on Windows.

Alternatively, enable **WSL 2** (Windows Subsystem for Linux) and follow the macOS/Linux instructions verbatim inside a WSL terminal.

---

## 4. Windows vs macOS/Linux — Command Differences

Most commands in this guide are identical across platforms. The table below covers every difference you will encounter.

| Action | macOS / Linux | Windows (PowerShell) | Windows (Git Bash) |
|--------|--------------|---------------------|-------------------|
| Run Maven wrapper | `./mvnw` | `.\mvnw` | `./mvnw` |
| Copy a file | `cp src dst` | `Copy-Item src dst` | `cp src dst` |
| Docker Compose | `docker compose` | `docker compose` | `docker compose` |
| Run `.sh` scripts | `bash scripts/x.sh` | *(use Git Bash or WSL)* | `bash scripts/x.sh` |

> **Recommended for Windows:** Use **Git Bash** as your terminal throughout this guide. All `./mvnw`, `cp`, and `bash scripts/...` commands work as written.

> **PowerShell users:** Replace `./mvnw` with `.\mvnw` and `cp` with `Copy-Item` wherever they appear.

> **CMD users:** Replace `./mvnw` with `mvnw` (no prefix). Use `copy` instead of `cp`.

The rest of this guide uses macOS/Linux syntax. Refer to this table whenever a command does not work on Windows.

---

## 5. IDE Setup (Important — Read Before Building)

### Disable Java auto-build in your IDE

This project uses **MapStruct** to generate mapper implementations during Maven compilation. IDEs like IntelliJ IDEA, VS Code, and Cursor also run their own background Java compiler, which can overwrite Maven's correctly-generated class files with broken stubs.

**You must disable the IDE's background Java compilation before running Maven builds.**

#### VS Code / Cursor (Windows, macOS, Linux)
The project already includes `.vscode/settings.json` with the correct setting pre-configured:
```json
{
  "java.autobuild.enabled": false
}
```
After cloning or opening the project, reload the IDE:
- **Windows / Linux:** `Ctrl+Shift+P` → `Developer: Reload Window`
- **macOS:** `Cmd+Shift+P` → `Developer: Reload Window`

#### IntelliJ IDEA (Windows, macOS, Linux)
`File` → `Settings` (or `Preferences` on macOS) → `Build, Execution, Deployment` → `Build Tools` → `Maven` → tick **"Delegate IDE build/run actions to Maven"**.

> **Why this matters:** Without this setting, the IDE rewrites compiled `.class` files seconds after Maven produces them, causing `NoSuchBeanDefinitionException` for MapStruct mappers at runtime.

---

## 6. Quick Start (TL;DR)

> Windows users: use Git Bash, or substitute commands per Section 4.

```bash
# 1. Start databases
docker compose up -d

# 2. Generate the AES key (skip if keystore.jceks already exists in the project root)
keytool -genseckey -alias tms.aes.key -keyalg AES -keysize 256 \
  -storetype JCEKS -keystore keystore.jceks -storepass P@ssword -keypass P@ssword

# macOS/Linux/Git Bash
cp keystore.jceks tms/src/main/resources/keystore.jceks

# Windows PowerShell
# Copy-Item keystore.jceks tms\src\main\resources\keystore.jceks

# 3. Full build (always do this first, or after any change to entities/)
./mvnw clean install -DskipTests        # macOS/Linux/Git Bash
# .\mvnw clean install -DskipTests      # Windows PowerShell

# 4. Run TMS (terminal 1)
./mvnw spring-boot:run -pl tms

# 5. Run CoreHR (terminal 2)
./mvnw spring-boot:run -pl corehr
```

Import `PaydaesTMS.postman_collection.json` into Postman and start calling APIs.

---

## 7. Step-by-Step Setup

### Step 1 — Start the databases

```bash
docker compose up -d
```

This starts two MySQL 8.0 containers:

| Container | Host Port | Database | User | Password |
|-----------|-----------|----------|------|----------|
| `paydaes-mysql-tms` | `3306` | `tmsdb` | `paydaes` | `paydaes123` |
| `paydaes-mysql-corehr` | `3307` | `companydb` | `paydaes` | `paydaes123` |

Wait until both are healthy:
```bash
docker compose ps
# STATUS column should show "healthy" for both
```

> The `mysql-corehr` container at port 3307 is the **default** tenant database for local testing.
> In production, each client or company would have their own separate database server.

---

### Step 2 — Generate the AES-256 encryption keystore

TMS encrypts all database credentials before storing them. The encryption key must exist **before** TMS starts.

`keytool` ships with the JDK and works the same on Windows, macOS, and Linux.

```bash
keytool -genseckey \
  -alias tms.aes.key \
  -keyalg AES \
  -keysize 256 \
  -storetype JCEKS \
  -keystore keystore.jceks \
  -storepass P@ssword \
  -keypass P@ssword
```

Then copy it into the TMS resources folder so it is bundled into the JAR at build time:

**macOS / Linux / Git Bash:**
```bash
cp keystore.jceks tms/src/main/resources/keystore.jceks
```

**Windows PowerShell:**
```powershell
Copy-Item keystore.jceks tms\src\main\resources\keystore.jceks
```

**Windows CMD:**
```cmd
copy keystore.jceks tms\src\main\resources\keystore.jceks
```

Verify the keystore is valid:
```bash
keytool -list -storetype JCEKS -keystore keystore.jceks -storepass P@ssword
# Should print: tms.aes.key, SecretKeyEntry
```

> **If `keystore.jceks` already exists in the project root**, this step is done. Just make sure `tms/src/main/resources/keystore.jceks` also exists.

> **Security note:** In production, do NOT bundle the keystore inside the JAR. Store it at `/etc/paydaes/keystore.jceks` (or `C:\paydaes\keystore.jceks` on Windows) and set `tms.keystore.path=file:/etc/paydaes/keystore.jceks` in your environment properties.

---

### Step 3 — Build the entire project

Always run a full build on first setup, or after any change to `entities/`:

**macOS / Linux / Git Bash:**
```bash
./mvnw clean install -DskipTests
```

**Windows PowerShell:**
```powershell
.\mvnw clean install -DskipTests
```

**Windows CMD:**
```cmd
mvnw clean install -DskipTests
```

This builds all modules in dependency order — `entities` → `tms` → `corehr` — and installs each to your local Maven repository (`~/.m2` on Mac/Linux, `C:\Users\<you>\.m2` on Windows).

Expected output:
```
[INFO] entities .......................................... SUCCESS
[INFO] tms ............................................... SUCCESS
[INFO] corehr ............................................ SUCCESS
[INFO] BUILD SUCCESS
```

> **Important:** The `entities` module is shared by both `tms` and `corehr`. If you change any entity, DTO, or DAO in `entities/`, you **must** run the full build above before restarting the services — otherwise the other modules compile against the old version.

---

### Step 4 — Run TMS

```bash
./mvnw spring-boot:run -pl tms         # macOS/Linux/Git Bash
# .\mvnw spring-boot:run -pl tms       # Windows PowerShell
```

Wait for:
```
Started TmsApplication in X.XXX seconds
```

TMS is now running on **http://localhost:8081**

---

### Step 5 — Run CoreHR

Open a **second** terminal window:

```bash
./mvnw spring-boot:run -pl corehr      # macOS/Linux/Git Bash
# .\mvnw spring-boot:run -pl corehr    # Windows PowerShell
```

Wait for:
```
Started CorehrApplication in X.XXX seconds
```

CoreHR is now running on **http://localhost:8082**

> CoreHR does **not** connect to any database on startup. It only calls TMS when the first request arrives for a new tenant. Startup is always fast.

---

## 8. Building the Project

### Full build (recommended for first setup or after changing `entities/`)
```bash
./mvnw clean install -DskipTests
```

### Rebuild only `entities` then run a service
If you only changed something in `entities/`:
```bash
./mvnw clean install -DskipTests -pl entities
./mvnw spring-boot:run -pl corehr
```

### Run from a packaged JAR (avoids IDE compilation interference)
```bash
./mvnw clean package -DskipTests

# macOS/Linux
java -jar tms/target/tms-0.0.1-SNAPSHOT.jar
java -jar corehr/target/corehr-0.0.1-SNAPSHOT.jar

# Windows PowerShell / CMD (same command, different separator irrelevant here)
java -jar tms\target\tms-0.0.1-SNAPSHOT.jar
java -jar corehr\target\corehr-0.0.1-SNAPSHOT.jar
```

> **Do not** use `-am` with `spring-boot:run` (e.g. `./mvnw spring-boot:run -pl corehr -am`). The `-am` flag includes the root `poc` pom in the reactor and Maven will fail with "Unable to find a suitable main class".

---

## 9. Running the Tests

```bash
# Run all tests
./mvnw test

# Run tests for one module only
./mvnw test -pl tms
./mvnw test -pl corehr

# Run a specific test class
./mvnw test -pl tms -Dtest="DbConnectionServiceImplTest"
./mvnw test -pl corehr -Dtest="TenantFetchAndIsolationDemoTest"
```

> **No database required.** All tests use `@WebMvcTest` or plain Mockito — nothing connects to MySQL.

### Test summary

| Module | Test Class | What it covers |
|--------|-----------|----------------|
| TMS | `ClientControllerTest` | All client REST endpoints (HTTP layer) |
| TMS | `CompanyControllerTest` | All company REST endpoints |
| TMS | `DbConnectionControllerTest` | All connection endpoints, 201 vs 200 upsert, validation |
| TMS | `ClientServiceImplTest` | Client CRUD, duplicate email, cascade delete |
| TMS | `CompanyServiceImplTest` | Company CRUD, duplicate name per client, cascade delete |
| TMS | `DbConnectionServiceImplTest` | Credential encryption on save, decryption on read, toggle, upsert |
| TMS | `CredentialStorageAndEncryptionDemoTest` | **Demo** — end-to-end encryption lifecycle |
| CoreHR | `EmployeeControllerTest` | All employee endpoints, tenant header validation |
| CoreHR | `LeaveTypeControllerTest` | All leave type endpoints |
| CoreHR | `PublicHolidayControllerTest` | All public holiday endpoints |
| CoreHR | `TmsServiceClientTest` | TMS HTTP calls, 404 / unreachable error handling |
| CoreHR | `DynamicDataSourceCacheTest` | Cache hit/miss, eviction, schema initializer routing |
| CoreHR | `TenantContextTest` | ThreadLocal isolation, DB switching, thread independence |
| CoreHR | `TenantFetchAndIsolationDemoTest` | **Demo** — full tenant fetch + data isolation |

---

## 10. API Walkthrough

Import `PaydaesTMS.postman_collection.json` into Postman. The collection uses these variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `tms_base_url` | `http://localhost:8081` | TMS base URL |
| `corehr_base_url` | `http://localhost:8082` | CoreHR base URL |
| `client_id` | `1` | Set this after step 1 below |
| `company_id` | `1` | Set this after step 2 below |

---

### Full end-to-end flow

#### 1. Register a client in TMS
```
POST http://localhost:8081/api/tms/clients
Content-Type: application/json

{
  "name": "Acme Corporation",
  "email": "admin@acme.com",
  "phoneNumber": "+60123456789"
}
```
Save the returned `id` as `client_id`.

---

#### 2. Register a company under the client
```
POST http://localhost:8081/api/tms/companies/client/{client_id}
Content-Type: application/json

{
  "name": "Acme Sdn Bhd"
}
```
Save the returned `id` as `company_id`.

---

#### 3. Create the tenant databases

Each tenant needs its own MySQL database and user. Use the helper script (requires Docker to be running):

**macOS / Linux / Git Bash:**
```bash
# Client's common database (leave types, public holidays)
bash scripts/create-tenant-db.sh \
  --db-name acme_commondb \
  --db-user acme_common_user \
  --db-password "AcmeCommon@123"

# Company's own database (employees)
bash scripts/create-tenant-db.sh \
  --db-name acme_company_db \
  --db-user acme_company_user \
  --db-password "AcmeCompany@123"
```

**Windows PowerShell / CMD (no Git Bash):**
Run the SQL directly via `docker exec`:
```powershell
# Client commondb
docker exec -i paydaes-mysql-corehr mysql -uroot -prootpassword -e "CREATE DATABASE IF NOT EXISTS ``acme_commondb`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS 'acme_common_user'@'%' IDENTIFIED BY 'AcmeCommon@123'; GRANT ALL PRIVILEGES ON ``acme_commondb``.* TO 'acme_common_user'@'%'; FLUSH PRIVILEGES;"

# Company db
docker exec -i paydaes-mysql-corehr mysql -uroot -prootpassword -e "CREATE DATABASE IF NOT EXISTS ``acme_company_db`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS 'acme_company_user'@'%' IDENTIFIED BY 'AcmeCompany@123'; GRANT ALL PRIVILEGES ON ``acme_company_db``.* TO 'acme_company_user'@'%'; FLUSH PRIVILEGES;"
```

> **For the local POC**, you can skip this step entirely and reuse the existing `companydb` with the default `paydaes` / `paydaes123` credentials — just use `"databaseName": "companydb"` in the next two steps.

---

#### 4. Register the company's database connection in TMS
```
PUT http://localhost:8081/api/tms/connections/company/{company_id}
Content-Type: application/json

{
  "host": "localhost",
  "port": 3307,
  "databaseName": "acme_company_db",
  "username": "acme_company_user",
  "password": "AcmeCompany@123"
}
```
- Returns `201 Created` on first registration.
- Returns `200 OK` on subsequent updates (safe to re-run).
- TMS encrypts `username` and `password` with AES-256-GCM before persisting.

---

#### 5. Register the client's commondb connection in TMS
```
PUT http://localhost:8081/api/tms/connections/client/{client_id}/commondb
Content-Type: application/json

{
  "host": "localhost",
  "port": 3307,
  "databaseName": "acme_commondb",
  "username": "acme_common_user",
  "password": "AcmeCommon@123"
}
```

---

#### 6. Call CoreHR endpoints

All CoreHR requests require two tenant headers:

| Header | Value | Description |
|--------|-------|-------------|
| `X-Client-Id` | `{client_id}` | Identifies the client (for commondb routing) |
| `X-Company-Id` | `{company_id}` | Identifies the company DB to route to |

**Create an employee (hits company DB):**
```
POST http://localhost:8082/api/corehr/employees
X-Client-Id: 1
X-Company-Id: 1
Content-Type: application/json

{
  "employeeId": "EMP-001",
  "firstName": "Ahmad",
  "lastName": "Razif",
  "email": "ahmad@acme.com",
  "jobTitle": "Software Engineer",
  "department": "Engineering",
  "salary": 8500.00
}
```

**Create a leave type (hits client commondb):**
```
POST http://localhost:8082/api/corehr/leave-types
X-Client-Id: 1
X-Company-Id: 1
Content-Type: application/json

{
  "code": "ANNUAL",
  "name": "Annual Leave",
  "maxDaysPerYear": 14,
  "isPaid": true,
  "carryForwardDays": 5
}
```

**Create a public holiday (hits client commondb):**
```
POST http://localhost:8082/api/corehr/public-holidays
X-Client-Id: 1
X-Company-Id: 1
Content-Type: application/json

{
  "holidayDate": "2026-01-01",
  "name": "New Year's Day"
}
```

**On the very first request for a tenant**, CoreHR:
1. Reads `X-Client-Id` and `X-Company-Id` from the headers
2. Calls TMS to fetch the decrypted connection details
3. Builds and caches a HikariCP connection pool
4. Runs the schema initialiser (creates tables if they don't exist)
5. Executes the query and returns the result

All subsequent requests skip steps 1–4.

---

### CoreHR API summary

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/corehr/employees` | List all employees |
| `POST` | `/api/corehr/employees` | Create employee |
| `GET` | `/api/corehr/employees/{id}` | Get by ID |
| `PUT` | `/api/corehr/employees/{id}` | Full update |
| `PATCH` | `/api/corehr/employees/{id}/status` | Update status |
| `DELETE` | `/api/corehr/employees/{id}` | Delete |
| `GET` | `/api/corehr/leave-types` | List all leave types |
| `POST` | `/api/corehr/leave-types` | Create leave type |
| `GET` | `/api/corehr/leave-types/{id}` | Get by ID |
| `PUT` | `/api/corehr/leave-types/{id}` | Update leave type |
| `PATCH` | `/api/corehr/leave-types/{id}/toggle` | Toggle active/inactive |
| `DELETE` | `/api/corehr/leave-types/{id}` | Delete |
| `GET` | `/api/corehr/public-holidays?year={year}` | List holidays by year |
| `POST` | `/api/corehr/public-holidays` | Create public holiday |
| `GET` | `/api/corehr/public-holidays/{id}` | Get by ID |
| `PUT` | `/api/corehr/public-holidays/{id}` | Update holiday |
| `DELETE` | `/api/corehr/public-holidays/{id}` | Delete |

---

## 11. Configuration Reference

### TMS — `tms/src/main/resources/application.properties`

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8081` | TMS HTTP port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/tmsdb` | TMS own database |
| `spring.datasource.username` | `paydaes` | TMS DB username |
| `spring.datasource.password` | `paydaes123` | TMS DB password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-creates/updates TMS tables on startup |
| `tms.keystore.path` | `classpath:keystore.jceks` | Path to the JCEKS keystore |
| `tms.keystore.store-password` | `P@ssword` | Keystore store password |
| `tms.keystore.key-alias` | `tms.aes.key` | Key alias inside the keystore |
| `tms.keystore.key-password` | `P@ssword` | Key entry password |

### CoreHR — `corehr/src/main/resources/application.properties`

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8082` | CoreHR HTTP port |
| `tms.service.url` | `http://localhost:8081` | TMS base URL CoreHR calls |
| `corehr.tenant.pool.maximum-pool-size` | `5` | Max connections per tenant pool |
| `corehr.tenant.pool.minimum-idle` | `0` | Shrink to zero when idle |
| `corehr.tenant.pool.connection-timeout-ms` | `30000` | HikariCP connection timeout |
| `corehr.tenant.pool.idle-timeout-ms` | `600000` | Close idle connection after 10 min |
| `corehr.tenant.pool.keepalive-time-ms` | `120000` | Ping idle connection every 2 min |
| `corehr.tenant.pool.cache-ttl-seconds` | `1800` | Re-fetch connection from TMS after 30 min |
| `corehr.rest-template.connect-timeout-ms` | `5000` | Timeout connecting to TMS |
| `corehr.rest-template.read-timeout-ms` | `10000` | Timeout reading TMS response |

---

## 12. Resetting the Environment

### Stop everything
```bash
# Stop services: Ctrl+C in each terminal (same on all platforms)

# Stop databases
docker compose down
```

### Full reset — wipe all data
```bash
docker compose down -v   # removes containers AND volumes (all MySQL data gone)
docker compose up -d
```

### Reset one database only

**macOS / Linux / Git Bash:**
```bash
# Reset TMS database
docker compose stop mysql-tms && docker compose rm -f mysql-tms
docker volume rm paydaespoc_mysql_tms_data
docker compose up -d mysql-tms

# Reset CoreHR database
docker compose stop mysql-corehr && docker compose rm -f mysql-corehr
docker volume rm paydaespoc_mysql_corehr_data
docker compose up -d mysql-corehr
```

**Windows PowerShell / CMD:**
```powershell
# Docker Compose commands are identical on Windows
docker compose stop mysql-tms
docker compose rm -f mysql-tms
docker volume rm paydaespoc_mysql_tms_data
docker compose up -d mysql-tms
```

---

## 13. Troubleshooting

### `NoSuchBeanDefinitionException: LeaveTypeMapper` on startup

**Cause:** The IDE's background Java compiler overwrote Maven's correctly compiled MapStruct mapper with a broken stub.

**Fix:**
1. Ensure `.vscode/settings.json` has `"java.autobuild.enabled": false`
2. Reload the IDE (`Ctrl+Shift+P` / `Cmd+Shift+P` → `Developer: Reload Window`)
3. Run: `./mvnw clean install -DskipTests` (or `.\mvnw` on Windows PowerShell)
4. Then: `./mvnw spring-boot:run -pl corehr`

---

### Mapper compile error — "Unknown property" / "No property named X in source"

**Cause:** You changed something in `entities/` but did not reinstall the module before building `corehr`.

**Fix:**
```bash
./mvnw clean install -DskipTests -pl entities
./mvnw spring-boot:run -pl corehr
```

---

### `Unable to find a suitable main class` when building

**Cause:** You used `-am` with `spring-boot:run`, which includes the root `poc` pom module in the reactor.

**Fix:** Always use `-pl <module>` **without** `-am` for `spring-boot:run`:
```bash
# Correct
./mvnw spring-boot:run -pl corehr

# Wrong — will fail
./mvnw spring-boot:run -pl corehr -am
```

---

### CoreHR returns `Table 'xxx.leave_types' doesn't exist`

**Cause:** CoreHR routed the query to the company DB instead of the client commondb.

**Check:**
- Confirm the app was built after the latest code changes: `./mvnw clean install -DskipTests`
- In the logs, look for `Switching to common DB for ...` appearing before the Hibernate SQL
- Confirm the client commondb connection is registered in TMS

---

### CoreHR can't connect to tenant DB — `TenantResolutionException`

**Cause:** The DB connection is not registered in TMS, or TMS is not running.

**Fix:**
1. Confirm TMS is running: `curl http://localhost:8081/api/tms/clients` (or open in browser)
2. Register the connection via `PUT http://localhost:8081/api/tms/connections/company/{id}`
3. If the database itself doesn't exist yet, create it first (see Step 3 of the API Walkthrough)

---

### Credentials cannot be decrypted after TMS restart

**Cause:** The `keystore.jceks` file was regenerated. The new key cannot decrypt values encrypted by the old key.

**Fix:** Either restore the original keystore file, or do a full reset (`docker compose down -v`) and re-register all connections.

---

### `'.\mvnw' is not recognized` on Windows CMD

**Cause:** You are using Windows CMD which requires no prefix.

**Fix:** Use `mvnw` (no `.\`):
```cmd
mvnw clean install -DskipTests
mvnw spring-boot:run -pl tms
```

---

### `.sh` script does not run on Windows

**Cause:** Windows does not natively execute Bash scripts.

**Options:**
1. **Git Bash (recommended):** Open Git Bash and run `bash scripts/create-tenant-db.sh ...`
2. **WSL:** Open your WSL terminal and run the script from there
3. **Docker exec directly:** Use the `docker exec` commands shown in Section 10, Step 3

---

© 2025 Paydaes Sdn. Bhd. 202301048576 (1542490-W). All Rights Reserved.

# Paydaes POC - Multi-Module Project Requirements

## Project Overview

This document outlines the requirements and architecture for the Paydaes Proof of Concept (POC) multi-module Maven project. The project is structured into three main modules: **Entities**, **TMS**, and **CoreHR**.

## Architecture Overview

### Module Structure
```
poc/
├── entities/         # Shared entities, repositories, DTOs, and DAOs
├── tms/              # Client management service
├── corehr/           # Employee management service
└── pom.xml           # Parent POM configuration
```

## Module Specifications

### 1. Entities Module

**Purpose**: Contains all shared data models, repositories, DTOs, and data access objects.

**Components**:
- **Entities**: JPA entities representing database tables
  - `Client`: Client information with fields like name, email, phone number
  - `Employee`: Employee information with comprehensive HR fields

- **Repositories**: Spring Data JPA repositories for database operations
  - `ClientRepository`: CRUD operations and custom queries for clients
  - `EmployeeRepository`: CRUD operations and custom queries for employees

- **DTOs**: Data Transfer Objects for API communication
  - `ClientDto`: Client data transfer object
  - `EmployeeDto`: Employee data transfer object

- **DAOs**: Data Access Objects providing additional data access methods
  - `ClientDao`: Enhanced client data access operations
  - `EmployeeDao`: Enhanced employee data access operations

**Dependencies**:
- Spring Boot Starter Data JPA
- Spring Boot Starter Test

### 2. TMS (Tenant Management System) Module

**Purpose**: Service for creating and managing new clients with database connections.

**Key Features**:
- Client registration and management
- Client information CRUD operations
- Client search and filtering capabilities
- Database connection management for multi-tenant architecture

**API Endpoints**:
- `POST /api/tms/clients` - Create new client
- `GET /api/tms/clients` - Get all clients
- `GET /api/tms/clients/{id}` - Get client by ID
- `GET /api/tms/clients/email/{email}` - Get client by email
- `GET /api/tms/clients/search?name={name}` - Search clients by name
- `PUT /api/tms/clients/{id}` - Update client
- `DELETE /api/tms/clients/{id}` - Delete client
- `GET /api/tms/clients/count` - Get total client count

**Components**:
- `TmsApplication`: Main Spring Boot application class
- `ClientService`: Business logic for client management
- `ClientController`: REST API endpoints for client operations

**Configuration**:
- Server Port: 8081
- Database: H2 in-memory database (tmsdb)
- H2 Console: Enabled for development

**Dependencies**:
- Entities module
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 Database

### 3. CoreHR Module

**Purpose**: Service for comprehensive employee management and HR operations.

**Key Features**:
- Employee registration and profile management
- Employee status management (Active, Inactive, Terminated)
- Department and role management
- Employee search and filtering
- Salary and compensation tracking
- Employee lifecycle management

**API Endpoints**:
- `POST /api/corehr/employees` - Create new employee
- `GET /api/corehr/employees` - Get all employees
- `GET /api/corehr/employees/{id}` - Get employee by ID
- `GET /api/corehr/employees/employee-id/{employeeId}` - Get employee by employee ID
- `GET /api/corehr/employees/email/{email}` - Get employee by email
- `GET /api/corehr/employees/department/{department}` - Get employees by department
- `GET /api/corehr/employees/status/{status}` - Get employees by status
- `GET /api/corehr/employees/search?name={name}` - Search employees by name
- `PUT /api/corehr/employees/{id}` - Update employee
- `PATCH /api/corehr/employees/{id}/status` - Update employee status
- `DELETE /api/corehr/employees/{id}` - Delete employee
- `GET /api/corehr/employees/count/status/{status}` - Get employee count by status

**Components**:
- `CorehrApplication`: Main Spring Boot application class
- `EmployeeService`: Business logic for employee management
- `EmployeeController`: REST API endpoints for employee operations

**Configuration**:
- Server Port: 8082
- Database: H2 in-memory database (companydb)
- H2 Console: Enabled for development

**Dependencies**:
- Entities module
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 Database

## Technical Requirements

### Development Environment
- Java 21
- Spring Boot 3.5.3
- Maven for dependency management
- H2 Database for development and testing

### Database Schema

**Clients Table**:
- id (Primary Key, Auto-generated)
- name (Required)
- email (Unique, Required)
- phone_number
- created_at
- updated_at

**Employees Table**:
- id (Primary Key, Auto-generated)
- employee_id (Unique, Required)
- first_name (Required)
- last_name (Required)
- email (Unique, Required)
- phone_number
- hire_date
- job_title
- department
- salary
- status (ACTIVE, INACTIVE, TERMINATED)
- created_at
- updated_at

## Deployment Instructions

### Prerequisites

#### 1. Start MySQL databases
```bash
docker compose up -d
```
This starts two MySQL 8.0 containers:
- `paydaes-mysql-tms` on port **3306** → `tmsdb`
- `paydaes-mysql-corehr` on port **3307** → `companydb`

---

#### 2. Generate the AES-256 encryption key in a JCEKS Keystore

TMS encrypts all database credentials using AES-256-GCM. The secret key is stored securely in a Java JCEKS keystore.

```bash
# Generate a 256-bit AES secret key in a JCEKS keystore
keytool -genseckey \
  -alias tms.aes.key \
  -keyalg AES \
  -keysize 256 \
  -storetype JCEKS \
  -keystore keystore.jceks \
  -storepass P@ssword \
  -keypass P@ssword
```

```bash
# Verify the key entry was created successfully
keytool -list \
  -storetype JCEKS \
  -keystore keystore.jceks \
  -storepass P@ssword
```

Expected output:
```
Keystore type: JCEKS
Keystore provider: SunJCE
Your keystore contains 1 entry
tms.aes.key, <date>, SecretKeyEntry,
```

```bash
# Place the keystore in the TMS resources directory
cp keystore.jceks tms/src/main/resources/keystore.jceks
```

> **Security note:** `keystore.jceks` is listed in `.gitignore` and must never be committed.
> In prod, store it outside the application directory and set `tms.keystore.path` to the absolute file path (etc: `file:/etc/paydaes/keystore.jceks`).

---

### Building the Project
```bash
# Build all modules
./mvnw clean install -U

# Build specific module
./mvnw clean install -pl entities
./mvnw clean install -pl tms
./mvnw clean install -pl corehr
```

### Running the Services
```bash
# Run TMS service
./mvnw -pl tms spring-boot:run
# Access at: http://localhost:8081

# Run CoreHR service
./mvnw -pl corehr spring-boot:run
# Access at: http://localhost:8082
```

### Database Access

| Service | Host | Port | Database | Username | Password |
|---------|------|------|----------|----------|----------|
| TMS     | localhost | 3306 | tmsdb    | paydaes  | paydaes123 |
| CoreHR  | localhost | 3307 | companydb | paydaes | paydaes123 |

Connect with any MySQL client (e.g. DBeaver, TablePlus, MySQL Workbench) using the credentials above.

---

### Resetting the Database

> **Warning:** This permanently deletes all data and volumes. Use only in development.

```bash
# Stop containers and remove all data volumes
docker compose down -v

# Restart fresh — databases are recreated automatically
docker compose up -d
```

To reset a single service only:
```bash
# Reset TMS database only
docker compose stop mysql-tms
docker compose rm -f mysql-tms
docker volume rm paydaespoc_mysql_tms_data
docker compose up -d mysql-tms

# Reset CoreHR database only
docker compose stop mysql-corehr
docker compose rm -f mysql-corehr
docker volume rm paydaespoc_mysql_corehr_data
docker compose up -d mysql-corehr
```

## Development Guidelines

1. Follow Spring Boot best practices
2. Implement proper exception handling
3. Use appropriate HTTP status codes
4. Maintain consistent API response formats
5. Write comprehensive unit and integration tests
6. Document all public APIs
7. Follow Java coding standards and conventions
8. Implement proper logging for debugging and monitoring


© 2025 Paydaes Sdn. Bhd. 202301048576 (1542490-W). All Rights Reserved.

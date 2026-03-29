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
cd tms
./mvnw -pl tms spring-boot:run
# Access at: http://localhost:8081

# Run CoreHR service
cd corehr
./mvnw -pl corehr spring-boot:run
# Access at: http://localhost:8082
```

### Database Access
- TMS H2 Console: http://localhost:8081/h2-console
- CoreHR H2 Console: http://localhost:8082/h2-console
- JDBC URL: jdbc:h2:mem:tmsdb (for TMS) or jdbc:h2:mem:companydb (for CoreHR)
- Username: sa
- Password: password

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

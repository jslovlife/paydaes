# Paydaes POC Enhancement Test

## Introduction

This test is designed to assess your ability to work with a multi-tenant architecture, enhance existing services, and ensure the quality of your code through testing. You will be working on a proof-of-concept (POC) project called "Paydaes".

## Scenario

The Paydaes system is designed to serve multiple clients. The architecture is as follows:

*   **Client:** Represents a customer of the Paydaes system.
*   **CommonDB:** Each client has a central database (`commondb`) that stores information common to all of that client's entities.
*   **Company:** A client can have multiple companies. Each company has its own separate database (e.g., companydb) to store company-specific data.

Your task is to enhance the existing POC to support this multi-tenant, multi-database architecture.

## Project Modules

The project consists of the following modules:

*   `tms`: Tenant Management Service - Responsible for managing client and company information, including database connections.
*   `corehr`: Core HR Service - A multi-tenant service that provides HR functionalities for each company.
*   `entities`: Contains shared data entities.

## Tasks

### 1. TMS Service Enhancement

The `tms` service needs to manage the database connection details for all clients and their respective companies.

**Requirements:**

*   Implement a feature in the `tms` module to store connection information for:
    *   The `commondb` for each client.
    *   The individual database for each company associated with a client.
*   The database credentials (username and password) for all connections **must be encrypted** before being stored in the `tms` database. You can choose any suitable encryption algorithm (AES 256-Bit preferred).

### 2. CoreHR Service - Multi-tenancy Implementation

The `corehr` service is a multi-tenant service. It needs to be able to connect to the correct databases based on the context of an API call.

**Requirements:**

*   Re-implement the database connection mechanism in the `corehr` service.
*   When an API request is received (e.g., for a specific user or tenant), the `corehr` service must:
    1.  Fetch the encrypted database connection details for the relevant client's `commondb` and the specific company's database from the `tms` service.
    2.  Decrypt the credentials.
    3.  Establish connections to both the `commondb` and the company-specific database.
    4.  Ensure that any data returned by the API is correctly scoped to the specific company.

### 3. Unit Testing

Ensure the reliability of your implementation by writing comprehensive unit tests.

**Requirements:**

*   Write unit tests for the `tms` service to verify:
    *   Correct storage of connection details.
    *   Proper encryption of credentials.
*   Write unit tests for the `corehr` service to verify:
    *   Correctly fetching and decrypting connection details from `tms`.
    *   Successful connection to the correct tenant databases.
    *   Data isolation between companies.

### 4. Database and Deployment

You have the flexibility to choose the database technology.

**Requirements:**

*   The current project uses an H2 in-memory database. You are allowed to continue using H2 or switch to MySQL.
*   **If you choose to use MySQL**, you must provide a `docker-compose.yml` file. This file should define the necessary services (e.g., MySQL database, `tms` service, `corehr` service) to run the entire application stack with a single `docker-compose up` command, requiring no manual setup.
* Bonus point: You can apply any secret management for the sensitive data.

## Submission

Please provide your solution as a zip file. The repository should contain the complete source code with your enhancements and a `EXPLAIN.md` file explaining how to build and run the project.


© 2025 Paydaes Sdn. Bhd. 202301048576 (1542490-W). All Rights Reserved.

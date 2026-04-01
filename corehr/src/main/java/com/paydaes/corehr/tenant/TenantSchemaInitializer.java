package com.paydaes.corehr.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

// run once when new company db first connected - create the tables if not exist
// IF NOT EXISTS so safe to call again, wont blow up if table already there
@Slf4j
@Component
public class TenantSchemaInitializer {

    private static final String CREATE_EMPLOYEES_TABLE = """
            CREATE TABLE IF NOT EXISTS employees (
                id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                employee_id VARCHAR(255) NOT NULL UNIQUE,
                first_name  VARCHAR(255) NOT NULL,
                last_name   VARCHAR(255) NOT NULL,
                email       VARCHAR(255) NOT NULL UNIQUE,
                phone_number VARCHAR(255),
                hire_date   DATE,
                job_title   VARCHAR(255),
                department  VARCHAR(255),
                salary      DECIMAL(19, 2),
                status      VARCHAR(50),
                created_at  DATETIME(6) NOT NULL,
                updated_at  DATETIME(6) NOT NULL,
                created_by  VARCHAR(255),
                updated_by  VARCHAR(255)
            )
            """;

    public void initialize(DataSource dataSource) {
        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.execute(CREATE_EMPLOYEES_TABLE);
            log.info("Schema init done");
        } catch (Exception e) {
            log.warn("Schema init got issue (maybe table exist already): {}", e.getMessage());
        }
    }
}

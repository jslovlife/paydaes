package com.paydaes.corehr.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

// same idea as TenantSchemaInitializer but for client commondb
// runs once when a new client commondb pool is first created
@Slf4j
@Component
public class CommonDbSchemaInitializer {

    private static final String CREATE_LEAVE_TYPES = """
            CREATE TABLE IF NOT EXISTS leave_types (
                id                 BIGINT        AUTO_INCREMENT PRIMARY KEY,
                code               VARCHAR(20)   NOT NULL,
                name               VARCHAR(100)  NOT NULL,
                max_days_per_year  DECIMAL(5,1),
                is_paid            TINYINT(1)    NOT NULL DEFAULT 1,
                carry_forward_days INT           NOT NULL DEFAULT 0,
                is_active          TINYINT(1)    NOT NULL DEFAULT 1,
                created_at         DATETIME(6)   NOT NULL,
                updated_at         DATETIME(6)   NOT NULL,
                created_by         VARCHAR(255),
                updated_by         VARCHAR(255),
                CONSTRAINT uq_leave_type_code UNIQUE (code)
            )
            """;

    private static final String CREATE_PUBLIC_HOLIDAYS = """
            CREATE TABLE IF NOT EXISTS public_holidays (
                id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
                holiday_date DATE         NOT NULL,
                name         VARCHAR(100) NOT NULL,
                year         INT          NOT NULL,
                is_active    TINYINT(1)   NOT NULL DEFAULT 1,
                created_at   DATETIME(6)  NOT NULL,
                updated_at   DATETIME(6)  NOT NULL,
                created_by   VARCHAR(255),
                updated_by   VARCHAR(255),
                CONSTRAINT uq_public_holiday_date UNIQUE (holiday_date)
            )
            """;

    public void initialize(DataSource dataSource) {
        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.execute(CREATE_LEAVE_TYPES);
            jdbc.execute(CREATE_PUBLIC_HOLIDAYS);
            log.info("Common db schema init done");
        } catch (Exception e) {
            log.warn("Common db schema init issue (maybe tables exist already): {}", e.getMessage());
        }
    }
}

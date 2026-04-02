package com.paydaes.tms.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.paydaes.entities.model.tms")
@EnableJpaRepositories(basePackages = "com.paydaes.entities.repository.tms")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
}

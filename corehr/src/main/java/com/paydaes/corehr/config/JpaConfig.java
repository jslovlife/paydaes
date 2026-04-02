package com.paydaes.corehr.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//Isolate JPA Config
@Configuration
@EntityScan(basePackages = {
        "com.paydaes.entities.model.corehr",
        "com.paydaes.entities.model.commondb"
})
@EnableJpaRepositories(basePackages = {
        "com.paydaes.entities.repository.corehr",
        "com.paydaes.entities.repository.commondb"
})
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
}

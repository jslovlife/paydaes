package com.paydaes.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan(basePackages = "com.paydaes.entities.model.tms")
@EnableJpaRepositories(basePackages = "com.paydaes.entities.repository.tms")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication(scanBasePackages = {
        "com.paydaes.entities.dao.tms",
        "com.paydaes.entities.dto",
        "com.paydaes.entities.config",
        "com.paydaes.tms"
})
public class TmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsApplication.class, args);
    }
}

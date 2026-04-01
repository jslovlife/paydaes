package com.paydaes.corehr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {
        "com.paydaes.entities.model.corehr",
        "com.paydaes.entities.model.commondb"
})
@EnableJpaRepositories(basePackages = {
        "com.paydaes.entities.repository.corehr",
        "com.paydaes.entities.repository.commondb"
})
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class},
        scanBasePackages = {
                "com.paydaes.entities.dao.corehr",
                "com.paydaes.entities.dao.commondb",
                "com.paydaes.entities.config",
                "com.paydaes.corehr"
        }
)
public class CorehrApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorehrApplication.class, args);
    }
}

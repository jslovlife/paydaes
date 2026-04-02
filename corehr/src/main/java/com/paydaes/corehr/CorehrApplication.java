package com.paydaes.corehr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

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

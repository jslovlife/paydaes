package com.paydaes.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JPA setup (EntityScan, EnableJpaRepositories, EnableJpaAuditing) is in JpaConfig.java
// so @WebMvcTest tests can exclude it without needing a real datasource
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

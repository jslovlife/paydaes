package com.paydaes.corehr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "corehr.rest-template")
public class RestTemplateProperties {

    private int connectTimeoutMs = 5_000;

    private int readTimeoutMs = 10_000;
}

package com.paydaes.corehr.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "tms.service")
public class TmsClientProperties {

    @NotBlank(message = "tms.service.url must be configured")
    private String url;
}

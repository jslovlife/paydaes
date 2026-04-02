package com.paydaes.corehr.client;

import com.paydaes.corehr.config.TmsClientProperties;
import com.paydaes.corehr.exception.TenantResolutionException;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmsServiceClient {

    private final RestTemplate restTemplate;
    private final TmsClientProperties tmsClientProperties;

    public DbConnectionDto getCompanyDbConnection(Long companyId) {
        String url = tmsClientProperties.getUrl() + "/api/tms/connections/company/{companyId}";
        return call(url, companyId, "company " + companyId);
    }

    public DbConnectionDto getClientCommonDbConnection(Long clientId) {
        String url = tmsClientProperties.getUrl() + "/api/tms/connections/client/{clientId}/commondb";
        return call(url, clientId, "client " + clientId);
    }

    private DbConnectionDto call(String url, Long id, String logContext) {
        try {
            DbConnectionDto dto = restTemplate.getForObject(url, DbConnectionDto.class, id);
            if (dto == null) {
                throw new TenantResolutionException("TMS returned empty db connection response for " + logContext);
            }
            return dto;
        } catch (HttpClientErrorException.NotFound e) {
            throw new TenantResolutionException("No db connection registered in TMS for " + logContext);
        } catch (ResourceAccessException e) {
            throw new TenantResolutionException("Cannot reach TMS service at " + tmsClientProperties.getUrl());
        } catch (TenantResolutionException e) {
            throw e;
        } catch (Exception e) {
            throw new TenantResolutionException("Failed to fetch connection details for " + logContext, e);
        }
    }
}

package com.paydaes.corehr.client;

import com.paydaes.corehr.config.TmsClientProperties;
import com.paydaes.corehr.exception.TenantResolutionException;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmsServiceClientTest {

    @Mock 
    RestTemplate restTemplate;

    @Mock 
    TmsClientProperties tmsClientProperties;

    @InjectMocks 
    TmsServiceClient client;

    @BeforeEach
    void setup() {
        when(tmsClientProperties.getUrl()).thenReturn("http://tms:8081");
    }

    private void stubRestTemplate(DbConnectionDto returnValue) {
        when(restTemplate.getForObject(anyString(), eq(DbConnectionDto.class), (Object[]) any()))
                .thenReturn(returnValue);
    }

    @Test
    void getCompanyDbConnection_returnsConnectionDetails() {
        DbConnectionDto expected = makeConnDto("db-host", 3307, "abc_db");
        stubRestTemplate(expected);

        DbConnectionDto result = client.getCompanyDbConnection(1L);

        assertThat(result.getHost()).isEqualTo("db-host");
        assertThat(result.getDatabaseName()).isEqualTo("abc_db");
    }

    @Test
    void getCompanyDbConnection_callsCorrectTmsUrl() {
        stubRestTemplate(makeConnDto("localhost", 3307, "abc_db"));

        client.getCompanyDbConnection(5L);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(DbConnectionDto.class), argsCaptor.capture());

        assertThat(urlCaptor.getValue()).isEqualTo("http://tms:8081/api/tms/connections/company/{companyId}");
        assertThat(argsCaptor.getValue()).containsExactly(5L);
    }

    @Test
    void getCompanyDbConnection_tmsReturns404_throwsTenantResolutionException() {
        when(restTemplate.getForObject(anyString(), eq(DbConnectionDto.class), (Object[]) any()))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> client.getCompanyDbConnection(1L))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("No db connection registered in TMS for company 1");
    }

    @Test
    void getCompanyDbConnection_tmsUnreachable_throwsTenantResolutionException() {
        when(restTemplate.getForObject(anyString(), eq(DbConnectionDto.class), (Object[]) any()))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> client.getCompanyDbConnection(1L))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("Cannot reach TMS service at http://tms:8081");
    }

    @Test
    void getCompanyDbConnection_tmsReturnsNull_throwsTenantResolutionException() {
        when(restTemplate.getForObject(anyString(), eq(DbConnectionDto.class), (Object[]) any()))
                .thenReturn(null);

        assertThatThrownBy(() -> client.getCompanyDbConnection(1L))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("empty db connection response");
    }

    @Test
    void getClientCommonDbConnection_returnsConnectionDetails() {
        DbConnectionDto expected = makeConnDto("common-host", 3306, "abc_commondb");
        stubRestTemplate(expected);

        DbConnectionDto result = client.getClientCommonDbConnection(2L);

        assertThat(result.getDatabaseName()).isEqualTo("abc_commondb");
    }

    @Test
    void getClientCommonDbConnection_callsCorrectTmsUrl() {
        stubRestTemplate(makeConnDto("localhost", 3306, "abc_commondb"));

        client.getClientCommonDbConnection(3L);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(DbConnectionDto.class), argsCaptor.capture());

        assertThat(urlCaptor.getValue()).isEqualTo("http://tms:8081/api/tms/connections/client/{clientId}/commondb");
        assertThat(argsCaptor.getValue()).containsExactly(3L);
    }

    @Test
    void getClientCommonDbConnection_tmsReturns404_throwsTenantResolutionException() {
        when(restTemplate.getForObject(anyString(), eq(DbConnectionDto.class), (Object[]) any()))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> client.getClientCommonDbConnection(2L))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("No db connection registered in TMS for client 2");
    }

    @Test
    void getClientCommonDbConnection_tmsUnreachable_throwsTenantResolutionException() {
        when(restTemplate.getForObject(anyString(), eq(DbConnectionDto.class), (Object[]) any()))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> client.getClientCommonDbConnection(2L))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("Cannot reach TMS service");
    }

    private DbConnectionDto makeConnDto(String host, int port, String dbName) {
        return new DbConnectionDto(1L, true, host, port, dbName, "user", "pass", null, null);
    }
}

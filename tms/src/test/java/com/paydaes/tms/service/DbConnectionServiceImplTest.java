package com.paydaes.tms.service;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dao.tms.ClientDbConnectionDao;
import com.paydaes.entities.dao.tms.CompanyDao;
import com.paydaes.entities.dao.tms.CompanyDbConnectionDao;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.model.tms.ClientDbConnection;
import com.paydaes.entities.model.tms.Company;
import com.paydaes.entities.model.tms.CompanyDbConnection;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.impl.DbConnectionServiceImpl;
import com.paydaes.tms.util.AesEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbConnectionServiceImplTest {

    @Mock 
    ClientDbConnectionDao clientDbConnectionDao;

    @Mock 
    CompanyDbConnectionDao companyDbConnectionDao;

    @Mock 
    ClientDao clientDao;

    @Mock 
    CompanyDao companyDao;

    @Mock 
    AesEncryptionUtil aesEncryptionUtil;

    @InjectMocks 
    DbConnectionServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "currentKeyVersion", "v1");
        lenient().when(aesEncryptionUtil.encrypt(anyString())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        lenient().when(aesEncryptionUtil.decrypt(anyString())).thenAnswer(inv -> {
            String v = (String) inv.getArgument(0);
            return v.startsWith("ENC:") ? v.substring(4) : v;
        });
    }

    @Test
    void saveClientDbConnection_newConnection_returnsCreatedTrue() {
        Client client = makeClient(1L);
        DbConnectionDto dto = makeConnDto("db-host", 3306, "abc_common", "abc_user", "secret");
        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.empty());
        when(clientDbConnectionDao.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 10L));

        DbConnectionService.SaveResult<DbConnectionDto> result = service.saveClientDbConnection(1L, dto);

        assertThat(result.created()).isTrue();
        assertThat(result.data().getHost()).isEqualTo("db-host");
        assertThat(result.data().getDatabaseName()).isEqualTo("abc_common");
    }

    @Test
    void saveClientDbConnection_credentialsAreEncryptedBeforePersisting() {
        Client client = makeClient(1L);
        DbConnectionDto dto = makeConnDto("db-host", 3306, "abc_common", "abc_user", "p@ssw0rd");
        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.empty());

        ArgumentCaptor<ClientDbConnection> captor = ArgumentCaptor.forClass(ClientDbConnection.class);
        when(clientDbConnectionDao.save(captor.capture())).thenAnswer(inv -> withId(inv.getArgument(0), 10L));

        service.saveClientDbConnection(1L, dto);

        ClientDbConnection saved = captor.getValue();
        // stored encrypted values (username and password)
        assertThat(saved.getUsername()).isEqualTo("ENC:abc_user");
        assertThat(saved.getPassword()).isEqualTo("ENC:p@ssw0rd");
        verify(aesEncryptionUtil).encrypt("abc_user");
        verify(aesEncryptionUtil).encrypt("p@ssw0rd");
    }

    @Test
    void saveClientDbConnection_existingConnection_returnsCreatedFalse() {
        Client client = makeClient(1L);
        ClientDbConnection existing = makeClientConn(10L, client);
        DbConnectionDto dto = makeConnDto("new-host", 3307, "abc_common", "u", "p");
        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(existing));
        when(clientDbConnectionDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DbConnectionService.SaveResult<DbConnectionDto> result = service.saveClientDbConnection(1L, dto);

        assertThat(result.created()).isFalse();
        assertThat(result.data().getHost()).isEqualTo("new-host");
    }

    @Test
    void saveClientDbConnection_clientNotFound_throwsResourceNotFound() {
        when(clientDao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.saveClientDbConnection(99L, makeConnDto("h", 3306, "db", "u", "p")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getClientDbConnection_decryptsCredentialsOnRead() {
        Client client = makeClient(1L);
        ClientDbConnection conn = makeClientConn(10L, client);
        conn.setUsername("ENC:abc_user");
        conn.setPassword("ENC:p@ssw0rd");
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(conn));

        DbConnectionDto result = service.getClientDbConnection(1L);

        // DTO should contain plaintext credential
        assertThat(result.getUsername()).isEqualTo("abc_user");
        assertThat(result.getPassword()).isEqualTo("p@ssw0rd");
        verify(aesEncryptionUtil).decrypt("ENC:abc_user");
        verify(aesEncryptionUtil).decrypt("ENC:p@ssw0rd");
    }

    @Test
    void getClientDbConnection_inactiveConnection_throwsResourceNotFound() {
        Client client = makeClient(1L);
        ClientDbConnection conn = makeClientConn(10L, client);
        conn.setActive(false);
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(conn));

        assertThatThrownBy(() -> service.getClientDbConnection(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void getClientDbConnection_notFound_throwsResourceNotFound() {
        when(clientDbConnectionDao.findByClientId(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getClientDbConnection(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void toggleClientDbConnection_flipsActiveStatus() {
        Client client = makeClient(1L);
        ClientDbConnection conn = makeClientConn(10L, client);
        conn.setActive(true);
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(conn));
        when(clientDbConnectionDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DbConnectionDto result = service.toggleClientDbConnection(1L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void deleteClientDbConnection_removesRecord() {
        Client client = makeClient(1L);
        ClientDbConnection conn = makeClientConn(10L, client);
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(conn));

        service.deleteClientDbConnection(1L);

        verify(clientDbConnectionDao).deleteById(10L);
    }

    @Test
    void saveCompanyDbConnection_newConnection_returnsCreatedTrue() {
        Company company = makeCompany(1L);
        DbConnectionDto dto = makeConnDto("db-host", 3307, "abc_db", "abc_user", "s3cret");
        when(companyDao.findById(1L)).thenReturn(Optional.of(company));
        when(companyDbConnectionDao.findByCompanyId(1L)).thenReturn(Optional.empty());
        when(companyDbConnectionDao.save(any())).thenAnswer(inv -> withCompanyConnId(inv.getArgument(0), 20L));

        DbConnectionService.SaveResult<DbConnectionDto> result = service.saveCompanyDbConnection(1L, dto);

        assertThat(result.created()).isTrue();
        assertThat(result.data().getDatabaseName()).isEqualTo("abc_db");
    }

    @Test
    void saveCompanyDbConnection_credentialsAreEncryptedBeforePersisting() {
        Company company = makeCompany(1L);
        DbConnectionDto dto = makeConnDto("db-host", 3307, "abc_db", "abc_user", "s3cret");
        when(companyDao.findById(1L)).thenReturn(Optional.of(company));
        when(companyDbConnectionDao.findByCompanyId(1L)).thenReturn(Optional.empty());

        ArgumentCaptor<CompanyDbConnection> captor = ArgumentCaptor.forClass(CompanyDbConnection.class);
        when(companyDbConnectionDao.save(captor.capture())).thenAnswer(inv -> withCompanyConnId(inv.getArgument(0), 20L));

        service.saveCompanyDbConnection(1L, dto);

        CompanyDbConnection saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("ENC:abc_user");
        assertThat(saved.getPassword()).isEqualTo("ENC:s3cret");
        verify(aesEncryptionUtil).encrypt("abc_user");
        verify(aesEncryptionUtil).encrypt("s3cret");
    }

    @Test
    void getCompanyDbConnection_decryptsCredentialsOnRead() {
        Company company = makeCompany(1L);
        CompanyDbConnection conn = makeCompanyConn(20L, company);
        conn.setUsername("ENC:abc_user");
        conn.setPassword("ENC:s3cret");
        when(companyDbConnectionDao.findByCompanyId(1L)).thenReturn(Optional.of(conn));

        DbConnectionDto result = service.getCompanyDbConnection(1L);

        assertThat(result.getUsername()).isEqualTo("abc_user");
        assertThat(result.getPassword()).isEqualTo("s3cret");
    }

    @Test
    void getCompanyDbConnection_inactiveConnection_throwsResourceNotFound() {
        Company company = makeCompany(1L);
        CompanyDbConnection conn = makeCompanyConn(20L, company);
        conn.setActive(false);
        when(companyDbConnectionDao.findByCompanyId(1L)).thenReturn(Optional.of(conn));

        assertThatThrownBy(() -> service.getCompanyDbConnection(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void getCompanyDbConnectionsByClientId_returnsAllCompanyConnections() {
        Company c1 = makeCompany(1L);
        Company c2 = makeCompany(2L);
        when(companyDao.findByClientId(10L)).thenReturn(List.of(c1, c2));
        when(companyDbConnectionDao.findByCompanyId(1L)).thenReturn(Optional.of(makeCompanyConn(100L, c1)));
        when(companyDbConnectionDao.findByCompanyId(2L)).thenReturn(Optional.of(makeCompanyConn(101L, c2)));

        List<DbConnectionDto> results = service.getCompanyDbConnectionsByClientId(10L);

        assertThat(results).hasSize(2);
    }

    private Client makeClient(Long id) {
        Client c = new Client("Test Corp", "test@corp.com", "+60123456789");
        c.setId(id);
        return c;
    }

    private Company makeCompany(Long id) {
        Client client = makeClient(10L);
        Company c = new Company("ABC Sdn Bhd", client);
        c.setId(id);
        return c;
    }

    private ClientDbConnection makeClientConn(Long id, Client client) {
        ClientDbConnection c = new ClientDbConnection();
        c.setId(id);
        c.setClient(client);
        c.setActive(true);
        c.setHost("localhost");
        c.setPort(3306);
        c.setDatabaseName("test_db");
        c.setUsername("ENC:user");
        c.setPassword("ENC:pass");
        return c;
    }

    private CompanyDbConnection makeCompanyConn(Long id, Company company) {
        CompanyDbConnection c = new CompanyDbConnection();
        c.setId(id);
        c.setCompany(company);
        c.setActive(true);
        c.setHost("localhost");
        c.setPort(3307);
        c.setDatabaseName("company_db");
        c.setUsername("ENC:user");
        c.setPassword("ENC:pass");
        return c;
    }

    private DbConnectionDto makeConnDto(String host, int port, String db, String user, String pass) {
        return new DbConnectionDto(null, true, host, port, db, user, pass, null, null);
    }

    private ClientDbConnection withId(ClientDbConnection conn, Long id) {
        conn.setId(id);
        return conn;
    }

    private CompanyDbConnection withCompanyConnId(CompanyDbConnection conn, Long id) {
        conn.setId(id);
        return conn;
    }
}

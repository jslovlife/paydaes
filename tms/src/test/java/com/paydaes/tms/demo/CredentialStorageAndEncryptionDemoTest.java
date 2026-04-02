package com.paydaes.tms.demo;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dao.tms.ClientDbConnectionDao;
import com.paydaes.entities.dao.tms.CompanyDao;
import com.paydaes.entities.dao.tms.CompanyDbConnectionDao;
import com.paydaes.entities.dto.tms.DbConnectionDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.model.tms.ClientDbConnection;
import com.paydaes.entities.model.tms.Company;
import com.paydaes.entities.model.tms.CompanyDbConnection;
import com.paydaes.tms.service.DbConnectionService;
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

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CredentialStorageAndEncryptionDemoTest {

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

    // Simulate a real AES-GCM encryption / decryption
    @BeforeEach
    void wireUpFakeEncryption() {
        ReflectionTestUtils.setField(service, "currentKeyVersion", "v1");
        lenient().when(aesEncryptionUtil.encrypt(anyString()))
                .thenAnswer(inv -> "ENC$" + Base64.getEncoder().encodeToString(
                        ((String) inv.getArgument(0)).getBytes()));
        lenient().when(aesEncryptionUtil.decrypt(anyString()))
                .thenAnswer(inv -> {
                    String v = inv.getArgument(0).toString();
                    return v.startsWith("ENC$")
                            ? new String(Base64.getDecoder().decode(v.substring(4)))
                            : v;
                });
    }

    @Test
    void tms_storesConnectionDetailsCorrectly_andEncryptsCredentials() {

        // STEP 1 — Admin create client commondb connection with plaintext credentials
        Client abc = new Client("ABC Corp", "admin@abc.com", "+60123456789");
        abc.setId(1L);

        DbConnectionDto input = new DbConnectionDto(
                null, true,
                "testdomain", 3306, "abc_commondb",
                "abc_db_user",
                "P@sSw0rd!23",
                null, null
        );

        when(clientDao.findById(1L)).thenReturn(Optional.of(abc));
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.empty()); 

        // capture the saved client connection
        ArgumentCaptor<ClientDbConnection> clientConnCaptor = ArgumentCaptor.forClass(ClientDbConnection.class);
        when(clientDbConnectionDao.save(clientConnCaptor.capture())).thenAnswer(inv -> {
            ClientDbConnection c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        // save the client connection
        DbConnectionService.SaveResult<DbConnectionDto> saveResult =
                service.saveClientDbConnection(1L, input);

        // STEP 2 — Username and password in the persisted entity must be encrypted
        ClientDbConnection persisted = clientConnCaptor.getValue();

        String expectedEncUser = "ENC$" + Base64.getEncoder().encodeToString("abc_db_user".getBytes());
        String expectedEncPass = "ENC$" + Base64.getEncoder().encodeToString("P@sSw0rd!23".getBytes());

        assertThat(persisted.getUsername())
                .as("username stored in DB must be encrypted")
                .isEqualTo(expectedEncUser)
                .doesNotContain("abc_db_user"); 

        assertThat(persisted.getPassword())
                .as("password stored in DB must be encrypted")
                .isEqualTo(expectedEncPass)
                .doesNotContain("P@sSw0rd!23"); 

        verify(aesEncryptionUtil).encrypt("abc_db_user");
        verify(aesEncryptionUtil).encrypt("P@sSw0rd!23");

        // STEP 3 — Non-sensitive fields (host, port, databaseName) are stored as encrypted form

        assertThat(persisted.getHost()).isEqualTo("testdomain");
        assertThat(persisted.getPort()).isEqualTo(3306);
        assertThat(persisted.getDatabaseName()).isEqualTo("abc_commondb");
        assertThat(persisted.getKeyVersion()).isEqualTo("v1");

        assertThat(saveResult.created())
                .as("first time saving this client connection must be flagged as a new record")
                .isTrue();

        // STEP 4 — When a consumer reads the record back, credentials must be decrypted and returned as plaintext into DTO

        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(persisted));

        DbConnectionDto readBack = service.getClientDbConnection(1L);

        assertThat(readBack.getUsername())
                .as("consumer reads plaintext username")
                .isEqualTo("abc_db_user");

        assertThat(readBack.getPassword())
                .as("consumer reads plaintext password")
                .isEqualTo("P@sSw0rd!23");

        assertThat(readBack.getHost()).isEqualTo("testdomain");
        assertThat(readBack.getDatabaseName()).isEqualTo("abc_commondb");

        verify(aesEncryptionUtil, atLeastOnce()).decrypt(expectedEncUser);
        verify(aesEncryptionUtil, atLeastOnce()).decrypt(expectedEncPass);

        // STEP 5 — Same encryption applies for company-specific DB connections

        Company abcHq = new Company("ABC HQ", abc);
        abcHq.setId(5L);

        DbConnectionDto companyInput = new DbConnectionDto(
                null, true,
                "testcompanydomain", 3307, "abc_company_db",
                "abc_db_user", "C0mpanyP@ss",
                null, null
        );

        when(companyDao.findById(5L)).thenReturn(Optional.of(abcHq));
        when(companyDbConnectionDao.findByCompanyId(5L)).thenReturn(Optional.empty());
        ArgumentCaptor<CompanyDbConnection> companyConnCaptor = ArgumentCaptor.forClass(CompanyDbConnection.class);
        when(companyDbConnectionDao.save(companyConnCaptor.capture())).thenAnswer(inv -> {
            CompanyDbConnection c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });

        service.saveCompanyDbConnection(5L, companyInput);

        CompanyDbConnection persistedCompanyConn = companyConnCaptor.getValue();
        assertThat(persistedCompanyConn.getUsername())
                .isEqualTo("ENC$" + Base64.getEncoder().encodeToString("abc_db_user".getBytes()))
                .doesNotContain("abc_db_user");
        assertThat(persistedCompanyConn.getPassword())
                .isEqualTo("ENC$" + Base64.getEncoder().encodeToString("C0mpanyP@ss".getBytes()))
                .doesNotContain("C0mpanyP@ss");
        assertThat(persistedCompanyConn.getHost()).isEqualTo("testcompanydomain");
        assertThat(persistedCompanyConn.getDatabaseName()).isEqualTo("abc_company_db");

        // STEP 6 — Updating an existing connection will return created=false

        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(persisted));
        doAnswer(inv -> inv.getArgument(0)).when(clientDbConnectionDao).save(any());

        DbConnectionDto updatedInput = new DbConnectionDto(
                null, true,
                "testdomain", 3306, "abc_commondb",
                "abc_db_user", "P@sSw0rd!23",
                null, null
        );

        DbConnectionService.SaveResult<DbConnectionDto> updateResult =
                service.saveClientDbConnection(1L, updatedInput);

        assertThat(updateResult.created())
                .as("updating an existing connection must not create a second row")
                .isFalse();
        assertThat(updateResult.data().getHost()).isEqualTo("testdomain");
    }
}

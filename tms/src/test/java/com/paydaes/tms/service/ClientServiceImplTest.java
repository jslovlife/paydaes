package com.paydaes.tms.service;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dao.tms.ClientDbConnectionDao;
import com.paydaes.entities.dao.tms.CompanyDao;
import com.paydaes.entities.dao.tms.CompanyDbConnectionDao;
import com.paydaes.entities.dto.tms.ClientDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.model.tms.ClientDbConnection;
import com.paydaes.entities.model.tms.Company;
import com.paydaes.entities.model.tms.CompanyDbConnection;
import com.paydaes.tms.exception.DuplicateResourceException;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock 
    ClientDao clientDao;

    @Mock 
    CompanyDao companyDao;
    
    @Mock 
    ClientDbConnectionDao clientDbConnectionDao;
    
    @Mock 
    CompanyDbConnectionDao companyDbConnectionDao;

    @InjectMocks 
    ClientServiceImpl service;

    @Test
    void createClient_savesAndReturnsDto() {
        ClientDto input = new ClientDto(null, "ABC Corp", "admin@abc.com", "+60123456789", null, null);
        Client saved = makeClient(1L, "ABC Corp", "admin@abc.com");
        when(clientDao.existsByEmail("admin@abc.com")).thenReturn(false);
        when(clientDao.save(any())).thenReturn(saved);

        ClientDto result = service.createClient(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ABC Corp");
        assertThat(result.getEmail()).isEqualTo("admin@abc.com");
    }

    @Test
    void createClient_duplicateEmail_throwsDuplicateResourceException() {
        ClientDto input = new ClientDto(null, "ABC Corp", "admin@abc.com", null, null, null);
        when(clientDao.existsByEmail("admin@abc.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createClient(input))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin@abc.com");
    }

    @Test
    void getClientById_found_returnsOptionalWithDto() {
        when(clientDao.findById(1L)).thenReturn(Optional.of(makeClient(1L, "ABC", "a@b.com")));

        Optional<ClientDto> result = service.getClientById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getClientById_notFound_returnsEmpty() {
        when(clientDao.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getClientById(99L)).isEmpty();
    }

    @Test
    void getAllClients_returnsAllMapped() {
        when(clientDao.findAll()).thenReturn(List.of(
                makeClient(1L, "ABC", "a@a.com"),
                makeClient(2L, "BCD", "b@b.com")
        ));

        List<ClientDto> result = service.getAllClients();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("ABC");
    }

    @Test
    void getTotalClientCount_returnsDaoValue() {
        when(clientDao.countTotalClients()).thenReturn(7L);
        assertThat(service.getTotalClientCount()).isEqualTo(7L);
    }

    @Test
    void updateClient_updatesFieldsAndReturnsDto() {
        Client existing = makeClient(1L, "Old Name", "old@abc.com");
        when(clientDao.findById(1L)).thenReturn(Optional.of(existing));
        when(clientDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientDto update = new ClientDto(null, "New Name", "new@abc.com", "+60199999999", null, null);
        ClientDto result = service.updateClient(1L, update);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@abc.com");
    }

    @Test
    void updateClient_notFound_throwsResourceNotFound() {
        when(clientDao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateClient(99L, new ClientDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteClient_cascadesDeleteToCompaniesAndTheirDbConnections() {
        Client client = makeClient(1L, "ABC", "a@a.com");
        Company company1 = makeCompany(10L, client);
        Company company2 = makeCompany(11L, client);
        CompanyDbConnection conn1 = makeCompanyConn(100L);
        CompanyDbConnection conn2 = makeCompanyConn(101L);

        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(companyDao.findByClientId(1L)).thenReturn(List.of(company1, company2));
        when(companyDbConnectionDao.findByCompanyId(10L)).thenReturn(Optional.of(conn1));
        when(companyDbConnectionDao.findByCompanyId(11L)).thenReturn(Optional.of(conn2));
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.empty());

        service.deleteClient(1L);

        // remove company db conn first
        verify(companyDbConnectionDao).deleteById(100L);
        verify(companyDbConnectionDao).deleteById(101L);
        // then remove company
        verify(companyDao).deleteById(10L);
        verify(companyDao).deleteById(11L);
        // then remove client
        verify(clientDao).deleteById(1L);
    }

    @Test
    void deleteClient_alsoRemovesClientCommonDbConnection() {
        Client client = makeClient(1L, "ABC", "a@a.com");
        ClientDbConnection clientConn = new ClientDbConnection();
        clientConn.setId(50L);

        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(companyDao.findByClientId(1L)).thenReturn(List.of());
        when(clientDbConnectionDao.findByClientId(1L)).thenReturn(Optional.of(clientConn));

        service.deleteClient(1L);

        verify(clientDbConnectionDao).deleteById(50L);
        verify(clientDao).deleteById(1L);
    }

    @Test
    void deleteClient_notFound_throwsResourceNotFound() {
        when(clientDao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteClient(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clientDao, never()).deleteById(any());
    }

    private Client makeClient(Long id, String name, String email) {
        Client c = new Client(name, email, null);
        c.setId(id);
        return c;
    }

    private Company makeCompany(Long id, Client client) {
        Company c = new Company("Company " + id, client);
        c.setId(id);
        return c;
    }

    private CompanyDbConnection makeCompanyConn(Long id) {
        CompanyDbConnection c = new CompanyDbConnection();
        c.setId(id);
        return c;
    }
}

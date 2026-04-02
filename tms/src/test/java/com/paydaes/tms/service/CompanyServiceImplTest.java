package com.paydaes.tms.service;

import com.paydaes.entities.dao.tms.ClientDao;
import com.paydaes.entities.dao.tms.CompanyDao;
import com.paydaes.entities.dao.tms.CompanyDbConnectionDao;
import com.paydaes.entities.dto.tms.CompanyDto;
import com.paydaes.entities.model.tms.Client;
import com.paydaes.entities.model.tms.Company;
import com.paydaes.entities.model.tms.CompanyDbConnection;
import com.paydaes.tms.exception.DuplicateResourceException;
import com.paydaes.tms.exception.ResourceNotFoundException;
import com.paydaes.tms.service.impl.CompanyServiceImpl;
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
class CompanyServiceImplTest {

    @Mock 
    CompanyDao companyDao;
    
    @Mock 
    ClientDao clientDao;
    
    @Mock 
    CompanyDbConnectionDao companyDbConnectionDao;

    @InjectMocks 
    CompanyServiceImpl service;

    @Test
    void createCompany_savesAndReturnsDto() {
        Client client = makeClient(1L);
        Company saved  = makeCompany(10L, "ABC HQ", client);
        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(companyDao.existsByNameAndClientId("ABC HQ", 1L)).thenReturn(false);
        when(companyDao.save(any())).thenReturn(saved);

        CompanyDto result = service.createCompany(1L, new CompanyDto(null, "ABC HQ", null, null, null, null));

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("ABC HQ");
        assertThat(result.getClientId()).isEqualTo(1L);
    }

    @Test
    void createCompany_clientNotFound_throwsResourceNotFound() {
        when(clientDao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createCompany(99L, new CompanyDto(null, "X", null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createCompany_duplicateNameUnderSameClient_throwsDuplicateResourceException() {
        Client client = makeClient(1L);
        when(clientDao.findById(1L)).thenReturn(Optional.of(client));
        when(companyDao.existsByNameAndClientId("ABC HQ", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.createCompany(1L, new CompanyDto(null, "ABC HQ", null, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ABC HQ");
    }

    @Test
    void createCompany_sameNameDifferentClients_isAllowed() {
        Client client2 = makeClient(2L);
        Company saved = makeCompany(20L, "ABC HQ", client2);
        when(clientDao.findById(2L)).thenReturn(Optional.of(client2));
        when(companyDao.existsByNameAndClientId("ABC HQ", 2L)).thenReturn(false);
        when(companyDao.save(any())).thenReturn(saved);

        // same name allowed for different clients
        assertThatCode(() -> service.createCompany(2L, new CompanyDto(null, "ABC HQ", null, null, null, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void getCompanyById_found_returnsOptionalWithDto() {
        Client client = makeClient(1L);
        when(companyDao.findById(10L)).thenReturn(Optional.of(makeCompany(10L, "HQ", client)));

        Optional<CompanyDto> result = service.getCompanyById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getClientId()).isEqualTo(1L);
        assertThat(result.get().getClientName()).isEqualTo("ABC Corp");
    }

    @Test
    void getCompaniesByClientId_returnsAll() {
        Client client = makeClient(1L);
        when(companyDao.findByClientId(1L)).thenReturn(List.of(
                makeCompany(10L, "HQ1", client),
                makeCompany(11L, "HQ2", client)
        ));

        List<CompanyDto> result = service.getCompaniesByClientId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void updateCompany_updatesNameAndReturnsDto() {
        Client client = makeClient(1L);
        Company company = makeCompany(10L, "Old Name", client);
        when(companyDao.findById(10L)).thenReturn(Optional.of(company));
        when(companyDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CompanyDto result = service.updateCompany(10L, new CompanyDto(null, "New Name", null, null, null, null));

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void updateCompany_notFound_throwsResourceNotFound() {
        when(companyDao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateCompany(99L, new CompanyDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCompany_removesDbConnectionBeforeDeletingCompany() {
        Company company = makeCompany(10L, "HQ", makeClient(1L));
        CompanyDbConnection conn = new CompanyDbConnection();
        conn.setId(100L);
        when(companyDao.findById(10L)).thenReturn(Optional.of(company));
        when(companyDbConnectionDao.findByCompanyId(10L)).thenReturn(Optional.of(conn));

        service.deleteCompany(10L);

        // remove company db conn first
        inOrder(companyDbConnectionDao, companyDao).verify(companyDbConnectionDao).deleteById(100L);
        // then remove company
        inOrder(companyDbConnectionDao, companyDao).verify(companyDao).deleteById(10L);
    }

    @Test
    void deleteCompany_noDbConnection_stillDeletesCompany() {
        when(companyDao.findById(10L)).thenReturn(Optional.of(makeCompany(10L, "HQ", makeClient(1L))));
        when(companyDbConnectionDao.findByCompanyId(10L)).thenReturn(Optional.empty());

        service.deleteCompany(10L);

        verify(companyDbConnectionDao, never()).deleteById(any());
        verify(companyDao).deleteById(10L);
    }

    @Test
    void deleteCompany_notFound_throwsResourceNotFound() {
        when(companyDao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteCompany(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(companyDao, never()).deleteById(any());
    }

    private Client makeClient(Long id) {
        Client c = new Client("ABC Corp", "admin@abc.com", null);
        c.setId(id);
        return c;
    }

    private Company makeCompany(Long id, String name, Client client) {
        Company c = new Company(name, client);
        c.setId(id);
        return c;
    }
}

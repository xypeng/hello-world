package com.trading.service;

import com.trading.model.Customer;
import com.trading.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setCustomerCode("C001");
        testCustomer.setCustomerName("Test Customer");
        testCustomer.setCustomerType("INSTITUTIONAL");
        testCustomer.setStatus("ACTIVE");
    }
    
    @Test
    void findAll_ShouldReturnAllCustomers() {
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Customer> result = customerService.findAll();
        
        assertEquals(1, result.size());
        assertEquals("C001", result.get(0).getCustomerCode());
    }
    
    @Test
    void findById_ShouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        Optional<Customer> result = customerService.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("C001", result.get().getCustomerCode());
    }
    
    @Test
    void findByCustomerCode_ShouldReturnCustomer() {
        when(customerRepository.findByCustomerCode("C001")).thenReturn(Optional.of(testCustomer));
        
        Optional<Customer> result = customerService.findByCustomerCode("C001");
        
        assertTrue(result.isPresent());
        assertEquals("Test Customer", result.get().getCustomerName());
    }
    
    @Test
    void save_ShouldPersistCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        
        Customer saved = customerService.save(testCustomer);
        
        assertNotNull(saved);
        assertEquals("C001", saved.getCustomerCode());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
    
    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(customerRepository).deleteById(1L);
        
        customerService.deleteById(1L);
        
        verify(customerRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void existsByCustomerCode_ShouldReturnTrue() {
        when(customerRepository.existsByCustomerCode("C001")).thenReturn(true);
        
        boolean exists = customerService.existsByCustomerCode("C001");
        
        assertTrue(exists);
    }
}
package com.trading.service;

import com.trading.model.Account;
import com.trading.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    private Account testAccount;
    
    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setCustomerId(1L);
        testAccount.setAccountCode("ACC001");
        testAccount.setCurrency("USD");
        testAccount.setBalance(new BigDecimal("100000.00"));
        testAccount.setAvailableBalance(new BigDecimal("100000.00"));
        testAccount.setFrozenBalance(BigDecimal.ZERO);
        testAccount.setCreditLimit(new BigDecimal("500000.00"));
        testAccount.setStatus("ACTIVE");
    }
    
    @Test
    void findAll_ShouldReturnAllAccounts() {
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountRepository.findAll()).thenReturn(accounts);
        
        List<Account> result = accountService.findAll();
        
        assertEquals(1, result.size());
        assertEquals("ACC001", result.get(0).getAccountCode());
    }
    
    @Test
    void findById_ShouldReturnAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        
        Optional<Account> result = accountService.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("ACC001", result.get().getAccountCode());
    }
    
    @Test
    void findByCustomerId_ShouldReturnAccount() {
        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        
        Optional<Account> result = accountService.findByCustomerId(1L);
        
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getCustomerId());
    }
    
    @Test
    void freezeAmount_ShouldDecreaseAvailableBalance() {
        BigDecimal freezeAmount = new BigDecimal("10000.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        accountService.freezeAmount(1L, freezeAmount);
        
        assertEquals(new BigDecimal("90000.00"), testAccount.getAvailableBalance());
        assertEquals(new BigDecimal("10000.00"), testAccount.getFrozenBalance());
    }
    
    @Test
    void freezeAmount_ShouldThrowException_WhenInsufficientBalance() {
        BigDecimal freezeAmount = new BigDecimal("200000.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        
        assertThrows(RuntimeException.class, () -> {
            accountService.freezeAmount(1L, freezeAmount);
        });
    }
    
    @Test
    void unfreezeAmount_ShouldIncreaseAvailableBalance() {
        testAccount.setFrozenBalance(new BigDecimal("10000.00"));
        testAccount.setAvailableBalance(new BigDecimal("90000.00"));
        
        BigDecimal unfreezeAmount = new BigDecimal("5000.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        accountService.unfreezeAmount(1L, unfreezeAmount);
        
        assertEquals(new BigDecimal("95000.00"), testAccount.getAvailableBalance());
        assertEquals(new BigDecimal("5000.00"), testAccount.getFrozenBalance());
    }
    
    @Test
    void updateBalance_ShouldAddToBalance() {
        BigDecimal amount = new BigDecimal("1000.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        accountService.updateBalance(1L, amount);
        
        assertEquals(new BigDecimal("101000.00"), testAccount.getBalance());
    }
    
    @Test
    void save_ShouldPersistAccount() {
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        Account saved = accountService.save(testAccount);
        
        assertNotNull(saved);
        assertEquals("ACC001", saved.getAccountCode());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
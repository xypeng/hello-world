package com.trading.service;

import com.trading.model.Account;
import com.trading.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public List<Account> findAll() {
        return accountRepository.findAll();
    }
    
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }
    
    public Optional<Account> findByAccountCode(String accountCode) {
        return accountRepository.findByAccountCode(accountCode);
    }
    
    public Optional<Account> findByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }
    
    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }
    
    @Transactional
    public void freezeAmount(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient available balance");
        }
        
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setFrozenBalance(account.getFrozenBalance().add(amount));
        accountRepository.save(account);
    }
    
    @Transactional
    public void unfreezeAmount(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getFrozenBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient frozen balance");
        }
        
        account.setFrozenBalance(account.getFrozenBalance().subtract(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);
    }
    
    @Transactional
    public void updateBalance(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setBalance(account.getBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);
    }
}
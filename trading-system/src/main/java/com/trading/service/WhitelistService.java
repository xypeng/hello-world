package com.trading.service;

import com.trading.model.Whitelist;
import com.trading.repository.WhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WhitelistService {
    
    private final WhitelistRepository whitelistRepository;
    
    public List<Whitelist> findAll() {
        return whitelistRepository.findAll();
    }
    
    public List<Whitelist> findByCustomerId(Long customerId) {
        return whitelistRepository.findByCustomerId(customerId);
    }
    
    public List<Whitelist> findByStatus(String status) {
        return whitelistRepository.findByStatus(status);
    }
    
    public boolean isSecurityWhitelisted(Long customerId, String securityCode) {
        // 全局白名单（customer_id 为空）
        boolean globalWhitelist = whitelistRepository.existsByCustomerIdAndSecurityCodeAndStatus(
                null, securityCode, "ACTIVE");
        
        // 客户专用白名单
        boolean customerWhitelist = whitelistRepository.existsByCustomerIdAndSecurityCodeAndStatus(
                customerId, securityCode, "ACTIVE");
        
        return globalWhitelist || customerWhitelist;
    }
    
    public Whitelist save(Whitelist whitelist) {
        return whitelistRepository.save(whitelist);
    }
    
    public void deleteById(Long id) {
        whitelistRepository.deleteById(id);
    }
}
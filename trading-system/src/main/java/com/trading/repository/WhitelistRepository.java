package com.trading.repository;

import com.trading.model.Whitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Long> {
    List<Whitelist> findByCustomerId(Long customerId);
    List<Whitelist> findByStatus(String status);
    List<Whitelist> findByCustomerIdAndStatus(Long customerId, String status);
    boolean existsByCustomerIdAndSecurityCodeAndStatus(Long customerId, String securityCode, String status);
}
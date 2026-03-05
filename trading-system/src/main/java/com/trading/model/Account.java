package com.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "account")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "account_code", unique = true, nullable = false)
    private String accountCode;
    
    @Column(name = "currency", nullable = false)
    private String currency;
    
    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "available_balance", precision = 18, scale = 2)
    private BigDecimal availableBalance;
    
    @Column(name = "frozen_balance", precision = 18, scale = 2)
    private BigDecimal frozenBalance;
    
    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (availableBalance == null) availableBalance = BigDecimal.ZERO;
        if (frozenBalance == null) frozenBalance = BigDecimal.ZERO;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
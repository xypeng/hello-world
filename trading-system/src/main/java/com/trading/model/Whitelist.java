package com.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "whitelist")
public class Whitelist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "security_code", nullable = false)
    private String securityCode;
    
    @Column(name = "security_name")
    private String securityName;
    
    @Column(name = "security_type")
    private String securityType;
    
    @Column(name = "exchange")
    private String exchange;
    
    @Column(name = "max_quantity")
    private Integer maxQuantity;
    
    @Column(name = "max_notional")
    private Double maxNotional;
    
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
        if (status == null) status = "ACTIVE";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
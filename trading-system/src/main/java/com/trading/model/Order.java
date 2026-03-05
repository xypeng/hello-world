package com.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", unique = true)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "account_id")
    private Long accountId;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "security_code")
    private String securityCode;
    
    @Column(name = "side", nullable = false)
    private String side;
    
    @Column(name = "order_type")
    private String orderType;
    
    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;
    
    @Column(name = "price", precision = 18, scale = 4)
    private BigDecimal price;
    
    @Column(name = "filled_quantity", precision = 18, scale = 4)
    private BigDecimal filledQuantity;
    
    @Column(name = "avg_price", precision = 18, scale = 4)
    private BigDecimal avgPrice;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "order_time")
    private LocalDateTime orderTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "emsx_order_id")
    private String emsxOrderId;
    
    @Column(name = "emsx_strategy")
    private String emsxStrategy;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        if (orderTime == null) orderTime = LocalDateTime.now();
        if (updateTime == null) updateTime = LocalDateTime.now();
        if (filledQuantity == null) filledQuantity = BigDecimal.ZERO;
    }
}
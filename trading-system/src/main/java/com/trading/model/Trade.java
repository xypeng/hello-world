package com.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trade")
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "trade_id", unique = true)
    private String tradeId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "emsx_order_id")
    private String emsxOrderId;
    
    @Column(name = "emsx_fill_id")
    private String emsxFillId;
    
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "side")
    private String side;
    
    @Column(name = "fill_quantity", precision = 18, scale = 4)
    private BigDecimal fillQuantity;
    
    @Column(name = "fill_price", precision = 18, scale = 4)
    private BigDecimal fillPrice;
    
    @Column(name = "notional", precision = 18, scale = 2)
    private BigDecimal notional;
    
    @Column(name = "commission", precision = 18, scale = 2)
    private BigDecimal commission;
    
    @Column(name = "trade_time")
    private LocalDateTime tradeTime;
    
    @Column(name = "counterparty")
    private String counterparty;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (tradeTime == null) tradeTime = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
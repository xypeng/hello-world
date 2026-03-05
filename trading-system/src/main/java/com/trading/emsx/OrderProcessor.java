package com.trading.emsx;

import com.trading.model.*;
import com.trading.service.AccountService;
import com.trading.service.CustomerService;
import com.trading.service.WhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessor {
    
    private final CustomerService customerService;
    private final AccountService accountService;
    private final WhitelistService whitelistService;
    
    /**
     * 处理订单：验资 -> 风控检查 -> 策略处理
     */
    public ProcessingResult processOrder(Order order) {
        log.info("Processing order: {} for customer: {}", order.getOrderId(), order.getCustomerId());
        
        // 1. 客户验证
        Customer customer = customerService.findById(order.getCustomerId())
                .orElseThrow(() -> new OrderRejectException("Customer not found: " + order.getCustomerId()));
        
        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new OrderRejectException("Customer is not active: " + customer.getStatus());
        }
        
        // 2. 账户验证
        Account account = accountService.findByCustomerId(order.getCustomerId())
                .orElseThrow(() -> new OrderRejectException("Account not found for customer"));
        
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new OrderRejectException("Account is not active");
        }
        
        // 3. 资金验资（仅针对买入）
        if ("BUY".equalsIgnoreCase(order.getSide())) {
            BigDecimal orderValue = order.getPrice().multiply(order.getQuantity());
            BigDecimal available = account.getAvailableBalance();
            
            if (available.compareTo(orderValue) < 0) {
                throw new OrderRejectException("Insufficient funds. Required: " + orderValue + ", Available: " + available);
            }
            
            // 冻结资金
            accountService.freezeAmount(account.getId(), orderValue);
        }
        
        // 4. 白名单检查
        boolean isWhitelisted = whitelistService.isSecurityWhitelisted(
                order.getCustomerId(), order.getSecurityCode());
        
        if (!isWhitelisted) {
            // 释放已冻结的资金
            if ("BUY".equalsIgnoreCase(order.getSide())) {
                BigDecimal orderValue = order.getPrice().multiply(order.getQuantity());
                accountService.unfreezeAmount(account.getId(), orderValue);
            }
            throw new OrderRejectException("Security not in whitelist: " + order.getSecurityCode());
        }
        
        // 5. 数量检查
        if (order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderRejectException("Invalid quantity: " + order.getQuantity());
        }
        
        // 6. 策略处理（这里是预留的钩子）
        order = applyStrategy(order);
        
        return new ProcessingResult(order, true, null);
    }
    
    /**
     * 策略处理钩子
     */
    private Order applyStrategy(Order order) {
        // TODO: 实现自定义策略逻辑
        // 例如：价格优化、订单拆分、冰山订单等
        log.debug("Applying strategy for order: {}", order.getOrderId());
        return order;
    }
    
    /**
     * 成交回报处理
     */
    public void processTrade(Trade trade) {
        log.info("Processing trade: {} for order: {}", trade.getTradeId(), trade.getOrderId());
        
        Order order = trade.getOrderId() != null ? 
                new Order() : null; // TODO: 从 repository 获取
        
        if (order != null) {
            // 更新订单状态
            // 更新持仓
            // 更新账户资金
        }
    }
    
    /**
     * 取消订单
     */
    public void processCancel(Long orderId) {
        log.info("Processing cancel for order: {}", orderId);
        // TODO: 实现撤单逻辑
    }
    
    public static class ProcessingResult {
        private final Order order;
        private final boolean success;
        private final String errorMessage;
        
        public ProcessingResult(Order order, boolean success, String errorMessage) {
            this.order = order;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public Order getOrder() { return order; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class OrderRejectException extends RuntimeException {
        public OrderRejectException(String message) {
            super(message);
        }
    }
}
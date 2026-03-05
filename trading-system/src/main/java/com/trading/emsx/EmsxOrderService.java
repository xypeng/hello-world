package com.trading.emsx;

import com.trading.model.Order;
import com.trading.repository.OrderRepository;
import com.trading.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmsxOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderProcessor orderProcessor;
    private final EmsxConnectionManager connectionManager;
    private final AccountService accountService;
    
    /**
     * 创建并处理订单
     */
    @Transactional
    public Order createOrder(Order order) {
        // 生成订单号
        if (order.getOrderId() == null) {
            order.setOrderId(generateOrderId());
        }
        
        order.setStatus("PENDING");
        order.setOrderTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 保存订单
        order = orderRepository.save(order);
        
        try {
            // 处理订单（验资、风控）
            OrderProcessor.ProcessingResult result = orderProcessor.processOrder(order);
            
            if (result.isSuccess()) {
                order.setStatus("APPROVED");
                order = orderRepository.save(order);
                
                // 发送到 Bloomberg EMSX
                sendToEmsx(order);
            } else {
                order.setStatus("REJECTED");
                order.setErrorMessage(result.getErrorMessage());
                order = orderRepository.save(order);
            }
            
        } catch (OrderProcessor.OrderRejectException e) {
            order.setStatus("REJECTED");
            order.setErrorMessage(e.getMessage());
            order = orderRepository.save(order);
            log.warn("Order rejected: {} - {}", order.getOrderId(), e.getMessage());
            
        } catch (Exception e) {
            order.setStatus("ERROR");
            order.setErrorMessage(e.getMessage());
            order = orderRepository.save(order);
            log.error("Error processing order: " + order.getOrderId(), e);
        }
        
        return order;
    }
    
    /**
     * 发送订单到 Bloomberg EMSX
     */
    private void sendToEmsx(Order order) {
        try {
            // TODO: 使用 Bloomberg API 构建并发送订单
            log.info("Sending order to EMSX: {} - {} {} {} @ {}", 
                    order.getOrderId(), order.getSide(), order.getQuantity(), 
                    order.getSecurityCode(), order.getPrice());
            
            // 模拟发送成功
            order.setEmsxOrderId("EMSX_" + order.getOrderId());
            order.setStatus("SENT");
            orderRepository.save(order);
            
        } catch (Exception e) {
            log.error("Failed to send order to EMSX", e);
            throw new RuntimeException("Failed to send order to EMSX", e);
        }
    }
    
    /**
     * 处理 EMSX 成交回报
     */
    @Transactional
    public void handleFill(String emsxOrderId, String fillId, String filledQty, String price) {
        log.info("Processing fill - Order: {}, Fill: {}, Qty: {}, Price: {}", 
                emsxOrderId, fillId, filledQty, price);
        
        // TODO: 实现成交回报处理逻辑
    }
    
    /**
     * 处理 EMSX 订单状态更新
     */
    @Transactional
    public void handleOrderUpdate(String emsxOrderId, String status) {
        log.info("Order update - EMSX Order: {}, Status: {}", emsxOrderId, status);
        
        Order order = orderRepository.findByEmsxOrderId(emsxOrderId)
                .orElse(null);
        
        if (order != null) {
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.save(order);
        }
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!"SENT".equals(order.getStatus()) && !"APPROVED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }
        
        // TODO: 发送取消请求到 EMSX
        
        order.setStatus("CANCELLING");
        orderRepository.save(order);
    }
    
    private String generateOrderId() {
        return "ORD_" + LocalDateTime.now().getYear() + 
                String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}
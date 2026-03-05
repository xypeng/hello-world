package com.trading.repository;

import com.trading.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByTradeId(String tradeId);
    Optional<Trade> findByEmsxFillId(String emsxFillId);
    List<Trade> findByOrderId(Long orderId);
    List<Trade> findByCustomerId(Long customerId);
}
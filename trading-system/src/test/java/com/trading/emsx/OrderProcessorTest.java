package com.trading.emsx;

import com.trading.model.Account;
import com.trading.model.Customer;
import com.trading.service.AccountService;
import com.trading.service.CustomerService;
import com.trading.service.WhitelistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private AccountService accountService;
    
    @Mock
    private WhitelistService whitelistService;
    
    @InjectMocks
    private OrderProcessor orderProcessor;
    
    private Customer testCustomer;
    private Account testAccount;
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setCustomerCode("C001");
        testCustomer.setStatus("ACTIVE");
        
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setCustomerId(1L);
        testAccount.setAccountCode("ACC001");
        testAccount.setBalance(new BigDecimal("100000"));
        testAccount.setAvailableBalance(new BigDecimal("100000"));
        testAccount.setFrozenBalance(BigDecimal.ZERO);
        testAccount.setStatus("ACTIVE");
        
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderId("ORD_001");
        testOrder.setCustomerId(1L);
        testOrder.setSecurityCode("AAPL US");
        testOrder.setSide("BUY");
        testOrder.setQuantity(new BigDecimal("100"));
        testOrder.setPrice(new BigDecimal("150.00"));
    }
    
    @Test
    void processOrder_ShouldSucceed_WhenValidOrder() {
        when(customerService.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(accountService.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(whitelistService.isSecurityWhitelisted(1L, "AAPL US")).thenReturn(true);
        when(accountService.freezeAmount(any(), any())).thenReturn(null);
        
        OrderProcessor.ProcessingResult result = orderProcessor.processOrder(testOrder);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrder());
        verify(accountService, times(1)).freezeAmount(eq(1L), any());
    }
    
    @Test
    void processOrder_ShouldReject_WhenCustomerNotActive() {
        testCustomer.setStatus("SUSPENDED");
        when(customerService.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        assertThrows(OrderProcessor.OrderRejectException.class, () -> {
            orderProcessor.processOrder(testOrder);
        });
    }
    
    @Test
    void processOrder_ShouldReject_WhenInsufficientFunds() {
        testAccount.setAvailableBalance(new BigDecimal("1000"));
        when(customerService.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(accountService.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        
        assertThrows(OrderProcessor.OrderRejectException.class, () -> {
            orderProcessor.processOrder(testOrder);
        });
    }
    
    @Test
    void processOrder_ShouldReject_WhenSecurityNotWhitelisted() {
        when(customerService.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(accountService.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(whitelistService.isSecurityWhitelisted(1L, "AAPL US")).thenReturn(false);
        
        assertThrows(OrderProcessor.OrderRejectException.class, () -> {
            orderProcessor.processOrder(testOrder);
        });
    }
    
    @Test
    void processOrder_ShouldReject_WhenCustomerNotFound() {
        when(customerService.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(OrderProcessor.OrderRejectException.class, () -> {
            orderProcessor.processOrder(testOrder);
        });
    }
}
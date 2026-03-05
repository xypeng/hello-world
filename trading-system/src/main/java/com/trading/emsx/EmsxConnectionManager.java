package com.trading.emsx;

import com.bloomberg.apiutils.*;
import com.bloomberg.blpapi.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class EmsxConnectionManager {
    
    private final EmsxConfig config;
    private Session session;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final CopyOnWriteArrayList<EmsxEventListener> listeners = new CopyOnWriteArrayList<>();
    
    public EmsxConnectionManager(EmsxConfig config) {
        this.config = config;
    }
    
    @PostConstruct
    public void connect() {
        try {
            SessionOptions sessionOptions = new SessionOptions();
            sessionOptions.setServerHost(config.getHost());
            sessionOptions.setServerPort(config.getPort());
            
            log.info("Connecting to Bloomberg EMSX at {}:{}", config.getHost(), config.getPort());
            
            session = new Session(sessionOptions, new SessionListener() {
                @Override
                public void processEvent(Event event, Session session) {
                    handleEvent(event);
                }
            });
            
            if (session.start()) {
                if (session.openService(config.getServiceName())) {
                    connected.set(true);
                    log.info("Connected to Bloomberg EMSX service: {}", config.getServiceName());
                } else {
                    log.error("Failed to open EMSX service: {}", config.getServiceName());
                }
            } else {
                log.error("Failed to start Bloomberg session");
            }
        } catch (Exception e) {
            log.error("Error connecting to Bloomberg EMSX", e);
        }
    }
    
    @PreDestroy
    public void disconnect() {
        if (session != null) {
            session.stop();
            connected.set(false);
            log.info("Disconnected from Bloomberg EMSX");
        }
    }
    
    private void handleEvent(Event event) {
        Event.EventType eventType = event.eventType();
        log.debug("Received Bloomberg event: {}", eventType);
        
        for (Message message : event) {
            for (EmsxEventListener listener : listeners) {
                try {
                    listener.onMessage(message, eventType);
                } catch (Exception e) {
                    log.error("Error in event listener", e);
                }
            }
        }
    }
    
    public void sendOrder(Request request) throws Exception {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to Bloomberg EMSX");
        }
        session.sendRequest(request, null);
    }
    
    public void addEventListener(EmsxEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeEventListener(EmsxEventListener listener) {
        listeners.remove(listener);
    }
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public Session getSession() {
        return session;
    }
    
    public interface EmsxEventListener {
        void onMessage(Message message, Event.EventType eventType);
    }
}
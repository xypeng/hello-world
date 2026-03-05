package com.trading.emsx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bloomberg.emsx")
public class EmsxConfig {
    
    private String host;
    private int port;
    private String serviceName;
    private String applicationName;
    
    public String getConnectionString() {
        return host + ":" + port;
    }
}
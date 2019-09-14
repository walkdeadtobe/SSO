package com.aak.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configure to get port of local server example port,address and  other information
 * @author chengr
 * @Time 2019-9-10
 */
@Component
@ConfigurationProperties(prefix="server") //接收application.yml中的server下面的属性
public class MyApplication {
    public String address;
    public String port;

    public String getUrl() {
        return address;
    }
    public void setUrl(String Url) {
        this.address = Url;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }
}


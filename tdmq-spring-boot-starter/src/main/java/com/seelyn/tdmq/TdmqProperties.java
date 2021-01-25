package com.seelyn.tdmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TDMQ配置信息
 *
 * @author linfeng
 */
@ConfigurationProperties(prefix = "tdmq")
public class TdmqProperties {

    private String serviceUrl = "pulsar://localhost:6650";
    private String listenerName;
    private String authenticationToken;
    private Integer ioThreads = 10;
    private Integer listenerThreads = 10;
    private Boolean enableTcpNoDelay = false;
    private Integer keepAliveIntervalSec = 20;
    private Integer connectionTimeoutSec = 10;
    private Integer operationTimeoutSec = 15;
    private Integer startingBackoffIntervalMs = 100;
    private Integer maxBackoffIntervalSec = 10;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public Integer getListenerThreads() {
        return listenerThreads;
    }

    public void setListenerThreads(Integer listenerThreads) {
        this.listenerThreads = listenerThreads;
    }

    public Boolean isEnableTcpNoDelay() {
        return enableTcpNoDelay;
    }

    public void setEnableTcpNoDelay(Boolean enableTcpNoDelay) {
        this.enableTcpNoDelay = enableTcpNoDelay;
    }

    public Integer getKeepAliveIntervalSec() {
        return keepAliveIntervalSec;
    }

    public void setKeepAliveIntervalSec(Integer keepAliveIntervalSec) {
        this.keepAliveIntervalSec = keepAliveIntervalSec;
    }

    public Integer getConnectionTimeoutSec() {
        return connectionTimeoutSec;
    }

    public void setConnectionTimeoutSec(Integer connectionTimeoutSec) {
        this.connectionTimeoutSec = connectionTimeoutSec;
    }

    public Integer getOperationTimeoutSec() {
        return operationTimeoutSec;
    }

    public void setOperationTimeoutSec(Integer operationTimeoutSec) {
        this.operationTimeoutSec = operationTimeoutSec;
    }

    public Integer getStartingBackoffIntervalMs() {
        return startingBackoffIntervalMs;
    }

    public void setStartingBackoffIntervalMs(Integer startingBackoffIntervalMs) {
        this.startingBackoffIntervalMs = startingBackoffIntervalMs;
    }

    public Integer getMaxBackoffIntervalSec() {
        return maxBackoffIntervalSec;
    }

    public void setMaxBackoffIntervalSec(Integer maxBackoffIntervalSec) {
        this.maxBackoffIntervalSec = maxBackoffIntervalSec;
    }

}

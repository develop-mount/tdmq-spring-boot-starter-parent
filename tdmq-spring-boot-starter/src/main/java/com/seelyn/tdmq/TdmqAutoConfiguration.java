package com.seelyn.tdmq;

import com.seelyn.tdmq.utils.ExecutorUtils;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author linfeng
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties({TdmqProperties.class})
public class TdmqAutoConfiguration {

    private final TdmqProperties tdmqProperties;

    @Autowired
    public TdmqAutoConfiguration(TdmqProperties tdmqProperties) {
        this.tdmqProperties = tdmqProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(tdmqProperties.getServiceUrl())
                .listenerName(tdmqProperties.getListenerName())
                .authentication(AuthenticationFactory.token(tdmqProperties.getAuthenticationToken()))
                .ioThreads(tdmqProperties.getIoThreads())
                .listenerThreads(tdmqProperties.getListenerThreads())
                .enableTcpNoDelay(tdmqProperties.isEnableTcpNoDelay())
                .keepAliveInterval(tdmqProperties.getKeepAliveIntervalSec(), TimeUnit.SECONDS)
                .connectionTimeout(tdmqProperties.getConnectionTimeoutSec(), TimeUnit.SECONDS)
                .operationTimeout(tdmqProperties.getOperationTimeoutSec(), TimeUnit.SECONDS)
                .startingBackoffInterval(tdmqProperties.getStartingBackoffIntervalMs(), TimeUnit.MILLISECONDS)
                .maxBackoffInterval(tdmqProperties.getMaxBackoffIntervalSec(), TimeUnit.SECONDS)
                .build();
    }

}

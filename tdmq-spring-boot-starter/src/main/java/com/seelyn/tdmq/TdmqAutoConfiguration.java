package com.seelyn.tdmq;

import com.seelyn.tdmq.consumer.ConsumerMetadataMap;
import com.seelyn.tdmq.consumer.ConsumerMetadataPostProcessor;
import com.seelyn.tdmq.consumer.ConsumerSubscribeFactory;
import com.seelyn.tdmq.event.BusEvent;
import com.seelyn.tdmq.producer.*;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.concurrent.TimeUnit;

/**
 * @author linfeng
 */
@Configuration
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

    @Bean("consumerMethodPostProcessor")
    public ConsumerMetadataPostProcessor consumerMethodPostProcessor() {
        return new ConsumerMetadataPostProcessor();
    }

    @Bean
    @DependsOn({"pulsarClient", "consumerMethodPostProcessor"})
    public ConsumerSubscribeFactory consumerSubscribeFactory(PulsarClient pulsarClient,
                                                             ConsumerMetadataMap consumerListenerMap,
                                                             TdmqProperties tdmqProperties) {

        return new ConsumerSubscribeFactory(pulsarClient, consumerListenerMap, tdmqProperties);
    }

    @Bean
    public <T> TdmqTemplate<T> tdmqTemplate(PulsarClient pulsarClient) {
        return new TdmqTemplate<>(pulsarClient);
    }

    @Bean
    public <T extends BusEvent> EventBusPublisher<T> eventBusPublisher(PulsarClient pulsarClient) {
        return new EventBusPublisher<>(pulsarClient);
    }

    @Bean
    public ListTdmqTemplate listBaseBytesTemplate(PulsarClient pulsarClient) {
        return new ListTdmqTemplate(pulsarClient);
    }

    @Bean
    public ObjectTdmqTemplate objectBaseBytesTemplate(PulsarClient pulsarClient) {
        return new ObjectTdmqTemplate(pulsarClient);
    }

    @Bean
    public StringTdmqTemplate stringTdmqTemplate(PulsarClient pulsarClient) {
        return new StringTdmqTemplate(pulsarClient);
    }

}

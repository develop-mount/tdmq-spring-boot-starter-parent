package com.seelyn.tdmq;

import com.seelyn.tdmq.consumer.ConsumerBeanCollection;
import com.seelyn.tdmq.consumer.ConsumerBeanPostProcessor;
import com.seelyn.tdmq.consumer.ConsumerSubscribeFactory;
import com.seelyn.tdmq.producer.ListTdmqTemplate;
import com.seelyn.tdmq.producer.ObjectTdmqTemplate;
import com.seelyn.tdmq.producer.StringTdmqTemplate;
import com.seelyn.tdmq.producer.TdmqTemplate;
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
    public ConsumerBeanPostProcessor consumerMethodPostProcessor() {
        return new ConsumerBeanPostProcessor();
    }

    @Bean
    @DependsOn({"pulsarClient", "consumerMethodPostProcessor"})
    public ConsumerSubscribeFactory consumerSubscribeFactory(PulsarClient pulsarClient,
                                                             ConsumerBeanCollection consumerBeanCollection,
                                                             TdmqProperties tdmqProperties) {

        return new ConsumerSubscribeFactory(pulsarClient, consumerBeanCollection, tdmqProperties);
    }

    @Bean
    public <T> TdmqTemplate<T> tTdmqTemplate(PulsarClient pulsarClient) {
        return new TdmqTemplate<>(pulsarClient);
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

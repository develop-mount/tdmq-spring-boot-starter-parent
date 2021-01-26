package com.seelyn.tdmq;

import com.seelyn.tdmq.consumer.ConsumerMethodCollection;
import com.seelyn.tdmq.consumer.ConsumerMethodPostProcessor;
import com.seelyn.tdmq.consumer.ConsumerSubscribeFactory;
import com.seelyn.tdmq.producer.ListBaseBytesTemplate;
import com.seelyn.tdmq.producer.ObjectBaseBytesTemplate;
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
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author linfeng
 */
@Configuration
@EnableConfigurationProperties({TdmqProperties.class, TdmqBatchProperties.class})
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
    public ConsumerMethodPostProcessor consumerMethodPostProcessor() {
        return new ConsumerMethodPostProcessor();
    }

    @Bean("consumerBatchExecutor")
    public AsyncTaskExecutor consumerBatchExecutor(TdmqBatchProperties batchProperties) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(batchProperties.getCorePoolSize());
        taskExecutor.setMaxPoolSize(batchProperties.getMaxPoolSize());
        taskExecutor.setQueueCapacity(batchProperties.getQueueCapacity());
        taskExecutor.setKeepAliveSeconds(batchProperties.getKeepAliveSeconds());
        taskExecutor.setThreadNamePrefix(batchProperties.getThreadNamePrefix());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(batchProperties.isWaitForJobsToCompleteOnShutdown());
        taskExecutor.setAwaitTerminationSeconds(batchProperties.getAwaitTerminationSeconds());
        return taskExecutor;
    }

    @Bean
    @DependsOn({"pulsarClient", "consumerMethodPostProcessor", "consumerBatchExecutor"})
    public ConsumerSubscribeFactory consumerSubscribeFactory(PulsarClient pulsarClient,
                                                             ConsumerMethodCollection consumerMethodCollection,
                                                             AsyncTaskExecutor consumerBatchExecutor) {

        return new ConsumerSubscribeFactory(pulsarClient, consumerMethodCollection, consumerBatchExecutor);
    }

    @Bean
    public <T> TdmqTemplate<T> tTdmqTemplate(PulsarClient pulsarClient) {
        return new TdmqTemplate<>(pulsarClient);
    }


    @Bean
    public ListBaseBytesTemplate listBaseBytesTemplate(PulsarClient pulsarClient) {
        return new ListBaseBytesTemplate(pulsarClient);
    }

    @Bean
    public ObjectBaseBytesTemplate objectBaseBytesTemplate(PulsarClient pulsarClient) {
        return new ObjectBaseBytesTemplate(pulsarClient);
    }

    @Bean
    public StringTdmqTemplate stringTdmqTemplate(PulsarClient pulsarClient) {
        return new StringTdmqTemplate(pulsarClient);
    }

}

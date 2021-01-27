package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.ConsumerInitException;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import com.seelyn.tdmq.utils.ExecutorUtils;
import com.seelyn.tdmq.utils.SchemaUtils;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订阅者，订阅
 *
 * @author linfeng
 */
public class ConsumerSubscribeFactory implements EmbeddedValueResolverAware, SmartInitializingSingleton, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerSubscribeFactory.class);

    private final PulsarClient pulsarClient;
    private final ConsumerMethodCollection consumerMethodCollection;
    private final AsyncTaskExecutor consumerBatchExecutor;

    private StringValueResolver stringValueResolver;
    private List<Consumer<?>> singleConsumers;
    private List<ConsumerFuture> batchConsumers;

    public ConsumerSubscribeFactory(PulsarClient pulsarClient,
                                    ConsumerMethodCollection consumerMethodCollection,
                                    AsyncTaskExecutor consumerBatchExecutor) {
        this.pulsarClient = pulsarClient;
        this.consumerMethodCollection = consumerMethodCollection;
        this.consumerBatchExecutor = consumerBatchExecutor;
    }

    @Override
    public void setEmbeddedValueResolver(@SuppressWarnings("NullableProblems") StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    public void afterSingletonsInstantiated() {

        //  初始化单消息订阅
        if (!consumerMethodCollection.getSingleMessageConsumer().isEmpty()) {

            singleConsumers = consumerMethodCollection.getSingleMessageConsumer().entrySet()
                    .stream()
                    .map(entry -> subscribeSingle(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
        //  初始化多消息订阅
        if (!consumerMethodCollection.getBatchMessageConsumer().isEmpty()) {

            batchConsumers = consumerMethodCollection.getBatchMessageConsumer().entrySet()
                    .stream()
                    .map(entry -> subscribeBatch(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

    }

    @Override
    public void destroy() throws Exception {
        for (Consumer<?> consumer : singleConsumers) {
            if (consumer.isConnected()) {
                consumer.close();
            }
        }
        for (ConsumerFuture consumerFuture : batchConsumers) {
            consumerFuture.future.cancel(true);
            if (consumerFuture.consumer.isConnected()) {
                consumerFuture.consumer.close();
            }
        }
    }

    private ConsumerFuture subscribeBatch(String name, ConsumerBatchMessage consumerMessage) {

        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerMessage.getParamType()))
                .consumerName("consumer-" + name)
                .subscriptionName("subscription-" + name)
                .subscriptionType(consumerMessage.getAnnotation().subscriptionType())
                .subscriptionMode(consumerMessage.getAnnotation().subscriptionMode());

        // 设置topic和tags
        topicAndTags(clientBuilder, consumerMessage.getAnnotation());

        clientBuilder.batchReceivePolicy(BatchReceivePolicy.builder()
                .maxNumMessages(consumerMessage.getAnnotation().maxNumMessages())
                .maxNumBytes(consumerMessage.getAnnotation().maxNumBytes())
                .timeout(consumerMessage.getAnnotation().timeoutMs(), consumerMessage.getAnnotation().timeoutUnit())
                .build());

        setDeadLetterPolicy(clientBuilder, consumerMessage.getAnnotation());

        try {
            Consumer<?> consumer = clientBuilder.subscribe();

            Future<?> future = consumerBatchExecutor.submit(() -> {

                while (!Thread.currentThread().isInterrupted()) {
                    //等待接收消息
                    Messages<?> messages = null;
                    try {
                        messages = consumer.batchReceive();
                    } catch (PulsarClientException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                    if (messages != null && messages.size() <= 0) {
                        ExecutorUtils.sleep(10, TimeUnit.MILLISECONDS);
                        continue;
                    }
                    try {
                        //noinspection unchecked
                        consumerMessage.getListener().received(consumer, messages);
                        //消息ACK
                        consumer.acknowledge(messages);
                    } catch (MessageRedeliverException e) {
                        consumer.negativeAcknowledge(messages);
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                }
            });

            return new ConsumerFuture(consumer, future);
        } catch (PulsarClientException e) {
            throw new ConsumerInitException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 设置死信策略
     *
     * @param clientBuilder 订阅构造器
     * @param annotation    TDMQ处理注解
     */
    private void setDeadLetterPolicy(ConsumerBuilder<?> clientBuilder, TdmqHandler annotation) {
        if (annotation.maxRedeliverCount() >= 0) {
            final DeadLetterPolicy.DeadLetterPolicyBuilder deadLetterBuilder = DeadLetterPolicy.builder();

            deadLetterBuilder.maxRedeliverCount(annotation.maxRedeliverCount());

            if (!annotation.deadLetterTopic().isEmpty()) {
                deadLetterBuilder.deadLetterTopic(annotation.deadLetterTopic());
            }

            clientBuilder.deadLetterPolicy(deadLetterBuilder.build());
        }
    }

    /**
     * 设置topic和tags
     *
     * @param clientBuilder 订阅构造器
     * @param handler       TDMQ处理注解
     */
    private void topicAndTags(ConsumerBuilder<?> clientBuilder, TdmqHandler handler) {

        Assert.notEmpty(handler.topics(), "@TdmqTopic 必须设置");
        for (TdmqTopic tdmqTopic : handler.topics()) {
            if (StringUtils.hasLength(tdmqTopic.topic()) && StringUtils.hasLength(tdmqTopic.tags())) {
                clientBuilder.topicByTag(stringValueResolver.resolveStringValue(tdmqTopic.topic()),
                        stringValueResolver.resolveStringValue(tdmqTopic.tags()));
            } else if (StringUtils.hasLength(tdmqTopic.topic())) {
                clientBuilder.topic(stringValueResolver.resolveStringValue(tdmqTopic.topic()));
            }
        }
    }

    /**
     * 订阅
     *
     * @param name            类名称
     * @param consumerMessage 订阅消息对象
     * @return 订阅 Consumer
     */
    private Consumer<?> subscribeSingle(String name, ConsumerSingleMessage consumerMessage) {


        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerMessage.getParamType()))
                .consumerName("consumer-" + name)
                .subscriptionName("subscription-" + name)
                .subscriptionType(consumerMessage.getAnnotation().subscriptionType())
                .subscriptionMode(consumerMessage.getAnnotation().subscriptionMode())
                .messageListener((consumer, message) -> {
                    try {
                        //noinspection unchecked
                        consumerMessage.getListener().received(consumer, message);
                        //消息ACK
                        consumer.acknowledge(message);
                    } catch (MessageRedeliverException e) {
                        consumer.negativeAcknowledge(message);
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                });

        // 设置topic和tags
        topicAndTags(clientBuilder, consumerMessage.getAnnotation());
        // 设置
        setDeadLetterPolicy(clientBuilder, consumerMessage.getAnnotation());

        try {
            return clientBuilder.subscribe();
        } catch (PulsarClientException e) {
            throw new ConsumerInitException(e.getLocalizedMessage(), e);
        }

    }

    static class ConsumerFuture {
        Consumer<?> consumer;
        Future<?> future;

        public ConsumerFuture(Consumer<?> consumer, Future<?> future) {
            this.consumer = consumer;
            this.future = future;
        }

        public Consumer<?> getConsumer() {
            return consumer;
        }

        public Future<?> getFuture() {
            return future;
        }
    }

}

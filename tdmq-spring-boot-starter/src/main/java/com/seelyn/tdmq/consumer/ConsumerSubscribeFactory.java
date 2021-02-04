package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqProperties;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.ConsumerInitException;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import com.seelyn.tdmq.utils.ExecutorUtils;
import com.seelyn.tdmq.utils.SchemaUtils;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * 订阅者，订阅
 *
 * @author linfeng
 */
public class ConsumerSubscribeFactory implements EmbeddedValueResolverAware, SmartInitializingSingleton {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerSubscribeFactory.class);

    private final PulsarClient pulsarClient;
    private final ConsumerBeanCollection consumerBeanCollection;

    private int batchThreads;
    private StringValueResolver stringValueResolver;

    public ConsumerSubscribeFactory(PulsarClient pulsarClient,
                                    ConsumerBeanCollection consumerBeanCollection,
                                    TdmqProperties tdmqProperties) {
        this.pulsarClient = pulsarClient;
        this.consumerBeanCollection = consumerBeanCollection;
        this.batchThreads = tdmqProperties.getBatchThreads();
    }

    @Override
    public void setEmbeddedValueResolver(@SuppressWarnings("NullableProblems") StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    public void afterSingletonsInstantiated() {

        //  初始化单消息订阅
        if (!consumerBeanCollection.getSingleMessageConsumer().isEmpty()) {

            consumerBeanCollection.getSingleMessageConsumer().forEach(this::subscribeSingle);
        }
        //  初始化多消息订阅
        if (!consumerBeanCollection.getBatchMessageConsumer().isEmpty()) {

            ConcurrentLinkedQueue<ConsumerBean> concurrentLinkedQueue = Queues.newConcurrentLinkedQueue();
            for (Map.Entry<String, ConsumerBeanBatch> entry : consumerBeanCollection.getBatchMessageConsumer().entrySet()) {
                concurrentLinkedQueue.add(subscribeBatch(entry.getKey(), entry.getValue()));
            }
            //批量消息
            batchConsumerListener(concurrentLinkedQueue);
        }

    }

    /**
     * 批量获取消息
     */
    private void batchConsumerListener(ConcurrentLinkedQueue<ConsumerBean> batchConsumers) {

        if (CollectionUtils.isEmpty(batchConsumers)) {
            return;
        }

        if (batchThreads <= 0) {
            batchThreads = batchConsumers.size();
        }

        ExecutorService executorServiceBatch = ExecutorUtils.newFixedThreadPool(batchThreads);

        for (int i = 0; i < batchThreads; i++) {

            executorServiceBatch.submit(() -> {

                while (!Thread.currentThread().isInterrupted()) {

                    ConsumerBean consumerBean = batchConsumers.poll();
                    if (consumerBean == null) {
                        continue;
                    }
                    // 再次添加回队尾，多线程可同时获取相同对象，做多线程处理
                    batchConsumers.add(consumerBean);

                    Consumer<?> consumer = consumerBean.consumer;
                    ConsumerBeanBatch batchBean = consumerBean.batchBean;

                    CompletableFuture<? extends Messages<?>> completableFuture = consumer.batchReceiveAsync();
                    Messages<?> messages = null;
                    try {
                        messages = completableFuture.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.error(e.getLocalizedMessage(), e);
                    } catch (ExecutionException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }

                    if (messages != null && messages.size() > 0) {
                        try {
                            //noinspection unchecked
                            batchBean.getListener().received(consumer, messages);
                            //消息ACK
                            consumer.acknowledge(messages);
                        } catch (MessageRedeliverException e) {
                            consumer.negativeAcknowledge(messages);
                        } catch (Exception e) {
                            LOGGER.error(e.getLocalizedMessage(), e);
                        }
                    }

                }
            });
        }

    }

    /**
     * 批量订阅
     *
     * @param name         类名
     * @param consumerBean 订阅关系对象
     * @return 订阅关系
     */
    private ConsumerBean subscribeBatch(String name, ConsumerBeanBatch consumerBean) {

        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerBean.getGenericType()))
                .consumerName("consumer-" + name)
                .subscriptionName("subscription-" + name)
                .subscriptionType(consumerBean.getHandler().subscriptionType())
                .subscriptionMode(consumerBean.getHandler().subscriptionMode());

        // 设置topic和tags
        topicAndTags(clientBuilder, consumerBean.getHandler());

        clientBuilder.batchReceivePolicy(BatchReceivePolicy.builder()
                .maxNumMessages(consumerBean.getHandler().maxNumMessages())
                .maxNumBytes(consumerBean.getHandler().maxNumBytes())
                .timeout(consumerBean.getHandler().timeoutMs(), consumerBean.getHandler().timeoutUnit())
                .build());

        setDeadLetterPolicy(clientBuilder, consumerBean.getHandler());

        try {
            return new ConsumerBean(clientBuilder.subscribe(), consumerBean);
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
     * @param name         类名称
     * @param consumerBean 订阅消息对象
     */
    private void subscribeSingle(String name, ConsumerBeanSingle consumerBean) {


        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerBean.getGenericType()))
                .consumerName("consumer-" + name)
                .subscriptionName("subscription-" + name)
                .subscriptionType(consumerBean.getHandler().subscriptionType())
                .subscriptionMode(consumerBean.getHandler().subscriptionMode())
                .messageListener((consumer, message) -> {
                    try {
                        //noinspection unchecked
                        consumerBean.getListener().received(consumer, message);
                        //消息ACK
                        consumer.acknowledge(message);
                    } catch (MessageRedeliverException e) {
                        consumer.negativeAcknowledge(message);
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                });

        // 设置topic和tags
        topicAndTags(clientBuilder, consumerBean.getHandler());
        // 设置
        setDeadLetterPolicy(clientBuilder, consumerBean.getHandler());

        try {
            clientBuilder.subscribe();
        } catch (PulsarClientException e) {
            throw new ConsumerInitException(e.getLocalizedMessage(), e);
        }

    }

    static class ConsumerBean {
        Consumer<?> consumer;
        ConsumerBeanBatch batchBean;

        ConsumerBean(Consumer<?> consumer, ConsumerBeanBatch batchBean) {
            this.consumer = consumer;
            this.batchBean = batchBean;
        }
    }

}

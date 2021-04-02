package com.seelyn.tdmq.consumer;

import com.google.common.collect.Lists;
import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.TdmqProperties;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 订阅者，订阅
 *
 * @author linfeng
 */
public class ConsumerSubscribeFactory implements EmbeddedValueResolverAware, SmartInitializingSingleton, DisposableBean {

    private final PulsarClient pulsarClient;
    private final ConsumerMetadataMap consumerListenerMap;
    private final int concurrentThreads;

    private StringValueResolver stringValueResolver;
    private List<Future<?>> consumerFutures;

    public ConsumerSubscribeFactory(PulsarClient pulsarClient,
                                    ConsumerMetadataMap consumerListenerMap,
                                    TdmqProperties tdmqProperties) {
        this.pulsarClient = pulsarClient;
        this.consumerListenerMap = consumerListenerMap;
        this.concurrentThreads = tdmqProperties.getConcurrentThreads() <= 0 ? 1 : tdmqProperties.getConcurrentThreads();
    }

    @Override
    public void setEmbeddedValueResolver(@SuppressWarnings("NullableProblems") StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    public void afterSingletonsInstantiated() {

        //  初始化单消息订阅
        if (!CollectionUtils.isEmpty(consumerListenerMap.getMap())) {

            Map<String, ConsumerMetadata> listenerMap = consumerListenerMap.getMap();

            List<SubscribeConsumerExecutor> consumerExecutors = Lists.newArrayListWithCapacity(listenerMap.size());
            int index = 1;
            for (Map.Entry<String, ConsumerMetadata> entry : listenerMap.entrySet()) {
                consumerExecutors.add(subscribe(entry.getValue(), index));
                index++;
            }
            executeConsumerListener(consumerExecutors);
        }

    }

    /**
     * 订阅处理执行
     *
     * @param consumerExecutors 订阅关系执行集合
     */
    private void executeConsumerListener(List<SubscribeConsumerExecutor> consumerExecutors) {

        if (CollectionUtils.isEmpty(consumerExecutors)) {
            return;
        }

        int initialArraySize = consumerExecutors.size() * concurrentThreads;
        consumerFutures = Lists.newArrayListWithCapacity(initialArraySize);

        for (SubscribeConsumerExecutor subscribeExecutor : consumerExecutors) {

            ExecutorService executorService = subscribeExecutor.executorService;
            Consumer<?> consumer = subscribeExecutor.consumer;
            if (subscribeExecutor.metadata.isSingle()) {
                TdmqListener<?> listener = subscribeExecutor.getListenerHandler();
                for (int n = 0; n < concurrentThreads; n++) {
                    consumerFutures.add(executorService.submit(new TdmqListenerHandlerThread(listener, consumer)));
                }
            } else {
                BatchTdmqListener<?> listener = subscribeExecutor.getListenerHandler();
                for (int n = 0; n < concurrentThreads; n++) {
                    consumerFutures.add(executorService.submit(new BatchTdmqListenerHandlerThread(listener, consumer)));
                }
            }
        }
    }


    /**
     * 单独订阅
     *
     * @param consumerListener 订阅关系对象
     * @param index            线程名称下标
     * @return 订阅关系
     */
    private SubscribeConsumerExecutor subscribe(ConsumerMetadata consumerListener, int index) {

        final String threadName = consumerListener.isSingle() ? "s-" + index : "b-" + index;
        final ConsumerBuilder<?> clientBuilder = initConsumerBuilder(consumerListener);

        try {
            return new SubscribeConsumerExecutor(clientBuilder.subscribe(), consumerListener,
                    ExecutorUtils.newFixedThreadPool(concurrentThreads, threadName));
        } catch (PulsarClientException e) {
            throw new ConsumerInitException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * 初始化订阅者构造器
     *
     * @param consumerListener 订阅者信息
     * @return 订阅者构造器
     */
    private ConsumerBuilder<?> initConsumerBuilder(ConsumerMetadata consumerListener) {

        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerListener.getGenericType()))
                .subscriptionName(consumerListener.getSubscriptionName())
                .subscriptionType(consumerListener.getHandler().subscriptionType())
                .subscriptionMode(consumerListener.getHandler().subscriptionMode());

        if (StringUtils.hasLength(consumerListener.getConsumerName())) {
            clientBuilder.consumerName(consumerListener.getConsumerName());
        }

        // 设置topic和tags
        setTopicAndTags(clientBuilder, consumerListener.getHandler());
        // 设置
        setDeadLetterPolicy(clientBuilder, consumerListener.getHandler());

        clientBuilder.batchReceivePolicy(BatchReceivePolicy.builder()
                .maxNumMessages(consumerListener.getHandler().maxNumMessages())
                .maxNumBytes(consumerListener.getHandler().maxNumBytes())
                .timeout(consumerListener.getHandler().timeoutMs(), consumerListener.getHandler().timeoutUnit())
                .build());
        return clientBuilder;
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
            if (StringUtils.hasLength(annotation.deadLetterTopic())) {
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
    private void setTopicAndTags(ConsumerBuilder<?> clientBuilder, TdmqHandler handler) {

        Assert.notEmpty(handler.topics(), "@TdmqTopic 必须设置");
        for (TdmqTopic tdmqTopic : handler.topics()) {

            String topic = StringUtils.hasLength(tdmqTopic.topic()) ? stringValueResolver.resolveStringValue(tdmqTopic.topic()) : "";
            String tags = StringUtils.hasLength(tdmqTopic.tags()) ? stringValueResolver.resolveStringValue(tdmqTopic.tags()) : "";

            if (StringUtils.hasLength(topic) && StringUtils.hasLength(tags)) {
                clientBuilder.topicByTag(topic, tags);
            } else if (StringUtils.hasLength(tdmqTopic.topic())) {
                clientBuilder.topic(topic);
            }
        }
    }

    @Override
    public void destroy() {
        if (CollectionUtils.isEmpty(consumerFutures)) {
            return;
        }
        for (Future<?> future : consumerFutures) {
            future.cancel(true);
        }
    }

    /**
     * 订阅关系
     */
    static class SubscribeConsumerExecutor {
        Consumer<?> consumer;
        ConsumerMetadata metadata;
        ExecutorService executorService;

        SubscribeConsumerExecutor(Consumer<?> consumer,
                                  ConsumerMetadata metadata,
                                  ExecutorService executorService) {
            this.consumer = consumer;
            this.metadata = metadata;
            this.executorService = executorService;
        }

        <T> T getListenerHandler() {

            //noinspection unchecked
            return (T) metadata.getListener();
        }

    }

    /**
     * 单消息处理
     */
    static class TdmqListenerHandlerThread implements Runnable {

        private static final Logger logger = LoggerFactory.getLogger(TdmqListenerHandlerThread.class);
        @SuppressWarnings("rawtypes")
        private final TdmqListener listener;
        private final Consumer<?> consumer;

        TdmqListenerHandlerThread(TdmqListener<?> listener, Consumer<?> consumer) {
            this.listener = listener;
            this.consumer = consumer;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted() && consumer.isConnected()) {

                CompletableFuture<? extends Message<?>> completableFuture = consumer.receiveAsync();
                Message<?> message = null;
                try {
                    message = completableFuture.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error(e.getLocalizedMessage(), e);
                } catch (ExecutionException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }

                if (message != null) {
                    try {
                        //noinspection unchecked
                        listener.received(consumer, message);
                        //消息ACK
                        consumer.acknowledge(message);
                    } catch (MessageRedeliverException e) {
                        consumer.negativeAcknowledge(message);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                }

            }
            logger.warn("{}线程已结束", Thread.currentThread().getName());
        }
    }

    /**
     * 批量消息处理
     */
    static class BatchTdmqListenerHandlerThread implements Runnable {

        private static final Logger logger = LoggerFactory.getLogger(TdmqListenerHandlerThread.class);
        @SuppressWarnings("rawtypes")
        private final BatchTdmqListener listener;
        private final Consumer<?> consumer;

        BatchTdmqListenerHandlerThread(BatchTdmqListener<?> listener, Consumer<?> consumer) {
            this.listener = listener;
            this.consumer = consumer;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted() && consumer.isConnected()) {

                CompletableFuture<? extends Messages<?>> completableFuture = consumer.batchReceiveAsync();
                Messages<?> messages = null;
                try {
                    messages = completableFuture.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error(e.getLocalizedMessage(), e);
                } catch (ExecutionException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }

                if (messages != null && messages.size() > 0) {
                    try {
                        //noinspection unchecked
                        listener.received(consumer, messages);
                        //消息ACK
                        consumer.acknowledge(messages);
                    } catch (MessageRedeliverException e) {
                        consumer.negativeAcknowledge(messages);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                }

            }
            logger.warn("{}线程已结束", Thread.currentThread().getName());
        }
    }
}

package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.TdmqProperties;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.ConsumerInitException;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import com.seelyn.tdmq.utils.ExecutorUtils;
import com.seelyn.tdmq.utils.SchemaUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * 订阅者，订阅
 *
 * @author linfeng
 */
public class ConsumerSubscribeFactory
    implements EmbeddedValueResolverAware, SmartInitializingSingleton {

    private final PulsarClient pulsarClient;
    private final ConsumerMetadataMap consumerMetadataMap;
    private final int concurrentThreads;

    private StringValueResolver stringValueResolver;

    public ConsumerSubscribeFactory(PulsarClient pulsarClient,
                                    ConsumerMetadataMap consumerMetadataMap,
                                    TdmqProperties tdmqProperties) {
        this.pulsarClient = pulsarClient;
        this.consumerMetadataMap = consumerMetadataMap;
        this.concurrentThreads =
            tdmqProperties.getConcurrentThreads() <= 0 ? 1 : tdmqProperties.getConcurrentThreads();
    }

    @Override
    public void setEmbeddedValueResolver(
        @SuppressWarnings("NullableProblems") StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    public void afterSingletonsInstantiated() {

        //  初始化单消息订阅
        if (!CollectionUtils.isEmpty(consumerMetadataMap.getMap())) {

            Map<String, ConsumerMetadata> listenerMap = consumerMetadataMap.getMap();

            List<SubscribeConsumerExecutor> consumerExecutors =
                Lists.newArrayListWithCapacity(listenerMap.size());
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

        for (SubscribeConsumerExecutor subscribeExecutor : consumerExecutors) {

            ExecutorService executorService = subscribeExecutor.executorService;
            Consumer<?> consumer = subscribeExecutor.consumer;
            if (subscribeExecutor.consumerMetadata.isSingle()) {

                TdmqListener<?> listener = subscribeExecutor.getListenerHandler();
                for (int n = 0; n < concurrentThreads; n++) {

                    executorService.submit(new TdmqListenerHandlerThread(listener, consumer));
                }
            } else {

                BatchTdmqListener<?> listener = subscribeExecutor.getListenerHandler();
                for (int n = 0; n < concurrentThreads; n++) {

                    executorService.submit(new BatchTdmqListenerHandlerThread(listener, consumer));
                }
            }
        }
    }


    /**
     * 单独订阅
     *
     * @param consumerMetadata 订阅关系对象
     * @param index            线程名称下标
     * @return 订阅关系
     */
    private SubscribeConsumerExecutor subscribe(ConsumerMetadata consumerMetadata, int index) {

        final String threadName = consumerMetadata.isSingle() ? "s-" + index : "b-" + index;
        final ConsumerBuilder<?> clientBuilder = initConsumerBuilder(consumerMetadata);

        try {
            return new SubscribeConsumerExecutor(clientBuilder.subscribe(), consumerMetadata,
                ExecutorUtils.newFixedThreadPool(concurrentThreads, threadName));
        } catch (PulsarClientException e) {
            throw new ConsumerInitException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * 初始化订阅者构造器
     *
     * @param consumerMetadata 订阅者信息
     * @return 订阅者构造器
     */
    private ConsumerBuilder<?> initConsumerBuilder(ConsumerMetadata consumerMetadata) {

        final ConsumerBuilder<?> clientBuilder = pulsarClient
            .newConsumer(SchemaUtils.getSchema(consumerMetadata.getGenericType()))
            .subscriptionName(consumerMetadata.getSubscriptionName())
            .subscriptionType(consumerMetadata.getHandler().subscriptionType())
            .subscriptionMode(consumerMetadata.getHandler().subscriptionMode());

        if (StringUtils.hasLength(consumerMetadata.getConsumerName())) {
            clientBuilder.consumerName(consumerMetadata.getConsumerName());
        }

        // 设置topic和tags
        setTopic(clientBuilder, consumerMetadata.getHandler());
        // 设置
        setDeadLetterPolicy(clientBuilder, consumerMetadata.getHandler());

        clientBuilder.batchReceivePolicy(BatchReceivePolicy.builder()
            .maxNumMessages(consumerMetadata.getHandler().maxNumMessages())
            .maxNumBytes(consumerMetadata.getHandler().maxNumBytes())
            .timeout(consumerMetadata.getHandler().timeoutMs(),
                consumerMetadata.getHandler().timeoutUnit())
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
            final DeadLetterPolicy.DeadLetterPolicyBuilder deadLetterBuilder =
                DeadLetterPolicy.builder();

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
    private void setTopic(ConsumerBuilder<?> clientBuilder, TdmqHandler handler) {

        Assert.notEmpty(handler.topics(), "@TdmqTopic 必须设置");
        for (TdmqTopic tdmqTopic : handler.topics()) {
            String topic = StringUtils.hasLength(tdmqTopic.topic()) ?
                stringValueResolver.resolveStringValue(tdmqTopic.topic()) : "";

            if (StringUtils.hasLength(topic)) {
                clientBuilder.topic(topic);
            } else {
                throw new IllegalArgumentException("@TdmqTopic 中 topic不能为空");
            }
        }
    }

    /**
     * 订阅关系
     */
    static class SubscribeConsumerExecutor {
        Consumer<?> consumer;
        ConsumerMetadata consumerMetadata;
        ExecutorService executorService;

        SubscribeConsumerExecutor(Consumer<?> consumer,
                                  ConsumerMetadata consumerMetadata,
                                  ExecutorService executorService) {
            this.consumer = consumer;
            this.consumerMetadata = consumerMetadata;
            this.executorService = executorService;
        }

        <T> T getListenerHandler() {

            //noinspection unchecked
            return (T) consumerMetadata.getListener();
        }

    }

    /**
     * 单消息处理
     */
    static class TdmqListenerHandlerThread implements Runnable {

        private static final Logger logger =
            LoggerFactory.getLogger(TdmqListenerHandlerThread.class);
        @SuppressWarnings("rawtypes")
        private final TdmqListener listener;
        private final Consumer<?> consumer;

        TdmqListenerHandlerThread(TdmqListener<?> listener, Consumer<?> consumer) {
            this.listener = listener;
            this.consumer = consumer;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                if (!consumer.isConnected()) {
                    continue;
                }

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

        private static final Logger logger =
            LoggerFactory.getLogger(TdmqListenerHandlerThread.class);
        @SuppressWarnings("rawtypes")
        private final BatchTdmqListener listener;
        private final Consumer<?> consumer;

        BatchTdmqListenerHandlerThread(BatchTdmqListener<?> listener, Consumer<?> consumer) {
            this.listener = listener;
            this.consumer = consumer;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                if (!consumer.isConnected()) {
                    continue;
                }
                CompletableFuture<? extends Messages<?>> completableFuture =
                    consumer.batchReceiveAsync();
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

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
import org.springframework.util.StringValueResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
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
    private final Executor consumerBatchExecutor;

    private StringValueResolver stringValueResolver;
    private List<Consumer<?>> singleConsumers;
    private List<Consumer<?>> batchConsumers;

    public ConsumerSubscribeFactory(PulsarClient pulsarClient,
                                    ConsumerMethodCollection consumerMethodCollection,
                                    Executor consumerBatchExecutor) {
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
        for (Consumer<?> consumer : batchConsumers) {
            if (consumer.isConnected()) {
                consumer.close();
            }
        }
    }

    private Consumer<?> subscribeBatch(String name, ConsumerBatchMessage consumerMessage) {

        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerMessage.getParamType()))
                .consumerName("consumer-" + name)
                .subscriptionName("subscription-" + name)
                .topicsAndTags(getTopicMap(consumerMessage.getAnnotation()))
                .subscriptionType(consumerMessage.getAnnotation().subscriptionType())
                .subscriptionMode(consumerMessage.getAnnotation().subscriptionMode());

        if (consumerMessage.getAnnotation().maxNumMessages() > 0) {

            BatchReceivePolicy.Builder builder = BatchReceivePolicy.builder();

            builder.maxNumMessages(consumerMessage.getAnnotation().maxNumMessages());
            if (consumerMessage.getAnnotation().maxNumBytes() > 0) {
                builder.maxNumBytes(consumerMessage.getAnnotation().maxNumBytes());
            }
            builder.timeout(consumerMessage.getAnnotation().timeoutMs(), consumerMessage.getAnnotation().timeoutUnit());
            clientBuilder.batchReceivePolicy(builder.build());
        }

        setDeadLetterPolicy(clientBuilder, consumerMessage.getAnnotation());

        try {
            Consumer<?> consumer = clientBuilder.subscribe();

            consumerBatchExecutor.execute(() -> {

                //noinspection InfiniteLoopStatement
                while (true) {
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

            return consumer;
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
     * 主题Map
     *
     * @param handler tdmq 处理注解
     * @return 主题Map
     */
    private Map<String, String> getTopicMap(TdmqHandler handler) {

        Map<String, String> topicMap = new HashMap<>(handler.topics().length);
        for (TdmqTopic tdmqTopic : handler.topics()) {
            topicMap.put(stringValueResolver.resolveStringValue(tdmqTopic.topic()),
                    stringValueResolver.resolveStringValue(tdmqTopic.tags()));
        }
        return topicMap;
    }


    /**
     * 订阅
     *
     * @param name            类名称
     * @param consumerMessage 订阅消息对象
     */
    private Consumer<?> subscribeSingle(String name, ConsumerSingleMessage consumerMessage) {


        final ConsumerBuilder<?> clientBuilder = pulsarClient
                .newConsumer(SchemaUtils.getSchema(consumerMessage.getParamType()))
                .consumerName("consumer-" + name)
                .subscriptionName("subscription-" + name)
                .topicsAndTags(getTopicMap(consumerMessage.getAnnotation()))
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

        // 设置
        setDeadLetterPolicy(clientBuilder, consumerMessage.getAnnotation());

        try {
            return clientBuilder.subscribe();
        } catch (PulsarClientException e) {
            throw new ConsumerInitException(e.getLocalizedMessage(), e);
        }

    }

}

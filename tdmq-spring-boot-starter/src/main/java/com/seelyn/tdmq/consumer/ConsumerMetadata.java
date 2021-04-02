package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import org.springframework.util.StringUtils;

/**
 * 订阅
 *
 * @author linfeng
 */
class ConsumerMetadata {

    private static final String CONSUMER_NAME_PREFIX = "consumer-";
    private static final String SUBSCRIPTION_NAME_PREFIX = "subscription-";

    private final String className;
    private final Object listener;
    private final TdmqHandler handler;
    private final Class<?> genericType;

    ConsumerMetadata(String className, Object listener, TdmqHandler handler, Class<?> genericType) {
        this.className = className;
        this.listener = listener;
        this.handler = handler;
        this.genericType = genericType;
    }

    boolean isSingle() {
        return listener instanceof TdmqListener;
    }

    Object getListener() {
        return listener;
    }

    TdmqHandler getHandler() {
        return handler;
    }

    Class<?> getGenericType() {
        return genericType;
    }

    /**
     * 消费者名称
     *
     * @return 消费者名称
     */
    String getConsumerName() {
        if (StringUtils.hasLength(handler.consumerName())) {
            return CONSUMER_NAME_PREFIX + handler.consumerName();
        } else {
            return null;
        }
    }

    /**
     * 订阅名称
     *
     * @return 订阅名称
     */
    String getSubscriptionName() {
        String subscriptionName;
        if (StringUtils.hasLength(handler.subscriptionName())) {
            subscriptionName = SUBSCRIPTION_NAME_PREFIX + handler.subscriptionName();
        } else {
            subscriptionName = SUBSCRIPTION_NAME_PREFIX + className;
        }
        return subscriptionName;
    }
}

package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.annotation.TdmqHandler;
import org.springframework.util.StringUtils;

public abstract class ConsumerBean {
    protected static final String CONSUMER_NAME_PREFIX = "consumer-";
    protected static final String SUBSCRIPTION_NAME_PREFIX = "subscription-";

    private final String name;
    private final TdmqHandler handler;

    public ConsumerBean(String name, TdmqHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    /**
     * 消费者名称
     *
     * @return 消费者名称
     */
    public String getConsumerName() {
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
    public String getSubscriptionName() {
        String subscriptionName;
        if (StringUtils.hasLength(handler.subscriptionName())) {
            subscriptionName = SUBSCRIPTION_NAME_PREFIX + handler.subscriptionName();
        } else {
            subscriptionName = SUBSCRIPTION_NAME_PREFIX + name;
        }
        return subscriptionName;
    }
}

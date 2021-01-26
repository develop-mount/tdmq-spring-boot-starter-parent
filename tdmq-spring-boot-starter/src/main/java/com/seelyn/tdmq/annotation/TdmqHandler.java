package com.seelyn.tdmq.annotation;

import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * TDMQ 处理注解
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TdmqHandler {

    /**
     * 主题注解
     *
     * @return 主题注解
     */
    TdmqTopic[] topics();

    /**
     * 订阅类型
     *
     * @return 订阅类型
     */
    SubscriptionType subscriptionType() default SubscriptionType.Key_Shared;

    /**
     * 订阅模式
     *
     * @return 订阅模式
     */
    SubscriptionMode subscriptionMode() default SubscriptionMode.Durable;

    /**
     * Maximum number of times that a message will be redelivered before being sent to the dead letter queue.
     *
     * @return max redeliver count
     */
    int maxRedeliverCount() default -1;

    /**
     * Name of the dead topic where the failing messages will be sent.
     *
     * @return topic
     */
    String retryLetterTopic() default "";

    /**
     * Name of the retry topic where the failing messages will be sent.
     *
     * @return topic
     */
    String deadLetterTopic() default "";

    /**
     * @return 最大消息数量
     */
    int maxNumMessages() default 200;

    /**
     * @return 最大消息字节
     */
    int maxNumBytes() default -1;

    /**
     * 超时毫秒数
     *
     * @return 超时毫秒数
     */
    int timeoutMs() default 1000;

    /**
     * 超时单位
     *
     * @return 超时单位
     */
    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;
}

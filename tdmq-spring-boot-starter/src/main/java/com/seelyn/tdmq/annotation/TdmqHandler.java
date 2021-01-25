package com.seelyn.tdmq.annotation;

import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * TDMQ 处理注解
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TdmqHandler {

    TdmqTopic[] topics();

    SubscriptionType subscriptionType() default SubscriptionType.Key_Shared;

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
     * @return
     */
    int maxNumMessages() default 200;

    /**
     * @return
     */
    int maxNumBytes() default 1024 * 1024;

    int timeoutMs() default 1000;

    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;
}

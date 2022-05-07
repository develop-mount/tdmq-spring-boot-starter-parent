package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

/**
 * 单条消息接收
 *
 * @param <T>
 */
public interface PulsarMessageListener<T> extends TdmqListener<T> {

    @Override
    default void received(Consumer<T> consumer, Message<T> message) throws MessageRedeliverException {
        received(message.getValue());
    }

    /**
     * 接收消息
     *
     * @param message 消息
     * @throws MessageRedeliverException 重试异常
     */
    void received(T message) throws MessageRedeliverException;

}

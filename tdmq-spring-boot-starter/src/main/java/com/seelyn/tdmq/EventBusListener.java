package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

/**
 * 事件监听
 *
 * @param <T>
 */
public interface EventBusListener<T> extends TdmqListener<T> {

    @Override
    default void received(Consumer<T> consumer, Message<T> message) throws MessageRedeliverException {
        
        onEventListener(message.getValue());
    }

    /**
     * 接收消息
     *
     * @param message 消息
     * @throws MessageRedeliverException 重试异常
     */
    void onEventListener(T message) throws MessageRedeliverException;

}

package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量消息接收
 *
 * @param <T>
 */
public interface PulsarMessagesListener<T> extends BatchTdmqListener<T> {

    @Override
    default void received(Consumer<T> consumer, Messages<T> messages) throws MessageRedeliverException {

        List<T> messageList = new ArrayList<>(messages.size());
        for (Message<T> message : messages) {
            messageList.add(message.getValue());
        }
        received(messageList);
    }

    /**
     * 接收消息
     *
     * @param messages 消息
     * @throws MessageRedeliverException 重试异常
     */
    void received(List<T> messages) throws MessageRedeliverException;
}

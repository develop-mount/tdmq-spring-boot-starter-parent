package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;

import java.io.Serializable;
import java.util.List;

/**
 * 批量接收消息接口
 *
 * @param <T>
 * @author linfeng
 */
public interface TdmqBatchListener<T> extends Serializable {

    /**
     * 接收消息
     *
     * @param consumer 订阅对象
     * @param messages 消息
     */
    void received(Consumer<T> consumer, Messages<T> messages) throws MessageRedeliverException;

}

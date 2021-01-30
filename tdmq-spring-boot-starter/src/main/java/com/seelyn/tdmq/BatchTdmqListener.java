package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;

import java.io.Serializable;

/**
 * 批量接收消息接口
 *
 * @param <T> 泛型
 * @author linfeng
 */
public interface BatchTdmqListener<T> extends Serializable {

    /**
     * 接收消息
     *
     * @param consumer 订阅对象
     * @param messages 消息
     * @throws MessageRedeliverException 消息抛回MQ异常
     */
    void received(Consumer<T> consumer, Messages<T> messages) throws MessageRedeliverException;

}

package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

import java.io.Serializable;

/**
 * TDMQ 接收接口
 *
 * @param <T>
 * @author linfeng
 */
public interface TdmqListener<T> extends Serializable {

    /**
     * 接收消息
     *
     * @param consumer 订阅实例
     * @param message  消息
     * @throws MessageRedeliverException 重试异常
     */
    void received(Consumer<T> consumer, Message<T> message) throws MessageRedeliverException;

}

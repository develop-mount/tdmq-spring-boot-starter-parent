package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.springframework.stereotype.Component;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${queue}")})
public class TestHandler implements TdmqListener<String> {
    @Override
    public void received(Consumer<String> consumer, Message<String> message) throws MessageRedeliverException {
        System.out.println(message.getValue());
    }

}

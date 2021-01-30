package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;
import org.springframework.stereotype.Component;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-1.topic}",
        tags = "${eqxiu.scs.mns.topics.content-todo-1.tags}")})
public class DemoHandler implements BatchTdmqListener<String> {

    @Override
    public void received(Consumer<String> consumer, Messages<String> messages) throws MessageRedeliverException {
        System.out.println("DemoHandler1:" + messages.size());
    }
}

package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-1.topic}",
        tags = "${eqxiu.scs.mns.topics.content-todo-1.tags}")})
public class DemoHandler1 implements BatchTdmqListener<Demo> {

    @Override
    public void received(Consumer<Demo> consumer, Messages<Demo> messages) throws MessageRedeliverException {
        System.out.println("DemoHandler1:" + messages.size());
        for (Message<Demo> demoMessage: messages) {
            System.out.println(String.format("DemoHandler1.received:%s", demoMessage.getValue()));
        }

    }
}

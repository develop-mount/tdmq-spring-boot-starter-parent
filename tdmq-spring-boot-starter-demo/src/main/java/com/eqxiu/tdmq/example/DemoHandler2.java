package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.springframework.stereotype.Component;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(maxNumMessages = 1, topics = {
        @TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-2.topic}", tags = "tag1")
})
public class DemoHandler2 implements BatchTdmqListener<String> {

    @Override
    public void received(Consumer<String> consumer, Messages<String> messages) throws MessageRedeliverException {

        System.out.println(String.format("消息数量%s",messages.size()));
        for (Message<String> msg: messages) {
            System.out.println(msg.getValue());
        }

    }
}

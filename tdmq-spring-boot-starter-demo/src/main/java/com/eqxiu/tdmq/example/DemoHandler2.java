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
@TdmqHandler(maxNumMessages = 1, topics = {
        @TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-2.topic}", tags = "tag1")
})
public class DemoHandler2 implements TdmqListener<String> {


    @Override
    public void received(Consumer<String> consumer, Message<String> message) throws MessageRedeliverException {
        System.out.println("DemoHandler2:" + message.getValue());
        System.out.println("thread:" + Thread.currentThread().getName());
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

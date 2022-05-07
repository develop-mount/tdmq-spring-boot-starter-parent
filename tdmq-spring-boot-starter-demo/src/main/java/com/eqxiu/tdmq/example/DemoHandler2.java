package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.PulsarMessagesListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(maxNumMessages = 1, topics = {
        @TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-2.topic}", tags = "tag1")
})
public class DemoHandler2 implements PulsarMessagesListener<String> {

    @Override
    public void received(List<String> messages) throws MessageRedeliverException {
        System.out.println(String.format("消息数量 %d", messages.size()));
        for (String msg : messages) {
            System.out.println(msg);
        }
    }
}

package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.EventBusListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.springframework.stereotype.Component;

@Component
@TdmqHandler(topics = {
        @TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-1.topic}")
})
public class DemoBusEventHandler implements EventBusListener<TestBusEvent> {

    @Override
    public void onEventListener(TestBusEvent message) throws MessageRedeliverException {
        System.out.println(message.getSource());
    }
}

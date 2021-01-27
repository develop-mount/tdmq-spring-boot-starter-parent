package com.eqxiu.tdmq.example;

import com.eqxiu.soc.iap.mns.dto.ElementCheckTodoDto;
import com.seelyn.tdmq.TdmqBatchListener;
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
@TdmqHandler(topics = {@TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-general.topic}",
        tags = "${eqxiu.scs.mns.topics.content-todo-general.tags}")})
public class DemoHandler implements TdmqBatchListener<ElementCheckTodoDto> {

    @Override
    public void received(Consumer<ElementCheckTodoDto> consumer, Messages<ElementCheckTodoDto> messages) throws MessageRedeliverException {
        System.out.println(messages.size());
    }
}

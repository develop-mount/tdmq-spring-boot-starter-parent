package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.ListBaseBytesListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${queue}", tags = "test")})
public class TestHandler extends ListBaseBytesListener<String> {
    @Override
    protected void receive(Consumer<byte[]> consumer, Message<byte[]> message, List<String> data) throws MessageRedeliverException {
        System.out.println(data);
    }

}

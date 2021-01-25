package com.eqxiu.tdmq.example;

import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

/**
 * @author linfeng
 */
//@Component
//@TdmqHandler(topics = "persistent://pulsar-m93253wq27/eqx-scs/scs")
public class TestHandler implements TdmqListener<String> {
    @Override
    public void received(Consumer<String> consumer, Message<String> message) throws MessageRedeliverException {
        System.out.println(message.getValue());
    }

}

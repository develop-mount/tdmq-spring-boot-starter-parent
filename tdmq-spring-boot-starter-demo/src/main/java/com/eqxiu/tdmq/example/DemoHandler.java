package com.eqxiu.tdmq.example;

import com.alibaba.fastjson.JSON;
import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.annotation.TdmqTopic;
import com.seelyn.tdmq.exception.MessageRedeliverException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author linfeng
 */
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${queue}", tags = "demo")})
public class DemoHandler implements TdmqListener<byte[]> {
    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> message) throws MessageRedeliverException {

        String json = new String(message.getValue(), StandardCharsets.UTF_8);
        Demo demo = JSON.parseObject(json, Demo.class);
        System.out.println(demo);
    }

}

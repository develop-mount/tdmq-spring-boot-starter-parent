package com.eqxiu.tdmq;

import com.alibaba.fastjson.JSON;
import com.eqxiu.tdmq.example.Demo;
import com.seelyn.tdmq.producer.ListBaseBytesTemplate;
import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TdmqSpringBootStarterApplicationTests {

    @Autowired
    private ListBaseBytesTemplate listBaseBytesTemplate;

    @Test
    void contextLoads() throws PulsarClientException {


        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://172.16.21.53:6000/")
                .listenerName("custom:pulsar-m93253wq27/vpc-ixlwfcao/subnet-3kcxngah")
                .authentication(AuthenticationFactory.token("eyJrZXlJZCI6InB1bHNhci1tOTMyNTN3cTI3IiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJzY3MifQ.a7z7SJW8AYH1z6908Jv1OTNlGAG0rvYjbOj_W03bcPY"))
                .build();

        Producer<byte[]> producer = pulsarClient.newProducer(Schema.BYTES)
                .producerName("producer")
                .topic("persistent://pulsar-m93253wq27/eqx-scs/test")
                .create();

        List<String> testList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String ss = "hello" + i;
            testList.add(ss);
        }

        listBaseBytesTemplate
                .createMessageOfList("persistent://pulsar-m93253wq27/eqx-scs/test", testList)
                .tags("test").send();

        Producer<byte[]> producerDemo = pulsarClient.newProducer(Schema.BYTES)
                .producerName("producer")
                .topic("persistent://pulsar-m93253wq27/eqx-scs/test")
                .create();
        for (int i = 0; i < 10; i++) {
            Demo demo = new Demo("name" + i, "demo" + i);
            producerDemo.newMessage().value(JSON.toJSONString(demo).getBytes(StandardCharsets.UTF_8)).tags("demo").send();
        }
    }

}

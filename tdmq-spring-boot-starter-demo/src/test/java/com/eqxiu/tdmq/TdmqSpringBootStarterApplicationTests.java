package com.eqxiu.tdmq;

import com.eqxiu.tdmq.example.Demo;
import com.eqxiu.tdmq.example.TestBusEvent;
import com.seelyn.tdmq.producer.EventBusPublisher;
import com.seelyn.tdmq.utils.SchemaUtils;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

//@SpringBootTest
public class TdmqSpringBootStarterApplicationTests {


    @Test
    void contextLoads() throws PulsarClientException {


        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://172.16.21.53:6000/")
                .listenerName("custom:pulsar-m93253wq27/vpc-ixlwfcao/subnet-3kcxngah")
                .authentication(AuthenticationFactory.token("eyJrZXlJZCI6InB1bHNhci1tOTMyNTN3cTI3IiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJzY3MifQ.a7z7SJW8AYH1z6908Jv1OTNlGAG0rvYjbOj_W03bcPY"))
                .build();

//        Producer<String> producer1 = pulsarClient.newProducer(Schema.STRING)
//                .producerName("producer")
//                .topic("persistent://pulsar-m93253wq27/eqx-scs-test/test1")
//                .create();
//
//        for (int i=0; i< 50; i++) {
//            String ss = "test2_" + i;
//            producer1.newMessage().property("tag2", "test2").value(ss).send();
//        }
//        producer1.newMessage().property("tag1", "test1").value("test11").send();


        EventBusPublisher<TestBusEvent> eventBusPublisher = new EventBusPublisher<>(pulsarClient);

        TestBusEvent event = new TestBusEvent();
        event.setSource("test21");

        eventBusPublisher.publishEvent("persistent://pulsar-m93253wq27/eqx-scs-test/demo2", event);


//
//        listBaseBytesTemplate
//                .createMessageOfList("persistent://pulsar-m93253wq27/eqx-scs-test/test1", testList)
//                .tags("test").send();
//
//        Producer<byte[]> producerDemo = pulsarClient.newProducer(Schema.BYTES)
//                .producerName("producer")
//                .topic("persistent://pulsar-m93253wq27/eqx-scs/test")
//                .create();
//        for (int i = 0; i < 10; i++) {
//            Demo demo = new Demo("name" + i, "demo" + i);
//            producerDemo.newMessage().value(JSON.toJSONString(demo).getBytes(StandardCharsets.UTF_8)).tags("demo").send();
//        }
    }

}

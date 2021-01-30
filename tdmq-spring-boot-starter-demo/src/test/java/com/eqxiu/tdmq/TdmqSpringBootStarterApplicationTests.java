package com.eqxiu.tdmq;

import com.eqxiu.tdmq.example.Demo;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
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

        Producer<Demo> producer = pulsarClient.newProducer(Schema.JSON(Demo.class))
                .producerName("producer")
                .topic("persistent://pulsar-m93253wq27/eqx-scs-test/demo")
                .create();

        Demo demo = new Demo("test", "linfs");
        demo.setContent(Lists.newArrayList("test1","test2"));

        producer.newMessage().value(demo).tags("demo1").send();

//        for (int k=1; k <= 12; k++) {
//            for (int i = 0; i < 50; i++) {
//                String ss = "hello" + i;
//                producer.newMessage().value(ss).tags("test"+k).send();
//            }
//        }

//
//        listBaseBytesTemplate
//                .createMessageOfList("persistent://pulsar-m93253wq27/eqx-scs/test", testList)
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

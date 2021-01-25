package com.seelyn.tdmq;

import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
public class TdmqSpringBootStarterApplicationTests {


    @Test
    void contextLoads() throws PulsarClientException {


//        PulsarClient pulsarClient = PulsarClient.builder()
//                .serviceUrl("pulsar://172.16.21.53:6000/")
//                .listenerName("custom:pulsar-m93253wq27/vpc-ixlwfcao/subnet-3kcxngah")
//                .authentication(AuthenticationFactory.token("eyJrZXlJZCI6InB1bHNhci1tOTMyNTN3cTI3IiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJzY3MifQ.a7z7SJW8AYH1z6908Jv1OTNlGAG0rvYjbOj_W03bcPY"))
//                .build();
//
//        Producer<String> producer = pulsarClient.newProducer(Schema.STRING)
//                .producerName("producer")
//                .topic("persistent://pulsar-m93253wq27/eqx-scs/scs")
//                .create();
//
//        for (int i=0; i< 500; i++) {
//            producer.newMessage().value("hello" + i).tags("cdc").send();
//        }

    }

}

package com.eqxiu.tdmq;

import org.apache.pulsar.client.api.*;
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

        Producer<String> producer = pulsarClient.newProducer(Schema.STRING)
                .producerName("producer")
                .topic("persistent://pulsar-m93253wq27/eqx-scs/test")
                .create();

        for (int i = 0; i < 10; i++) {
            producer.newMessage().value("hello" + i).tags("cdc").send();
        }

    }

}

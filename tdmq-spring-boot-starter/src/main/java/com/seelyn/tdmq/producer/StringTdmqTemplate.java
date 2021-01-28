package com.seelyn.tdmq.producer;

import org.apache.pulsar.client.api.PulsarClient;

public class StringTdmqTemplate extends TdmqTemplate<String> {
    public StringTdmqTemplate(PulsarClient pulsarClient) {
        super(pulsarClient);
    }
}

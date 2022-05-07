package com.seelyn.tdmq.producer;

import com.seelyn.tdmq.event.BusEvent;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBusPublisher<T extends BusEvent> extends TdmqTemplate<T> {

    private static final Logger logger = LoggerFactory.getLogger(EventBusPublisher.class);

    public EventBusPublisher(PulsarClient pulsarClient) {
        super(pulsarClient);
    }

    public void publishEvent(String eventName, T event) {

        try {
            this.send(eventName, event);
        } catch (PulsarClientException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}

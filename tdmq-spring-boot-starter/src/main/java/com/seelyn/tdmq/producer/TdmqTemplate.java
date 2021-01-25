package com.seelyn.tdmq.producer;

import com.seelyn.tdmq.exception.ProducerInitException;
import com.seelyn.tdmq.utils.SchemaUtils;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.TypedMessageBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TDMQ 模板
 *
 * @param <T>
 * @author linfeng
 */
public class TdmqTemplate<T> {

    private final PulsarClient pulsarClient;
    private final Map<String, Producer<T>> producers = new ConcurrentHashMap<>();

    public TdmqTemplate(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    public MessageId send(String topic, T message) throws PulsarClientException {

        Producer<T> producer = producers.computeIfAbsent(topic, key -> buildProducer(key, message.getClass()));
        return producer.send(message);
    }

    public CompletableFuture<MessageId> sendAsync(String topic, T message) {

        Producer<T> producer = producers.computeIfAbsent(topic, key -> buildProducer(key, message.getClass()));
        return producer.sendAsync(message);
    }

    public TypedMessageBuilder<T> createMessage(String topic, T message) {

        Producer<T> producer = producers.computeIfAbsent(topic, key -> buildProducer(key, message.getClass()));
        return producer.newMessage().value(message);
    }

    private Producer<T> buildProducer(String topic, Class<?> holder) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> tClass = (Class<T>) holder;
            return pulsarClient.newProducer(SchemaUtils.getSchema(tClass))
                    .topic(topic)
                    .create();
        } catch (PulsarClientException e) {
            throw new ProducerInitException("Failed to init producer.", e);
        }
    }

}

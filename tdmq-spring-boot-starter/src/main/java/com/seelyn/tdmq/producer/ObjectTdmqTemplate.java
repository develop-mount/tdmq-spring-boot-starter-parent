package com.seelyn.tdmq.producer;

import com.seelyn.tdmq.utils.JsonMapperUtils;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.TypedMessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ObjectTdmqTemplate extends TdmqTemplate<byte[]> {
    public ObjectTdmqTemplate(PulsarClient pulsarClient) {
        super(pulsarClient);
    }

    public <T> TypedMessageBuilder<byte[]> createMessageOfObject(String topic, T message) {

        String json = JsonMapperUtils.getInstance().toJson(message);
        return super.createMessage(topic, json.getBytes(StandardCharsets.UTF_8));
    }

    public <T> MessageId sendOfObject(String topic, T message) throws PulsarClientException {

        String json = JsonMapperUtils.getInstance().toJson(message);
        return super.send(topic, json.getBytes(StandardCharsets.UTF_8));
    }

    public <T> CompletableFuture<MessageId> sendAsyncOfObject(String topic, T message) {

        String json = JsonMapperUtils.getInstance().toJson(message);
        return super.sendAsync(topic, json.getBytes(StandardCharsets.UTF_8));
    }
}

package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import com.seelyn.tdmq.utils.JsonMapperUtils;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class ListBaseBytesListener<T> implements TdmqListener<byte[]> {

    private final Class<T> resolveClass;

    public ListBaseBytesListener() {
        Type superClass = getClass().getGenericSuperclass();
        //如果泛型类是Class的实例，不包含泛型
        if (superClass instanceof Class<?>) {
            throw new RuntimeException("缺少泛型实现");
        }
        //noinspection unchecked
        resolveClass = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> message) throws MessageRedeliverException {

        byte[] bytesData = message.getValue();
        if (bytesData == null || bytesData.length == 0) {
            return;
        }
        String json = new String(bytesData, StandardCharsets.UTF_8);
        if (!StringUtils.hasLength(json)) {
            return;
        }
        List<T> data = JsonMapperUtils.getInstance().fromJson(json, List.class, resolveClass);
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        receive(consumer, message, data);
    }

    /**
     * 接收byte数组转对象
     *
     * @param consumer 订阅对象
     * @param message  字节数组消息
     * @param data     转换后的对象
     * @throws MessageRedeliverException 抛回重新处理异常
     */
    protected abstract void receive(Consumer<byte[]> consumer, Message<byte[]> message, List<T> data) throws MessageRedeliverException;

}

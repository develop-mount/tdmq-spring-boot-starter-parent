package com.seelyn.tdmq;

import com.seelyn.tdmq.exception.MessageRedeliverException;
import com.seelyn.tdmq.utils.JsonMapperUtils;
import com.seelyn.tdmq.utils.ResolvableTypeUtils;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基础对象订阅
 *
 * @author linfeng
 */
public abstract class BaseObjectTdmqListener<T> implements TdmqListener<byte[]> {

    private final Class<T> resolveClass;

    public BaseObjectTdmqListener() {
        Type superClass = getClass().getGenericSuperclass();
        //如果泛型类是Class的实例，不包含泛型
        if (superClass instanceof Class<?>) {
            throw new RuntimeException(String.format("%s缺少泛型实现", BaseObjectTdmqListener.class.getName()));
        }
        ResolvableType resolvableType = ResolvableType.forType(superClass);
        List<Class<?>> classList = ResolvableTypeUtils.getResolvableType(resolvableType.getGeneric(0));
        if (CollectionUtils.isEmpty(classList)) {
            throw new RuntimeException(String.format("%s缺少泛型实现", BaseObjectTdmqListener.class.getName()));
        }
        if (classList.size() > 1) {
            throw new RuntimeException(String.format("%s不能包含多层泛型实现", BaseObjectTdmqListener.class.getName()));
        }
        //noinspection unchecked
        resolveClass = (Class<T>) classList.get(0);
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
        T data = JsonMapperUtils.getInstance().fromJson(json, resolveClass);
        if (data != null) {
            receive(consumer, message, data);
        }
    }

    /**
     * 接收byte数组转对象
     *
     * @param consumer 订阅对象
     * @param message  字节数组消息
     * @param data     转换后的对象
     * @throws MessageRedeliverException 抛回重新处理异常
     */
    protected abstract void receive(Consumer<byte[]> consumer, Message<byte[]> message, T data) throws MessageRedeliverException;

}

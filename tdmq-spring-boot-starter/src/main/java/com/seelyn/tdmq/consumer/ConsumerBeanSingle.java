package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerBeanSingle {

    private final TdmqHandler handler;
    private final TdmqListener<?> listener;
    private final Class<?> genericType;

    ConsumerBeanSingle(TdmqHandler handler, TdmqListener<?> listener, Class<?> genericType) {
        this.handler = handler;
        this.listener = listener;
        this.genericType = genericType;
    }

    public TdmqHandler getHandler() {
        return handler;
    }

    @SuppressWarnings("rawtypes")
    public TdmqListener getListener() {
        return listener;
    }

    public Class<?> getGenericType() {
        return genericType;
    }
}

package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerBeanBatch extends ConsumerBean {

    private final TdmqHandler handler;
    private final BatchTdmqListener<?> bean;
    private final Class<?> genericType;

    ConsumerBeanBatch(String name, TdmqHandler handler, BatchTdmqListener<?> bean, Class<?> genericType) {
        super(name, handler);
        this.handler = handler;
        this.bean = bean;
        this.genericType = genericType;
    }

    public TdmqHandler getHandler() {
        return handler;
    }

    @SuppressWarnings("rawtypes")
    public BatchTdmqListener getListener() {
        return bean;
    }

    public Class<?> getGenericType() {
        return genericType;
    }
}

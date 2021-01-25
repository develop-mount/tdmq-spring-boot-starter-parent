package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqBatchListener;
import com.seelyn.tdmq.annotation.TdmqHandler;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerBatchMessage {

    private final TdmqHandler annotation;
    private final TdmqBatchListener<?> bean;
    private final Class<?> paramType;

    ConsumerBatchMessage(TdmqHandler annotation, TdmqBatchListener<?> bean, Class<?> paramType) {
        this.annotation = annotation;
        this.bean = bean;
        this.paramType = paramType;
    }

    public TdmqHandler getAnnotation() {
        return annotation;
    }

    @SuppressWarnings("rawtypes")
    public TdmqBatchListener getListener() {
        return bean;
    }

    public Class<?> getParamType() {
        return paramType;
    }
}

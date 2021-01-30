package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerBeanBatch {

    private final TdmqHandler annotation;
    private final BatchTdmqListener<?> bean;
    private final Class<?> paramType;

    ConsumerBeanBatch(TdmqHandler annotation, BatchTdmqListener<?> bean, Class<?> paramType) {
        this.annotation = annotation;
        this.bean = bean;
        this.paramType = paramType;
    }

    public TdmqHandler getAnnotation() {
        return annotation;
    }

    @SuppressWarnings("rawtypes")
    public BatchTdmqListener getListener() {
        return bean;
    }

    public Class<?> getParamType() {
        return paramType;
    }
}

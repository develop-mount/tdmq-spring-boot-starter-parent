package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.TdmqListener;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerSingleMessage {

    private final TdmqHandler annotation;
    private final TdmqListener bean;
    private final Class<?> paramType;

    ConsumerSingleMessage(TdmqHandler annotation, TdmqListener bean, Class<?> paramType) {
        this.annotation = annotation;
        this.bean = bean;
        this.paramType = paramType;
    }

    public TdmqHandler getAnnotation() {
        return annotation;
    }

    public TdmqListener getBean() {
        return bean;
    }

    public Class<?> getParamType() {
        return paramType;
    }
}

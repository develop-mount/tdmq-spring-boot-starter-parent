package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerBeanSingle {

    private final TdmqHandler annotation;
    private final TdmqListener<?> listener;
    private final Class<?> paramType;

    ConsumerBeanSingle(TdmqHandler annotation, TdmqListener<?> listener, Class<?> paramType) {
        this.annotation = annotation;
        this.listener = listener;
        this.paramType = paramType;
    }

    public TdmqHandler getAnnotation() {
        return annotation;
    }

    @SuppressWarnings("rawtypes")
    public TdmqListener getListener() {
        return listener;
    }

    public Class<?> getParamType() {
        return paramType;
    }
}

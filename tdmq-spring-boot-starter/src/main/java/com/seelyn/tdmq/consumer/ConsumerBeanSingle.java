package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import org.springframework.util.StringUtils;

/**
 * 订阅者执行方法
 *
 * @author linfeng
 */
public class ConsumerBeanSingle extends ConsumerBean {

    private final TdmqHandler handler;
    private final TdmqListener<?> listener;
    private final Class<?> genericType;

    ConsumerBeanSingle(String name, TdmqHandler handler, TdmqListener<?> listener, Class<?> genericType) {
        super(name, handler);
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

package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqBatchListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.TdmqListener;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author linfeng
 */
@Component
public class ConsumerMethodPostProcessor implements ConsumerMethodCollection, BeanPostProcessor, Ordered {

    private final ConcurrentMap<String, ConsumerSingleMessage> singleMessageConcurrentMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConsumerBatchMessage> batchMessageConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> targetClass = AopUtils.getTargetClass(bean);
        TdmqHandler tdmqHandler = AnnotationUtils.findAnnotation(targetClass, TdmqHandler.class);
        if (tdmqHandler == null) {
            return bean;
        }
        if (bean instanceof TdmqListener || bean instanceof TdmqBatchListener) {

            ResolvableType resolvableType = ResolvableType.forClass(targetClass);
            Class<?> resolveInterface = resolvableType.getInterfaces()[0].getGeneric(0).resolve();

            if (bean instanceof TdmqBatchListener) {

                batchMessageConcurrentMap.putIfAbsent(targetClass.getName(), new ConsumerBatchMessage(tdmqHandler, (TdmqBatchListener) bean, resolveInterface));
            }
            if (bean instanceof TdmqListener) {

                singleMessageConcurrentMap.putIfAbsent(targetClass.getName(), new ConsumerSingleMessage(tdmqHandler, (TdmqListener) bean, resolveInterface));
            }
        } else {

            throw new IllegalStateException(String.format(
                    "@TdmqHandler found on bean target class '%s', " +
                            "but not found in any interface(s) for TdmqListener or TdmqBatchListener", targetClass.getSimpleName()));
        }

        return bean;
    }

    @Override
    public ConcurrentMap<String, ConsumerSingleMessage> getSingleMessageConsumer() {
        return singleMessageConcurrentMap;
    }

    @Override
    public ConcurrentMap<String, ConsumerBatchMessage> getBatchMessageConsumer() {
        return batchMessageConcurrentMap;
    }
}

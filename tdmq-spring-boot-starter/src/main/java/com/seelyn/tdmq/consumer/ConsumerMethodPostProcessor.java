package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.TdmqBatchListener;
import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author linfeng
 */
public class ConsumerMethodPostProcessor implements ConsumerMethodCollection, BeanPostProcessor, Ordered {

    private final ConcurrentMap<String, ConsumerSingleMessage> singleMessageConcurrentMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConsumerBatchMessage> batchMessageConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessAfterInitialization(@SuppressWarnings("NullableProblems") Object bean,
                                                 @SuppressWarnings("NullableProblems") String beanName) throws BeansException {

        Class<?> targetClass = AopUtils.getTargetClass(bean);
        TdmqHandler tdmqHandler = AnnotationUtils.findAnnotation(targetClass, TdmqHandler.class);
        if (tdmqHandler == null) {
            return bean;
        }
        if (bean instanceof TdmqListener || bean instanceof TdmqBatchListener) {

            ResolvableType resolvableType = ResolvableType.forClass(targetClass);
            Class<?> resolveInterface = resolvableType.getInterfaces()[0].getGeneric(0).resolve();

            if (bean instanceof TdmqListener) {

                singleMessageConcurrentMap.putIfAbsent(targetClass.getName(), new ConsumerSingleMessage(tdmqHandler, (TdmqListener<?>) bean, resolveInterface));
            }
            if (bean instanceof TdmqBatchListener) {

                batchMessageConcurrentMap.putIfAbsent(targetClass.getName(), new ConsumerBatchMessage(tdmqHandler, (TdmqBatchListener<?>) bean, resolveInterface));
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

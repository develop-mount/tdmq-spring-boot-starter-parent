package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.BatchTdmqListener;
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
public class ConsumerBeanPostProcessor implements ConsumerBeanCollection, BeanPostProcessor, Ordered {

    private final ConcurrentMap<String, ConsumerBeanSingle> singleMessageConcurrentMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConsumerBeanBatch> batchMessageConcurrentMap = new ConcurrentHashMap<>();

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

        if (bean instanceof TdmqListener || bean instanceof BatchTdmqListener) {

            Class<?> resolveInterface = getResolvableClass(targetClass);
            if (bean instanceof TdmqListener) {

                singleMessageConcurrentMap.putIfAbsent(targetClass.getName(), new ConsumerBeanSingle(tdmqHandler, (TdmqListener<?>) bean, resolveInterface));
            }
            if (bean instanceof BatchTdmqListener) {

                batchMessageConcurrentMap.putIfAbsent(targetClass.getName(), new ConsumerBeanBatch(tdmqHandler, (BatchTdmqListener<?>) bean, resolveInterface));
            }
        } else {

            throw new IllegalStateException(String.format(
                    "@TdmqHandler found on bean target class '%s', " +
                            "but not found in any interface(s) for TdmqListener or BatchTdmqListener", targetClass.getSimpleName()));
        }

        return bean;
    }

    private Class<?> getResolvableClass(Class<?> targetClass) {
        ResolvableType resolvableType = ResolvableType.forClass(targetClass);
        if (resolvableType.getInterfaces().length <= 0) {
            return getResolvableClass(targetClass.getSuperclass());
        } else {
            return resolvableType.getInterfaces()[0].getGeneric(0).resolve();
        }
    }

    @Override
    public ConcurrentMap<String, ConsumerBeanSingle> getSingleMessageConsumer() {
        return singleMessageConcurrentMap;
    }

    @Override
    public ConcurrentMap<String, ConsumerBeanBatch> getBatchMessageConsumer() {
        return batchMessageConcurrentMap;
    }
}

package com.seelyn.tdmq.consumer;

import com.seelyn.tdmq.BatchTdmqListener;
import com.seelyn.tdmq.TdmqListener;
import com.seelyn.tdmq.annotation.TdmqHandler;
import com.seelyn.tdmq.exception.NotSupportedException;
import com.seelyn.tdmq.utils.SchemaUtils;
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
                                                 String beanName) throws BeansException {

        Class<?> targetClass = AopUtils.getTargetClass(bean);
        TdmqHandler tdmqHandler = AnnotationUtils.findAnnotation(targetClass, TdmqHandler.class);
        if (tdmqHandler == null) {
            return bean;
        }

        if (bean instanceof TdmqListener || bean instanceof BatchTdmqListener) {

            Class<?> resolveInterface = getResolvableClass(targetClass);

            if (!SchemaUtils.validateSchema(resolveInterface)) {
                throw new NotSupportedException(String.format("TdmqListener<T>或BatchTdmqListener<T> 的T不能为此类型%s，类不能为集合或Map",
                        resolveInterface.getName()));
            }

            if (bean instanceof TdmqListener) {

                singleMessageConcurrentMap.putIfAbsent(targetClass.getName(),
                        new ConsumerBeanSingle(targetClass.getName(), tdmqHandler, (TdmqListener<?>) bean, resolveInterface));
            } else {

                batchMessageConcurrentMap.putIfAbsent(targetClass.getName(),
                        new ConsumerBeanBatch(targetClass.getName(), tdmqHandler, (BatchTdmqListener<?>) bean, resolveInterface));
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

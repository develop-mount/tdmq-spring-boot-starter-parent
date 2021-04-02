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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 订阅listener
 *
 * @author linfeng
 */
public class ConsumerMetadataPostProcessor implements ConsumerMetadataMap, BeanPostProcessor, Ordered {

    private final ConcurrentMap<String, ConsumerMetadata> concurrentMap = new ConcurrentHashMap<>();

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

            concurrentMap.putIfAbsent(targetClass.getName(),
                    new ConsumerMetadata(targetClass.getName(), bean, tdmqHandler, resolveInterface));

        } else {

            throw new IllegalStateException(String.format(
                    "@TdmqHandler found on bean target class '%s', " +
                            "but not found in any interface(s) for TdmqListener or BatchTdmqListener", targetClass.getSimpleName()));
        }

        return bean;
    }

    /**
     * 获得泛型类型
     *
     * @param targetClass 目标类
     * @return 泛型类型
     */
    private Class<?> getResolvableClass(Class<?> targetClass) {
        ResolvableType resolvableType = ResolvableType.forClass(targetClass);
        if (resolvableType.getInterfaces().length <= 0) {
            return getResolvableClass(targetClass.getSuperclass());
        } else {
            return resolvableType.getInterfaces()[0].getGeneric(0).resolve();
        }
    }

    @Override
    public Map<String, ConsumerMetadata> getMap() {
        return concurrentMap;
    }

}

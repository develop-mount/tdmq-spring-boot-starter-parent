package com.seelyn.tdmq.consumer;

import java.util.concurrent.ConcurrentMap;

/**
 * 订阅者方法集合接口
 *
 * @author linfeng
 */
public interface ConsumerBeanCollection {

    /**
     * TDMQ 订阅者执行方法
     *
     * @return 订阅者执行方法Map
     */
    ConcurrentMap<String, ConsumerBeanSingle> getSingleMessageConsumer();

    /**
     * 订阅者执行方法
     *
     * @return 订阅者执行方法Map
     */
    ConcurrentMap<String, ConsumerBeanBatch> getBatchMessageConsumer();

}

package com.seelyn.tdmq.consumer;

import java.util.concurrent.ConcurrentMap;

/**
 * 订阅者方法集合接口
 *
 * @author linfeng
 */
public interface ConsumerMethodCollection {

    /**
     * TDMQ 订阅者执行方法
     *
     * @return 订阅者执行方法Map
     */
    ConcurrentMap<String, ConsumerSingleMessage> getSingleMessageConsumer();

    /**
     * 订阅者执行方法
     *
     * @return 订阅者执行方法Map
     */
    ConcurrentMap<String, ConsumerBatchMessage> getBatchMessageConsumer();

}

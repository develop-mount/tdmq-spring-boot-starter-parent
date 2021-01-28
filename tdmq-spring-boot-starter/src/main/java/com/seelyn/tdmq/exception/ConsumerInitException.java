package com.seelyn.tdmq.exception;

/**
 * TDMQ 初始化一次
 * @author linfeng
 */
public class ConsumerInitException extends RuntimeException {

    public ConsumerInitException(String message) {
        super(message);
    }

    public ConsumerInitException(String message, Throwable cause) {
        super(message, cause);
    }

}

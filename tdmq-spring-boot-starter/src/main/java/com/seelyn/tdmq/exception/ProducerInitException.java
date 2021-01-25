package com.seelyn.tdmq.exception;

/**
 * @author linfeng
 */
public class ProducerInitException extends RuntimeException {
    public ProducerInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProducerInitException(String message) {
        super(message);
    }
}

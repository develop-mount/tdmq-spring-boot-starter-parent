package com.seelyn.tdmq.exception;

/**
 * TDMQ 抛回MQ，后续处理
 *
 * @author linfeng
 */
public class MessageRedeliverException extends Exception {

    public MessageRedeliverException(String message) {
        super(message);
    }
}

package com.seelyn.eventbus.context;

import java.util.EventObject;

/**
 * 总线事件
 */
public abstract class BusEvent extends EventObject {

    private static final long serialVersionUID = 5033684583529517550L;
    private final long timestamp = System.currentTimeMillis();

    public BusEvent(Object source) {
        super(source);
    }

    public final long getTimestamp() {
        return this.timestamp;
    }

}

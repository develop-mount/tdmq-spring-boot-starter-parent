package com.seelyn.eventbus.context;

import java.util.EventListener;

/**
 * 总线事件监听器
 *
 * @param <E>
 */
@FunctionalInterface
public interface BusListener<E extends BusEvent> extends EventListener {

    void onBusEvent(E event);
}

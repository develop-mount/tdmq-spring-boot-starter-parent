package com.seelyn.tdmq.event;

public class BusEvent implements java.io.Serializable {

    private static final long serialVersionUID = 5516075349620653480L;

    public BusEvent() {
    }

    /**
     * The object on which the Event initially occurred.
     */
    protected transient Object source;

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

}

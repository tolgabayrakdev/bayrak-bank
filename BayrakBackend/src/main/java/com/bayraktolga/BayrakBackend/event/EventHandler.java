package com.bayraktolga.BayrakBackend.event;

public interface EventHandler {
    String getEventType();
    void handle(Object payload);
}

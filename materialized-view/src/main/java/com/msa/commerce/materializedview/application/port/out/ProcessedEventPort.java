package com.msa.commerce.materializedview.application.port.out;

public interface ProcessedEventPort {

    boolean markProcessed(String eventId);

    void unmark(String eventId);

}

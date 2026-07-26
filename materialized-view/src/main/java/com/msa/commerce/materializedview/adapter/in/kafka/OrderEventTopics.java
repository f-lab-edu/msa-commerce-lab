package com.msa.commerce.materializedview.adapter.in.kafka;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderEventTopics {

    public static final String ORDER_CREATED = "order.created";

    public static final String ORDER_UPDATED = "order.updated";

    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

}

package com.msa.commerce.orchestrator.adapter.out.kafka;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaTopics {

    public static final String ORDER_CREATED = "order.created";

    public static final String ORDER_UPDATED = "order.updated";

    public static final String PAYMENT_RESULT = "payment.result";

    public static final String RETRY_EVENTS = "retry.events";

    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

}

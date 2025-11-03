package com.msa.commerce.orchestrator.domain;

import lombok.Getter;

@Getter
public enum AddressType {
    DEFAULT("기본 배송지"),
    ALTERNATE("대체 배송지"),
    GIFT("선물 배송지");

    private final String description;

    AddressType(String description) {
        this.description = description;
    }

}

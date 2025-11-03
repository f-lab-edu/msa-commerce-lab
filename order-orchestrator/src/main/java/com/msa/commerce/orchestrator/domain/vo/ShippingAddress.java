package com.msa.commerce.orchestrator.domain.vo;

import java.util.Objects;

import com.msa.commerce.orchestrator.domain.AddressType;

public record ShippingAddress(
    AddressType addressType, String recipientName, String phoneNumber,
    String zipCode, String addressLine1, String addressLine2
) {

    public ShippingAddress {
        Objects.requireNonNull(addressType, "Address type cannot be null");
    }

    public static ShippingAddress createDefault() {
        return new ShippingAddress(
            AddressType.DEFAULT, null, null,
            null, null, null
        );
    }

    public static ShippingAddress create(
        AddressType addressType, String recipientName, String phoneNumber,
        String zipCode, String addressLine1, String addressLine2
    ) {
        return new ShippingAddress(
            addressType, recipientName, phoneNumber,
            zipCode, addressLine1, addressLine2
        );
    }

}

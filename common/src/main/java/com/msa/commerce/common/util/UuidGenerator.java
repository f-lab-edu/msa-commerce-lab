package com.msa.commerce.common.util;

import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

public final class UuidGenerator {

    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    private UuidGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static UUID generate() {
        return GENERATOR.generate();
    }

}

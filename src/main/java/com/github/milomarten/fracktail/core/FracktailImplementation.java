package com.github.milomarten.fracktail.core;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public enum FracktailImplementation {
    FRACKTAIL_5("fracktail5"),;

    private final String value;

    public static FracktailImplementation get(String value) {
        return Arrays.stream(FracktailImplementation.values())
                .filter(f -> Objects.equals(f.value, value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such implementation: " + value));
    }
}

package com.github.milomarten.fracktail4.base.parameter.type;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EnumType implements ParameterType {
    private final List<String> options;

    @Override
    public String format(String value) {
        return String.join("|", options);
    }
}

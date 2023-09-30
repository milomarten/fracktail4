package com.github.milomarten.fracktail4.base.parameter.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberType implements ParameterType {
    public static final NumberType INSTANCE = new NumberType();

    @Override
    public String format(String value) {
        return "<" + value + ">";
    }
}

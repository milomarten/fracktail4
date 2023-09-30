package com.github.milomarten.fracktail4.base.parameter.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringType implements ParameterType {
    public static final StringType INSTANCE = new StringType();

    @Override
    public String format(String value) {
        return "<" + value + ">";
    }
}

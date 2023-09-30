package com.github.milomarten.fracktail4.base.parameter.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constant implements ParameterType {
    public static final Constant INSTANCE = new Constant();
    @Override
    public String format(String value) {
        return value;
    }
}

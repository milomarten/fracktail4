package com.github.milomarten.fracktail5.platform;

public interface ParameterConverter<IN, OUT> {
    public OUT convert(IN in);
}

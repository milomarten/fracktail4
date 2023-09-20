package com.github.milomarten.fracktail4.base;

import lombok.Data;

import java.util.Optional;
import java.util.OptionalInt;

@Data
public class Parameters {
    private final String[] sourceParams;

    public int getNumberOfParameters() { return this.sourceParams.length; }

    public Optional<String> getParameter(int idx) {
        if (idx < 0) {
            idx = getNumberOfParameters() + idx;
        }
        if (idx >= 0 && idx < getNumberOfParameters()) {
            return Optional.of(sourceParams[idx]);
        } else {
            return Optional.empty();
        }
    }

    public OptionalInt getIntParameter(int idx) {
        var param = getParameter(idx);
        return param.map(s -> OptionalInt.of(Integer.parseInt(s)))
                .orElseGet(OptionalInt::empty);
    }
}

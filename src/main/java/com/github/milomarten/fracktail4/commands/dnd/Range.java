package com.github.milomarten.fracktail4.commands.dnd;

import java.math.BigDecimal;

public record Range(BigDecimal low, BigDecimal high) {
    public Range add(BigDecimal other) {
        return new Range(this.low.add(other), this.high.add(other));
    }

    public Range add(Range other) {
        return new Range(
                this.low.add(other.low),
                this.high.add(other.high)
        );
    }

    public Range multiply(BigDecimal scale) {
        return new Range(this.low.multiply(scale), this.high.multiply(scale));
    }

    public Range subtract(BigDecimal other) {
        return new Range(this.low.subtract(other), this.high.subtract(other));
    }
}

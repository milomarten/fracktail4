package com.github.milomarten.fracktail4.commands.dice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.RoundingMode;

@Data
@AllArgsConstructor
@Builder
public class DiceEvaluatorOptions {
    @Builder.Default private OutputType outputType = OutputType.PLAIN;
    @Builder.Default private RoundingMode roundingMode = RoundingMode.DOWN;

    public enum OutputType {
        PLAIN,
        ANSI
    }
}

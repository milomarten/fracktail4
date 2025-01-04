package com.github.milomarten.fracktail4.commands.dice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import java.math.RoundingMode;

@Data
@AllArgsConstructor
@Builder
public class DiceEvaluatorOptions {
    @Builder.Default private OutputType outputType = OutputType.PLAIN;
    @Builder.Default private RoundingMode roundingMode = RoundingMode.DOWN;
    @Builder.Default private UniformRandomProvider random = RandomSource.MT.create();

    public enum OutputType {
        PLAIN,
        ANSI
    }
}

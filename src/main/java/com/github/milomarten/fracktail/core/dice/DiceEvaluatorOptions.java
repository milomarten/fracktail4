package com.github.milomarten.fracktail.core.dice;

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
    @Builder.Default private RoundingMode roundingMode = RoundingMode.DOWN;
    @Builder.Default private UniformRandomProvider random = RandomSource.MT.create();
}

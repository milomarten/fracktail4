package com.github.milomarten.fracktail.core.dice.table.multi;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

@Builder
@Data
public class MultiRollParams {
    private int quantity;
    private boolean distinct;
    @Builder.Default private UniformRandomProvider random = RandomSource.MT_64.create();
}

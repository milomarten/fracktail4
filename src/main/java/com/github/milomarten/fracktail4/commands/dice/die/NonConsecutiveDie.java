package com.github.milomarten.fracktail4.commands.dice.die;

import com.github.milomarten.fracktail4.commands.dice.RollResult;
import com.github.milomarten.fracktail4.commands.dice.Rollable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.List;

@RequiredArgsConstructor
public class NonConsecutiveDie implements Rollable<Integer> {
    private final List<Integer> options;

    @Override
    public RollResult<Integer> roll(UniformRandomProvider random) {
        var randomIdx = random.nextInt(options.size());
        return new RollResult<>(options.get(randomIdx));
    }
}

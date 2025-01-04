package com.github.milomarten.fracktail4.commands.dice.die;

import com.github.milomarten.fracktail4.commands.dice.DiceExpressionConfiguration;
import com.github.milomarten.fracktail4.commands.dice.RollResult;
import com.github.milomarten.fracktail4.commands.dice.Rollable;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import static com.github.milomarten.fracktail4.commands.dice.Utils.checkRange;

@Getter
@RequiredArgsConstructor
public class Die implements Rollable<Integer> {
    /**
     * The number of sides on the dice to roll.
     */
    private final int numFaces;

    @Override
    public RollResult<Integer> roll(UniformRandomProvider random) {
        if (numFaces == 0) { return new RollResult<>(0); }
        var roll = random.nextInt(1, numFaces + 1);

        if (roll == 1) { return new RollResult<>(roll, Status.CRITICAL_FAIL); }
        else if (roll == numFaces) { return new RollResult<>(roll, Status.CRITICAL_SUCCESS); }
        else { return new RollResult<>(roll); }
    }

    @Override
    public void validate() {
        checkRange(numFaces, 0, DiceExpressionConfiguration.MAX_DICE_SIDES, "Number of Sides");
    }
}

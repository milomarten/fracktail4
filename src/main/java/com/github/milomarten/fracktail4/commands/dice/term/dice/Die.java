package com.github.milomarten.fracktail4.commands.dice.term.dice;

import com.github.milomarten.fracktail4.commands.dice.term.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import static com.github.milomarten.fracktail4.commands.dice.Utils.checkRange;

@RequiredArgsConstructor
@AllArgsConstructor
public class Die implements Rollable<Integer> {
    /**
     * The number of sides on the dice to roll.
     */
    @Getter private final int numFaces;

    /**
     * The source of randomness for the dice rolls.
     * By default, uses a new instance of java.util.Random.
     */
    private UniformRandomProvider randomSource = RandomSource.MT.create();

    @Override
    public RollResult<Integer> roll() {
        if (numFaces == 0) { return new RollResult<>(0); }
        var roll = randomSource.nextInt(1, numFaces + 1);

        if (roll == 1) { return new RollResult<>(roll, Status.CRITICAL_FAIL); }
        else if (roll == numFaces) { return new RollResult<>(roll, Status.CRITICAL_SUCCESS); }
        else { return new RollResult<>(roll); }
    }

    @Override
    public void validate() {
        checkRange(numFaces, 0, 1000, "Number of Sides");
    }
}

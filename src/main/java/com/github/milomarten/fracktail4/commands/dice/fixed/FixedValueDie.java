package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.RollResult;
import com.github.milomarten.fracktail4.commands.dice.Rollable;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Represents a generic die which have an equal chance of rolling options presented by an enum
 * @param <E> The type that represents all options the face can be.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FixedValueDie<E> implements Rollable<E> {
    private final E[] options;

    /**
     * Create a die which uses the provided enum class, with specific randomness
     *
     * @param source The enum class
     */
    public static <E extends Enum<E>> FixedValueDie<E> fromEnum(Class<E> source) {
        return new FixedValueDie<>(source.getEnumConstants());
    }

    @Override
    public RollResult<E> roll(UniformRandomProvider random) {
        var roll = this.options[random.nextInt(this.options.length)];
        return new RollResult<>(roll, Status.NEUTRAL);
    }
}

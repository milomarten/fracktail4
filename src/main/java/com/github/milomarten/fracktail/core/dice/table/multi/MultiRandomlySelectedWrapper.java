package com.github.milomarten.fracktail.core.dice.table.multi;

import com.github.milomarten.fracktail.core.dice.table.RandomlySelected;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * A wrapper around a RandomlySelected to support MultiRandomlySelected.
 * This class natively supports all options in MultiRollParams by invoking the wrapped RandomlySelected some amount of times.
 * Sanity checks are also done before starting the roll:
 * - If requested quantity is negative, an IllegalArgumentException is thrown.
 * - If distinct = true and requested quantity is greater than the table size, an IllegalArgumentException is thrown.
 * <br>
 * For distinct queries, a guardrail of 100 "tries" is enforced. If this guardrail is exceeded, the rolls will give up,
 * and fewer than requested options will be returned.
 * @param <T>
 */
@RequiredArgsConstructor
public class MultiRandomlySelectedWrapper<T> implements MultiRandomlySelected<T> {
    // Guard to make sure we don't spin forever trying to get something unique.
    private static final int ITERATION_GUARDRAIL = 100;

    private final RandomlySelected<T> wrapper;

    @Override
    public List<T> getMultiple(MultiRollParams params) {
        validateParameters(params);
        if (params.isDistinct()) {
            var alreadyChosen = new HashSet<T>();
            var returnList = new ArrayList<T>();
            int loop = 0;
            while (returnList.size() < params.getQuantity() && loop < ITERATION_GUARDRAIL) {
                loop++;
                var roll = wrapper.get(params.getRandom());
                if (alreadyChosen.add(roll)) {
                    returnList.add(roll);
                }
            }
            return returnList;
        } else {
            return IntStream.range(0, params.getQuantity())
                    .mapToObj(i -> wrapper.get(params.getRandom()))
                    .toList();
        }
    }

    @Override
    public T get(UniformRandomProvider random) {
        return wrapper.get(random);
    }

    @Override
    public OptionalInt length() {
        return wrapper.length();
    }

    private void validateParameters(MultiRollParams params) {
        if (params.getQuantity() < 0) {
            throw new IllegalArgumentException("Requested <0 options");
        }
        if (params.isDistinct()) {
            var size = wrapper.length();
            if (size.isPresent() && params.getQuantity() > size.getAsInt()) {
                throw new IllegalArgumentException(String.format("Wanted %d unique options, but only %d are possible",
                        params.getQuantity(), size.getAsInt()));
            }
        }
    }
}

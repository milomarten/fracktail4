package com.github.milomarten.fracktail.core.dice;

/**
 * Constant values, to allow for consistent operation across dice evaluation
 */
public class DiceExpressionConfiguration {
    /**
     * The maximum number of dice permitted in a single expression.
     */
    public static final int MAX_DICE = 32;
    /**
     * The maximum number of sides on a single dice.
     */
    public static final int MAX_DICE_SIDES = 1000;

    /**
     * The maximum number of integer digits any number can be.
     */
    public static final int MAX_DIGITS_INTEGER_PART = 18;

    /**
     * The maximum number of fraction digits a number can have.
     * Unlike the others, this does not emit an error if exceeded.
     * Instead, the final output will be rounded to this number of digits at most.
     */
    public static final int MAX_DIGITS_FRACTION_PART = 4;
}

package com.github.milomarten.fracktail4.commands.dice;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @ParameterizedTest
    @CsvSource(value = {
            "5, 1",
            "50, 2",
            "34, 2",
            "100, 3",
            "123, 3",
            "5.1, 1",
            "1.2345, 1",
            "123.456, 3",
            "0.34535263527251, 0"
    })
    public void testDigitCount(String value, int numberOfExpectedDigits) {
        var bd = new BigDecimal(value);
        assertEquals(numberOfExpectedDigits, Utils.numberOfIntegerDigits(bd));
    }
}
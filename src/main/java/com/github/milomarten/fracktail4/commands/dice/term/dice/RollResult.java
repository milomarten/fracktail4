package com.github.milomarten.fracktail4.commands.dice.term.dice;

import com.github.milomarten.fracktail4.commands.dice.term.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class RollResult<T> {
    private final T value;
    private Status status = Status.NEUTRAL;
}

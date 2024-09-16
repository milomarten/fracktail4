package com.github.milomarten.fracktail4.commands.dice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DiceEvaluatorOptions {
    @Builder.Default private OutputType outputType = OutputType.ANSI;

    public enum OutputType {
        PLAIN,
        ANSI
    }
}

package com.github.milomarten.fracktail4.base.parameter;

import com.github.milomarten.fracktail4.base.CommandConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoOpParameterParser implements ParameterParser {
    public static final NoOpParameterParser INSTANCE = new NoOpParameterParser();

    @Override
    public Parameters parse(CommandConfiguration configuration, String contents) {
        var wrapped = new String[]{ contents };
        return new Parameters(wrapped);
    }
}

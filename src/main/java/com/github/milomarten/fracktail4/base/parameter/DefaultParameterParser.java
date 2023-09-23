package com.github.milomarten.fracktail4.base.parameter;

import com.github.milomarten.fracktail4.base.CommandConfiguration;
import com.github.milomarten.fracktail4.base.Parameters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultParameterParser implements ParameterParser {
    public static final DefaultParameterParser INSTANCE = new DefaultParameterParser();
    @Override
    public Parameters parse(CommandConfiguration configuration, String contents) {
        var tokens = contents.split(configuration.getDelimiter());
        return new Parameters(tokens);
    }
}

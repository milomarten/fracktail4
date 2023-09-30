package com.github.milomarten.fracktail4.base.parameter;

import com.github.milomarten.fracktail4.base.CommandConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultParameterParser implements ParameterParser {
    public static final DefaultParameterParser INSTANCE = new DefaultParameterParser();
    public static final Parameters EMPTY_PARAMS = new Parameters(new String[0]);

    @Override
    public Parameters parse(CommandConfiguration configuration, String contents) {
        if (contents.length() == 0) {
            return EMPTY_PARAMS;
        }
        var tokens = contents.split(configuration.getDelimiter());
        return new Parameters(tokens);
    }
}

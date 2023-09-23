package com.github.milomarten.fracktail4.base.parameter;

import com.github.milomarten.fracktail4.base.CommandConfiguration;
import com.github.milomarten.fracktail4.base.Parameters;
import lombok.Data;

@Data
public class VarargsParameterParser implements ParameterParser {
    private final int expectedParameterCount;

    @Override
    public Parameters parse(CommandConfiguration configuration, String contents) {
        var tokens = contents.split(configuration.getDelimiter(), expectedParameterCount);
        return new Parameters(tokens);
    }
}

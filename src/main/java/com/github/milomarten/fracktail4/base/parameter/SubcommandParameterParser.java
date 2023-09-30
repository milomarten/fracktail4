package com.github.milomarten.fracktail4.base.parameter;

import com.github.milomarten.fracktail4.base.CommandConfiguration;
import lombok.Builder;
import lombok.Singular;

import java.util.Map;

@Builder
public class SubcommandParameterParser implements ParameterParser {
    @Singular private Map<String, ParameterParser> options;
    @Builder.Default private ParameterParser defaultParser = DefaultParameterParser.INSTANCE;

    @Override
    public Parameters parse(CommandConfiguration configuration, String contents) {
        if (contents.isBlank()) {
            return defaultParser.parse(configuration, contents);
        }
        String[] tokens = contents.split(configuration.getDelimiter(), 2);
        if (options.containsKey(tokens[0])) {
            String remaining = tokens.length == 1 ? "" : tokens[1];
            Parameters inner = options.get(tokens[0]).parse(configuration, remaining);
            return inner.prepend(tokens[0]);
        } else {
            return defaultParser.parse(configuration, contents);
        }
    }
}

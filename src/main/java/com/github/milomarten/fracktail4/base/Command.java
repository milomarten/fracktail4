package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.DefaultParameterParser;
import com.github.milomarten.fracktail4.base.parameter.ParameterParser;

public interface Command {
    CommandData getCommandData();

    default ParameterParser getParameterParser() {
        return DefaultParameterParser.INSTANCE;
    }
}

package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.DefaultParameterParser;
import com.github.milomarten.fracktail4.base.parameter.ParameterParser;
import com.github.milomarten.fracktail4.permissions.Role;

import java.util.stream.Collectors;

public interface Command {
    CommandData getCommandData();

    default Role getRequiredRole() {
        return Role.NORMAL;
    }

    default ParameterParser getParameterParser() {
        return DefaultParameterParser.INSTANCE;
    }
}

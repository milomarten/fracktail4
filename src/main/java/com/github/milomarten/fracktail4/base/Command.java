package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.DefaultParameterParser;
import com.github.milomarten.fracktail4.base.parameter.ParameterParser;
import com.github.milomarten.fracktail4.permissions.Role;

import java.util.List;
import java.util.stream.Collectors;

public interface Command {
    CommandData getCommandData();

    default Role getRequiredRole() {
        return Role.NORMAL;
    }

    default ParameterParser getParameterParser() {
        return DefaultParameterParser.INSTANCE;
    }

    default String getHelpText() {
        CommandData cd = this.getCommandData();
        return String.format("%s %s - %s",
                String.join(",", cd.getAliases()),
                helpStringForParams(cd.getParams()),
                cd.getDescription());
    }

    private static String helpStringForParams(List<CommandData.Param> params) {
        return params.stream()
                .map(p -> {
                    if (p.isOptional()) {
                        return "[_" + p.getName() + "_]";
                    } else {
                        return "_" + p.getName() + "_";
                    }
                })
                .collect(Collectors.joining(" "));
    }
}

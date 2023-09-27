package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.DefaultParameterParser;
import com.github.milomarten.fracktail4.base.parameter.ParameterParser;
import com.github.milomarten.fracktail4.permissions.Role;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Set;

@Builder
@Data
public class CommandData {
    private String id;
    @Singular private Set<String> aliases;
    private String description;
    @Singular private List<Param> params;
    @Builder.Default private Role role = Role.NORMAL;
    @Builder.Default private ParameterParser parameterParser = DefaultParameterParser.INSTANCE;

    @Data
    @Builder
    public static class Param {
        private String name;
        private boolean optional;
    }
}

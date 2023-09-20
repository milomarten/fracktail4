package com.github.milomarten.fracktail4.base;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Set;

@Builder
@Data
public class CommandData {
    private String id;
    @Singular
    private Set<String> aliases;
    private String description;
    @Singular private List<Param> params;

    @Data
    @Builder
    public static class Param {
        private String name;
        private boolean optional;
    }
}

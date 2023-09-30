package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.type.ParameterType;
import com.github.milomarten.fracktail4.base.parameter.type.StringType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommandParam {
    private String name;
    private boolean optional;
    @Builder.Default private ParameterType type = StringType.INSTANCE;
}

package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.type.Constant;
import com.github.milomarten.fracktail4.base.parameter.type.StringType;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class CommandFlow {
    private String description;
    @Singular
    private List<CommandParam> params;

    public static CommandFlow of(String description, String constant) {
        return of(description, new String[]{constant});
    }

    public static CommandFlow of(String description, String constant, String param1) {
        return of(description, new String[]{constant}, param1);
    }

    public static CommandFlow of(String description, String constant, String param1, String param2) {
        return of(description, new String[]{constant}, param1, param2);
    }

    public static CommandFlow of(String description, String[] constants, String... params) {
        var builder = CommandFlow.builder()
                .description(description);
        for (String param : constants) {
            builder = builder.param(CommandParam.builder()
                    .name(param)
                    .type(Constant.INSTANCE)
                    .build());
        }
        for (String param : params) {
            builder = builder.param(CommandParam.builder()
                    .name(param)
                    .type(StringType.INSTANCE)
                    .build());
        }
        return builder.build();
    }
}

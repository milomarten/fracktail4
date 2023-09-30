package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.parameter.Parameters;
import reactor.core.publisher.Mono;

import java.util.Set;

public class SimpleCommand implements AllPlatformCommand {
    private final CommandData commandData;
    private final String response;

    public SimpleCommand(String command, String response, String... aliases) {
        this.commandData = CommandData.builder()
                .id(command)
                .alias(command)
                .aliases(Set.of(aliases))
                .description(response)
                .build();
        this.response = response;
    }

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }

    @Override
    public Mono<?> doCommand(Parameters parameters, Context context) {
        return context.respond(this.response);
    }
}

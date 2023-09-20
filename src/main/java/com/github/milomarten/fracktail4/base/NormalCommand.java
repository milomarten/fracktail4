package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

public abstract class NormalCommand implements Command, DiscordCommand {
    @Value("${command.delimiter: }")
    protected String delimiter = " ";

    @Override
    public Mono<?> doCommand(MessageCreateEvent event) {
        String[] tokens = event.getMessage().getContent().split(this.delimiter);
        String[] params = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, params, 0, params.length);
        return event.getMessage()
                .getChannel()
                .flatMap(mc -> mc.createMessage(doCommand(new Parameters(params))));
    }

    protected abstract String doCommand(Parameters params);
}

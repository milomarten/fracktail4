package com.github.milomarten.fracktail4.amtrak;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.milomarten.fracktail4.amtrak.parameters.AmtrakCommandParameters;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class AmtrakCommand extends AbstractSlashCommand<AmtrakCommandParameters> {
    private final AmtrakLookup lookup;
    private final AmtrakGateway gateway;
    private final Handlebars handlebars;

    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

    @Override
    public Class<AmtrakCommandParameters> getParameterClass() {
        return AmtrakCommandParameters.class;
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder augment(ImmutableApplicationCommandRequest.Builder builder) {
        return builder
                .name("amtrak")
                .description("Interact with the Amtrak API. Also supports VIA and Brightline!");
    }

    @Override
    protected SlashCommandResponse handleEvent(ChatInputInteractionEvent event, AmtrakCommandParameters parameters) {
        return parameters.getLookup().doLookup(this);
    }

    private Template getTemplate(String name) {
        return templateCache.computeIfAbsent(name, n -> {
            try {
                return handlebars.compile("amtrak/" + n);
            } catch (IOException ex) {
                log.error("Unable to compile template {}", name, ex);
                return null;
            }
        });
    }

    public String templateResponse(String name, Object context) {
        try {
            var template = getTemplate(name);
            if (template == null) {
                return "Error generating response, please contact the admin";
            }
            return template.apply(context);
        } catch (IOException e) {
            return "Error generating response, please contact the admin";
        }
    }
}

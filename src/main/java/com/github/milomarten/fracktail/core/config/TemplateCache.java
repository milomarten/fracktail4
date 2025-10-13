package com.github.milomarten.fracktail.core.config;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateCache {
    private final Handlebars handlebars;
    private final Map<String, Template> templates;

    public Template get(String name) {
        return templates.computeIfAbsent(name, n -> {
            try {
                return handlebars.compile(name);
            } catch (IOException ex) {
                log.error("Error loading template {}", name, ex);
                return Template.EMPTY;
            }
        });
    }

    public String apply(String name, Object context) {
        try {
            return get(name).apply(context);
        } catch (IOException ex) {
            log.error("Error applying template {} with {}", name, context, ex);
            return "";
        }
    }

    public DiscordResponse reply(String name, Object context) {
        var template = get(name);
        return event -> {
            try {
                return event.reply(template.apply(context));
            } catch (IOException ex) {
                log.error("Error loading template {}", name, ex);
                return event.reply("Error loading response template " + name);
            }
        };
    }
}

package com.github.milomarten.fracktail5.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.fracktail4.commands.BotDisableCommand;
import com.github.milomarten.fracktail4.commands.CommandBootstrap;
import com.github.milomarten.fracktail4.commands.PuptimeCommand;
import com.github.milomarten.fracktail4.commands.UptimeCommand;
import com.github.milomarten.fracktail4.commands.dice.DiceSlashCommand;
import com.github.milomarten.fracktail4.commands.remind.RemindMeCommand;
import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordJacksonMapper;
import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordParameterHelper;
import com.github.milomarten.fracktail4.platform.discord.mapper.ObjectToDiscordReflectiveMapper;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SimpleCommandAsSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NewCommandsTest {
    @Test
    public void testNewCommandSpecs() {
        compareCommands(
                new UptimeCommand(),
                new com.github.milomarten.fracktail5.commands.UptimeCommand()
        );
        compareCommands(
                new PuptimeCommand(null),
                new com.github.milomarten.fracktail5.commands.PuptimeCommand()
        );
        compareCommands(
                new BotDisableCommand(),
                new LockCommand()
        );
        compareCommands(
                new SimpleCommandAsSlashCommand(new CommandBootstrap().math()),
                new MathCommand()
        );
        compareCommands(
                new DiceSlashCommand(null),
                new com.github.milomarten.fracktail5.commands.DiceSlashCommand(null)
        );
        // Intentionally removed one parameter, `where`, which is now populated automatically instead
        // of a manual entry
//        compareCommands(
//                new RemindMeCommand(null),
//                new com.github.milomarten.fracktail5.commands.RemindMeCommand(null)
//        );
    }

    private void compareCommands(SlashCommandWrapper v4, DiscordSlashCommand v5) {
        if (v4 instanceof AbstractSlashCommand<?> abs) {
            abs.setHelper(makeHelper());
        }

        var v4Request = v4.getRequest();
        var v5Request = v5.getDiscordSlashCommandDetails().getSpec()
                .visit(ApplicationCommandRequest.builder()).build();

        if (!Objects.equals(v4Request, v5Request)) {
            var paramMapV4 = v4Request.options().toOptional()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(a -> a.name(), a -> a));
            var paramMapV5 = v5Request.options().toOptional()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(a -> a.name(), a -> a));

            paramMapsEqualOrGreater(v4Request.name(), paramMapV4, paramMapV5);
        }
    }

    private void paramMapsEqualOrGreater(String cmdName, Map<String, ApplicationCommandOptionData> v4, Map<String, ApplicationCommandOptionData> v5) {
        for (var entry : v4.entrySet()) {
            var correspondingValue = v5.get(entry.getKey());
            assertEquals(entry.getValue(), correspondingValue, cmdName + ":" + entry.getKey());
        }
    }

    private DiscordParameterHelper makeHelper() {
        return new DiscordParameterHelper(
                new DiscordJacksonMapper(
                        new ObjectMapper()
                ),
                new ObjectToDiscordReflectiveMapper(),
                new LocalValidatorFactoryBean()
        );
    }
}
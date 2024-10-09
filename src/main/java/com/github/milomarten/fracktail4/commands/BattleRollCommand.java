package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.commands.dnd.AttackParameters;
import com.github.milomarten.fracktail4.commands.dnd.BattleRoll;
import com.github.milomarten.fracktail4.commands.dnd.DefenseParameters;
import com.github.milomarten.fracktail4.commands.dnd.Type;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public class BattleRollCommand implements SlashCommandWrapper {
    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("battleroll")
                .description("Make a battle roll using hard-coded values")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("damageBase")
                        .description("The DB of the attack")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .minValue(1d).maxValue(28d)
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("attackType")
                        .description("The type of the attack")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .choices(Type.asChoices())
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("attackerPrimaryType")
                        .description("The primary type of the attacker")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .choices(Type.asChoices())
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("attackerSecondaryType")
                        .description("The secondary type of the attacker")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .choices(Type.asChoices())
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("attackStat")
                        .description("The physical or special attack, depending on the type of move")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .minValue(1d)
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("attackCS")
                        .description("The physical or special attack combat stages, depending on the type of move")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .minValue(-6d).maxValue(6d)
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("isCrit")
                        .description("If the attack is a crit")
                        .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("defenderPrimaryType")
                        .description("The primary type of the defender")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .choices(Type.asChoices())
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("defenderSecondaryType")
                        .description("The secondary type of the defender")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .choices(Type.asChoices())
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("defenseStat")
                        .description("The physical or special defense, depending on the type of move")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .minValue(1d)
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("defenseCS")
                        .description("The physical or special defense combat stages, depending on the type of move")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .minValue(-6d).maxValue(6d)
                        .required(false)
                        .build()
                )
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        int db = (int) event.getOption("damageBase").orElseThrow().getValue().orElseThrow().asLong();
        var attackType = event.getOption("attackType")
                .flatMap(acie -> acie.getValue())
                .map(v -> v.asString())
                .flatMap(Type::fromString)
                .orElse(Type.NONE);
        var attackerPrimaryType = event.getOption("attackerPrimaryType")
                .flatMap(acie -> acie.getValue())
                .map(v -> v.asString())
                .flatMap(Type::fromString)
                .orElse(Type.NONE);
        var attackerSecondaryType = event.getOption("attackerSecondaryType")
                .flatMap(acie -> acie.getValue())
                .map(v -> v.asString())
                .flatMap(Type::fromString)
                .orElse(Type.NONE);
        int attackStat = (int) event.getOption("attackStat")
                .orElseThrow()
                .getValue()
                .orElseThrow()
                .asLong();
        int attackCS = event.getOption("attackCS")
                .flatMap(a -> a.getValue())
                .map(a -> a.asLong())
                .orElse(0L)
                .intValue();
        var defenderPrimaryType = event.getOption("defenderPrimaryType")
                .flatMap(acie -> acie.getValue())
                .map(v -> v.asString())
                .flatMap(Type::fromString)
                .orElse(Type.NONE);
        var defenderSecondaryType = event.getOption("defenderSecondaryType")
                .flatMap(acie -> acie.getValue())
                .map(v -> v.asString())
                .flatMap(Type::fromString)
                .orElse(Type.NONE);
        int defenseStat = (int) event.getOption("defenseStat")
                .orElseThrow()
                .getValue()
                .orElseThrow()
                .asLong();
        int defenseCS = event.getOption("defenseCS")
                .flatMap(a -> a.getValue())
                .map(a -> a.asLong())
                .orElse(0L)
                .intValue();
        boolean crit = event.getOption("isCrit")
                .flatMap(a -> a.getValue())
                .map(a -> a.asBoolean())
                .orElse(false);

        var attacker = AttackParameters.builder()
                .damageBase(db)
                .attackType(attackType)
                .attackerPrimaryType(attackerPrimaryType)
                .attackerSecondaryType(attackerSecondaryType)
                .attackStat(attackStat)
                .attackCS(attackCS)
                .criticalHit(crit)
                .build();
        var defender = DefenseParameters.builder()
                .defenderPrimaryType(defenderPrimaryType)
                .defenderSecondaryType(defenderSecondaryType)
                .defenseStat(defenseStat)
                .defenseCS(defenseCS)
                .build();

        var result = BattleRoll.doAttackRoll(attacker, defender);

        return SlashCommands.replyEphemeral(event, result.toString());
    }
}

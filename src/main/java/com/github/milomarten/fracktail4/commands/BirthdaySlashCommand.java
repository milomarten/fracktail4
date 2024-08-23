package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.birthday.BirthdayCritter;
import com.github.milomarten.fracktail4.birthday.BirthdayHandler;
import com.github.milomarten.fracktail4.birthday.BirthdayUtils;
import com.github.milomarten.fracktail4.permissions.discord.DiscordPermissionProvider;
import com.github.milomarten.fracktail4.permissions.discord.DiscordRole;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdaySlashCommand implements SlashCommandWrapper {
    private final BirthdayHandler handler;
    private final DiscordPermissionProvider permissions;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("birthday")
                .description("Get information about your fellow member's birthdays!")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("set")
                        .description("Add your birthday to the birthday calendar")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("month")
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .description("Month of birth")
                                .choices(BirthdayUtils.getMonthOptions())
                                .required(true)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("day")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .description("Day of birth")
                                .minValue(1d)
                                .maxValue(31d)
                                .required(true)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("year")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .description("Year of birth")
                                .minValue(1900d)
                                .maxValue(2011d)
                                .required(false)
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("force-set")
                        .description("Add someone's birthday to the birthday calendar. Mods only!")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("user")
                                .type(ApplicationCommandOption.Type.USER.getValue())
                                .description("User to add the birthday of")
                                .required(true)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("month")
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .description("Month of birth")
                                .choices(BirthdayUtils.getMonthOptions())
                                .required(true)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("day")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .description("Day of birth")
                                .minValue(1d)
                                .maxValue(31d)
                                .required(true)
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("year")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .description("Year of birth")
                                .minValue(1900d)
                                .maxValue(2011d)
                                .required(false)
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("delete")
                        .description("Remove someone's birthday to the birthday calendar. Mods only!")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("user")
                                .type(ApplicationCommandOption.Type.USER.getValue())
                                .description("User to remove the birthday of")
                                .required(true)
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("next")
                        .description("Get the next birthday")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("previous")
                        .description("Get the previously celebrated birthday")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("metadata")
                        .description("Get information about the calendar")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("lookup")
                        .description("Check on another member's birthday")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("user")
                                .description("Lookup user")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("username")
                                        .description("Look up a user's birthday")
                                        .required(false)
                                        .type(ApplicationCommandOption.Type.USER.getValue())
                                        .build())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("month")
                                .description("Lookup by month")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("month")
                                        .description("Look up all birthdays in a month")
                                        .required(false)
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var first = event.getOptions().get(0);
        return switch (first.getName()) {
            case "set" -> set(event, first);
            case "force-set" -> forceSet(event, first);
            case "delete" -> delete(event, first);
            case "next" -> next(event);
            case "previous" -> previous(event);
            case "lookup" -> lookup(event, first);
            case "metadata" -> metadata(event);
            default -> replyEphemeral(event, "Unknown option " + first.getName());
        };
    }

    private Mono<Void> set(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (handler.hasBirthday(event.getInteraction().getUser().getId())) {
            return replyEphemeral(event, "You've already entered your birthday!");
        }

        String month = opt.getOption("month").orElseThrow()
                .getValue().orElseThrow()
                .asString();
        int day = (int) opt.getOption("day").orElseThrow()
                .getValue().orElseThrow()
                .asLong();
        Optional<Year> year = opt.getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(l -> Year.of(l.intValue()));

        MonthDay birthday;
        try {
            birthday = MonthDay.of(BirthdayUtils.parseMonth(month).orElseThrow(), day);
        } catch (DateTimeException ex) {
            return replyEphemeral(event,
                    month + "/" + day + " is not a valid day! Nice try!");
        }

        return event.deferReply()
                .then(handler.createBirthday(event.getInteraction().getUser().getId(), birthday, year.orElse(null))
                .then(followup(event, "Added your birthday to the calendar!")))
                .then();
    }

    private Mono<Void> forceSet(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (isNotMod(event)) {
            return replyEphemeral(event, "Nice try, but only mods can use that command.");
        }

        String month = opt.getOption("month").orElseThrow()
                .getValue().orElseThrow()
                .asString();
        int day = (int) opt.getOption("day").orElseThrow()
                .getValue().orElseThrow()
                .asLong();
        Optional<Year> year = opt.getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(l -> Year.of(l.intValue()));
        Snowflake userId = opt.getOption("user").orElseThrow()
                .getValue().orElseThrow()
                .asSnowflake();

        MonthDay birthday;
        try {
            birthday = MonthDay.of(BirthdayUtils.parseMonth(month).orElseThrow(), day);
        } catch (DateTimeException ex) {
            return replyEphemeral(event,
                    month + "/" + day + " is not a valid day! Nice try!");
        }

        return event.deferReply()
                .then(handler.createBirthday(userId, birthday, year.orElse(null)))
                .then(event.getInteraction().getClient()
                        .getUserById(userId))
                .flatMap(user -> followup(event, "Added " + user.getUsername() + "'s birthday to the calendar!"))
                .then();
    }

    private Mono<Void> delete(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (isNotMod(event)) {
            return replyEphemeral(event, "Nice try, but only mods can use that command.");
        }

        Snowflake userId = opt.getOption("user").orElseThrow()
                .getValue().orElseThrow()
                .asSnowflake();

        return event.deferReply()
                .then(handler.removeBirthday(userId))
                .then(event.getInteraction().getClient()
                        .getUserById(userId))
                .flatMap(user -> followup(event, "Removed " + user.getUsername() + "'s birthday from the calendar!"))
                .then();
    }

    private Mono<?> next(ChatInputInteractionEvent event) {
        var now = LocalDate.now();
        var nextBirthdaysMaybe = handler.getNextBirthdays(now);
        if (nextBirthdaysMaybe.isEmpty()) {
            return replyEphemeral(event, "There are no birthdays in the calendar...");
        }
        var nextBirthdays = nextBirthdaysMaybe.get();
        var nextBirthdayCritters = nextBirthdays.celebrators();
        return event.deferReply()
                .thenMany(Flux.fromIterable(nextBirthdayCritters))
                .flatMap(bc -> elaborate(event, bc))
                .collectList()
                .flatMap(birthdays -> {
                    if (birthdays.isEmpty()) {
                        // Recursively try birthdays and purging non-member birthdays
                        return handler.removeBirthdays(nextBirthdayCritters)
                                .then(next(event));
                    } else {
                        var dateOfBirthdays = birthdays.get(0).getBirthdayAsString();

                        var title = String.format("The next birthdays are on %s (%s):\n",
                                dateOfBirthdays,
                                BirthdayUtils.getDurationWords(LocalDate.now(), nextBirthdays.when()));
                        var reply = birthdays.stream()
                                .map(t -> "\t" + t.getName())
                                .collect(Collectors.joining("\n", title, ""));

                        return followup(event, reply);
                    }
                });
    }

    private Mono<?> previous(ChatInputInteractionEvent event) {
        var now = LocalDate.now();
        var previousBirthdaysMaybe = handler.getPreviousBirthdays(now);
        if (previousBirthdaysMaybe.isEmpty()) {
            return replyEphemeral(event, "There are no birthdays in the calendar...");
        }
        var previousBirthdays = previousBirthdaysMaybe.get();
        var previousBirthdayCritters = previousBirthdays.celebrators();
        return event.deferReply()
                .thenMany(Flux.fromIterable(previousBirthdayCritters))
                .flatMap(bc -> elaborate(event, bc))
                .collectList()
                .flatMap(birthdays -> {
                    if (birthdays.isEmpty()) {
                        // Recursively try birthdays and purging non-member birthdays
                        return handler.removeBirthdays(previousBirthdayCritters)
                                .then(previous(event));
                    } else {
                        var dateOfBirthdays = birthdays.get(0).getBirthdayAsString();

                        var title = String.format("The previous birthdays were on %s (%s):\n",
                                dateOfBirthdays,
                                BirthdayUtils.getDurationWords(LocalDate.now(), previousBirthdays.when()));
                        var reply = birthdays.stream()
                                .map(t -> "\t" + t.getName())
                                .collect(Collectors.joining("\n", title, ""));

                        return followup(event, reply);
                    }
                });
    }

    private Mono<?> lookup(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        var second = opt.getOptions().get(0);
        return switch (second.getName()) {
            case "user" -> lookupUser(event, second);
            case "month" -> lookupMonth(event, second);
            default -> replyEphemeral(event, "Unknown option " + second.getName());
        };
    }

    private Mono<?> lookupUser(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        var userId = opt.getOption("username")
                .flatMap(a -> a.getValue())
                .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                .orElseGet(() -> event.getInteraction().getUser().getId());
        var userMono = opt.getOption("username")
                .flatMap(a -> a.getValue())
                .map(ApplicationCommandInteractionOptionValue::asUser)
                .orElseGet(() -> Mono.just(event.getInteraction().getUser()));
        var birthdayMaybe = handler.getBirthday(userId);

        return event.deferReply()
                .then(Mono.justOrEmpty(birthdayMaybe))
                .flatMap(bc -> elaborate(event, bc))
                .flatMap(e -> followup(event, e.getName() + "'s birthday is on " + e.getBirthdayAsString()).thenReturn(true))
                .switchIfEmpty(userMono
                        .flatMap(user -> {
                            return Mono.justOrEmpty(event.getInteraction().getGuildId())
                                    .flatMap(user::asMember)
                                    .map(BirthdayUtils::getName)
                                    .switchIfEmpty(Mono.fromSupplier(user::getUsername))
                                    .flatMap(name -> followup(event, name + "'s birthday is not on the calendar!"));
                        }).thenReturn(false)
                );
    }

    private Mono<?> lookupMonth(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        var monthRaw = opt.getOption("month").orElseThrow()
                .getValue()
                .map(ApplicationCommandInteractionOptionValue::asString);
        var monthMaybe = monthRaw
                .map(BirthdayUtils::parseMonth)
                .orElseGet(() -> Optional.of(MonthDay.now().getMonth()));
        if (monthMaybe.isEmpty()) {
            return replyEphemeral(event,
                    "I don't understand the month you've given. Format should be the full month name, the month number with no leading 0s, or the first three letters of the month.");
        }
        var searchMonth = monthMaybe.get();
        var birthdaysInMonth = handler.getBirthdaysOn(searchMonth);
        if (birthdaysInMonth.isEmpty()) {
            return replyEphemeral(event, "There are no birthdays in the calendar for " + BirthdayUtils.getDisplayMonth(searchMonth) + "...");
        }

        return event.deferReply()
                .thenMany(Flux.fromIterable(birthdaysInMonth))
                .flatMap(bc -> elaborate(event, bc))
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return followup(event, "There are no birthdays in the calendar for " + BirthdayUtils.getDisplayMonth(searchMonth) + "...");
                    } else {
                        var reply = list.stream()
                                .sorted(Comparator.comparing(t -> t.celebrator().getDay()))
                                .map(bi -> "\t" + bi.getName() + " - " + bi.getBirthdayAsString())
                                .collect(Collectors.joining("\n", "The birthdays for " + BirthdayUtils.getDisplayMonth(searchMonth) + " are:\n", ""));

                        return followup(event, reply);
                    }
                });
    }

    private Mono<?> metadata(ChatInputInteractionEvent event) {
        var response = String.format("There are %d birthdays in the calendar. The dates are: %s",
                handler.getNumberOfBirthdays(),
                handler.getBirthdays()
                        .stream()
                        .map(BirthdayCritter::getDay)
                        .map(BirthdayUtils::getDisplayBirthday)
                        .collect(Collectors.joining(", "))
                );
        return replyEphemeral(event, response);
    }

    private Mono<BirthdayInstance> elaborate(ChatInputInteractionEvent event, BirthdayCritter critter) {
        return Mono.defer(() -> event.getClient().getMemberById(
                event.getInteraction().getGuildId().orElseThrow(),
                critter.getCritter()
                ))
                        .zipWith(Mono.just(critter), (member, bc) -> new BirthdayInstance(bc, member))
                        .onErrorResume(ex -> {
                            log.error("Error getting member {}. Suppressing...", critter.getCritter(), ex);
                            return Mono.empty();
                        });
    }

    private boolean isNotMod(ChatInputInteractionEvent event) {
        return permissions.getPermissionsForUser(event.getInteraction().getUser())
                .doesNotHaveRole(DiscordRole.MOD);
    }
}

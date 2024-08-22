package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.birthday.BirthdayCritter;
import com.github.milomarten.fracktail4.birthday.BirthdayHandler;
import com.github.milomarten.fracktail4.birthday.BirthdayUtils;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands.followupEphemeral;
import static com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands.replyEphemeral;

@Component
@RequiredArgsConstructor
public class BirthdaySlashCommand implements SlashCommandWrapper {
    private final BirthdayHandler handler;

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
                        .name("next")
                        .description("Get the next birthday")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("previous")
                        .description("Get the previously celebrated birthday")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
//                .addOption(ApplicationCommandOptionData.builder()
//                        .name("metadata")
//                        .description("Get information about the calendar")
//                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
//                        .build())
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
            case "next" -> next(event);
            case "previous" -> previous(event);
            case "lookup" -> lookup(event, first);
            default -> replyEphemeral(event, "Unknown option " + first.getName());
        };
    }

    private Mono<Void> set(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (handler.hasBirthday(event.getInteraction().getUser().getId())) {
            return replyEphemeral(event, "You've already entered your birthday!");
        }

        int month = (int) opt.getOption("month").orElseThrow()
                .getValue().orElseThrow()
                .asLong();
        int day = (int) opt.getOption("day").orElseThrow()
                .getValue().orElseThrow()
                .asLong();
        Optional<Year> year = opt.getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(l -> Year.of(l.intValue()));

        MonthDay birthday;
        try {
            birthday = MonthDay.of(month, day);
        } catch (DateTimeException ex) {
            return replyEphemeral(event,
                    month + "/" + day + " is not a valid day! Nice try!");
        }

        return event.deferReply()
                .then(handler.createBirthday(event.getInteraction().getUser().getId(), birthday, year.orElse(null))
                    .flatMap(i -> followupEphemeral(event, "Added your birthday to the calendar!")))
                .then();
    }

    private Mono<?> next(ChatInputInteractionEvent event) {
        var now = LocalDate.now();
        var nextBirthdaysMaybe = handler.getNextBirthdays(now);
        if (nextBirthdaysMaybe.isEmpty()) {
            return replyEphemeral(event, "There are no birthdays in the calendar...");
        }
        var nextBirthdays = nextBirthdaysMaybe.get();
        var nextBirthdayCritters = nextBirthdays.critter();
        return event.deferReply()
                .thenMany(Flux.fromIterable(nextBirthdayCritters))
                .flatMap(bc -> elaborate(event, bc))
                .collectList()
                .flatMap(birthdays -> {
                    if (birthdays.isEmpty()) {
                        // Recursively try birthdays and purging non-member birthdays
                        return handler.removeBirthdays(nextBirthdayCritters)
                                .then(next(event));
                    } else if (birthdays.size() == 1) {
                        var birthday = birthdays.get(0);
                        return followupEphemeral(event, "The next birthday is " + birthday.getName() + ", on " + birthday.getBirthdayAsString() + "!");
                    } else {
                        var reply = birthdays.stream()
                                .map(t -> "\t" + t.getName() + " - " + t.getBirthdayAsString())
                                .collect(Collectors.joining("\n", "Here are the next birthdays:\n", ""));

                        return followupEphemeral(event, reply);
                    }
                });
    }

    private Mono<?> previous(ChatInputInteractionEvent event) {
        var now = LocalDate.now();
        var previousBirthdaysMaybe = handler.getNextBirthdays(now);
        if (previousBirthdaysMaybe.isEmpty()) {
            return replyEphemeral(event, "There are no birthdays in the calendar...");
        }
        var previousBirthdays = previousBirthdaysMaybe.get();
        var previousBirthdayCritters = previousBirthdays.critter();
        return event.deferReply()
                .thenMany(Flux.fromIterable(previousBirthdayCritters))
                .flatMap(bc -> elaborate(event, bc))
                .collectList()
                .flatMap(birthdays -> {
                    if (birthdays.isEmpty()) {
                        // Recursively try birthdays and purging non-member birthdays
                        return handler.removeBirthdays(previousBirthdayCritters)
                                .then(previous(event));
                    } else if (birthdays.size() == 1) {
                        var birthday = birthdays.get(0);
                        return followupEphemeral(event, "The previous birthday was " + birthday.getName() + ", on " + birthday.getBirthdayAsString() + "!");
                    } else {
                        var reply = birthdays.stream()
                                .map(t -> "\t" + t.getName() + " - " + t.getBirthdayAsString())
                                .collect(Collectors.joining("\n", "Here are the previous birthdays:\n", ""));

                        return followupEphemeral(event, reply);
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

        return event.deferReply().withEphemeral(true)
                .then(Mono.justOrEmpty(birthdayMaybe))
                .flatMap(bc -> elaborate(event, bc))
                .flatMap(e -> followupEphemeral(event, e.getName() + "'s birthday is on " + e.getBirthdayAsString()).thenReturn(true))
                .switchIfEmpty(userMono
                        .flatMap(user -> {
                            return Mono.justOrEmpty(event.getInteraction().getGuildId())
                                    .flatMap(user::asMember)
                                    .map(BirthdaySlashCommand::getName)
                                    .switchIfEmpty(Mono.fromSupplier(user::getUsername))
                                    .flatMap(name -> followupEphemeral(event, name + "'s birthday is not on the calendar!"));
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

        return event.deferReply().withEphemeral(true)
                .thenMany(Flux.fromIterable(birthdaysInMonth))
                .flatMap(bc -> elaborate(event, bc))
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return followupEphemeral(event, "There are no birthdays in the calendar for " + BirthdayUtils.getDisplayMonth(searchMonth) + "...");
                    } else {
                        var reply = list.stream()
                                .sorted(Comparator.comparing(t -> t.celebrator.getDay()))
                                .map(bi -> "\t" + bi.getName() + " - " + bi.getBirthdayAsString())
                                .collect(Collectors.joining("\n", "The birthdays for " + BirthdayUtils.getDisplayMonth(searchMonth) + " are:\n", ""));

                        return followupEphemeral(event, reply);
                    }
                });
    }

    private Mono<BirthdayInstance> elaborate(ChatInputInteractionEvent event, BirthdayCritter critter) {
        return Mono.fromCallable(() -> event.getInteraction().getMember().orElseThrow())
                        .zipWith(Mono.just(critter), (member, bc) -> new BirthdayInstance(bc, member))
                        .onErrorContinue((ex, obj) -> {});
    }

    private static String getName(Member member) {
        String username = member.getUsername();
        return member.getNickname()
                .map(n -> n + " (AKA " + username + ")")
                .orElse(username);
    }

    private record BirthdayInstance(BirthdayCritter celebrator, Member member) {
        public String getBirthdayAsString() {
            return String.format("%s %02d",
                    BirthdayUtils.getDisplayMonth(celebrator.getDay().getMonth()),
                    celebrator.getDay().getDayOfMonth());
        }

        public String getName() {
            return BirthdaySlashCommand.getName(this.member);
        }

        public OptionalInt getAge(Year now) {
            int age = celebrator.getYear()
                    .map(birthYear -> now.getValue() - birthYear.getValue())
                    .orElse(-1);
            return age < 0 ? OptionalInt.empty() : OptionalInt.of(age);
        }
    }
}

package com.github.milomarten.fracktail4.platform.discord.annotations;

import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import org.springframework.core.convert.ConversionService;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnotatedSlashCommandWrapper implements SlashCommandWrapper {
    private final ApplicationCommandRequest request;
    private final List<Function<ChatInputInteractionEvent, ?>> parameterMapper;
    private final Object bean;
    private final Method method;

    public AnnotatedSlashCommandWrapper(SlashCommand annotation, Object bean, Method method, ConversionService conversionService) {
        String name = fillInStar(annotation.name(), method.getName());
        this.parameterMapper = new ArrayList<>();
        this.bean = bean;
        this.method = method;
        var choices = Arrays.stream(method.getParameters())
                .map(p -> compileParameter(p, conversionService))
                .toList();
        this.request = ApplicationCommandRequest.builder()
                .name(name)
                .description(fillInStar(annotation.description(), name))
                .nameLocalizationsOrNull(getLocalizations(annotation.nameLocalizations()))
                .descriptionLocalizationsOrNull(getLocalizations(annotation.descriptionLocalizations()))
                .options(choices.isEmpty() ? Possible.absent() : Possible.of(choices))
                .build();
    }

    private static String fillInStar(String in, String fallback) {
        return "*".equals(in) ? fallback : in;
    }

    private static Map<String, String> getLocalizations(LocalizedString[] opts) {
        if (opts.length == 0) {
            return null;
        }
        return Arrays.stream(opts)
                .collect(Collectors.toMap(LocalizedString::locale, LocalizedString::value));
    }

    private ApplicationCommandOptionData compileParameter(Parameter parameter, ConversionService conversionService) {
        if (parameter.isAnnotationPresent(StringParameter.class)) {
            StringParameter sp = parameter.getAnnotation(StringParameter.class);
            if (!conversionService.canConvert(String.class, parameter.getType())) {
                throw new IllegalArgumentException("Cannot convert String parameter " + parameter.getName() + "into type " + parameter.getType().getName());
            }
            var name = fillInStar(sp.name(), parameter.getName());

            this.parameterMapper.add((event) -> {
                return event.getOption(name)
                        .flatMap(opt -> opt.getValue())
                        .map(val -> val.asString())
                        .orElse(sp.fallback());
            });

            var choices = Arrays.stream(sp.choices())
                    .map(l -> (ApplicationCommandOptionChoiceData) ApplicationCommandOptionChoiceData.builder()
                            .name(fillInStar(l.name(), l.value()))
                            .nameLocalizationsOrNull(getLocalizations(l.nameLocalizations()))
                            .value(l.value())
                            .build()
                    ).toList();
            return ApplicationCommandOptionData.builder()
                    .name(name)
                    .description(fillInStar(sp.description(), name))
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .nameLocalizationsOrNull(getLocalizations(sp.nameLocalizations()))
                    .descriptionLocalizationsOrNull(getLocalizations(sp.descriptionLocalizations()))
                    .autocomplete(sp.autocomplete())
                    .required(sp.required())
                    .minLength(sp.minLength() < 0 ? Possible.absent() : Possible.of(sp.minLength()))
                    .maxLength(sp.maxLength() < 0 ? Possible.absent() : Possible.of(sp.maxLength()))
                    .choices(choices.isEmpty() ? Possible.absent() : Possible.of(choices))
                    .build();
        }
        throw new IllegalArgumentException("Can only handle parameters annotated with @StringParameter");
    }

    @Override
    public ApplicationCommandRequest getRequest() {
        return this.request;
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        Object[] params = this.parameterMapper.stream()
                .map(func -> func.apply(event))
                .toArray();
        return Mono.fromCallable(() -> method.invoke(this.bean, params))
                .cast(String.class)
                .flatMap(obj -> SlashCommands.replyEphemeral(event, obj));
    }
}

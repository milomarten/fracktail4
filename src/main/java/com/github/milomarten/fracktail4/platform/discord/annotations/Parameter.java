package com.github.milomarten.fracktail4.platform.discord.annotations;

import discord4j.core.object.command.ApplicationCommandOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Parameter {
    String DEFAULT = "*";

    String name() default DEFAULT;

    LocalizedString[] nameLocalizations() default {};

    String description();

    LocalizedString[] descriptionLocalizations() default {};

    boolean required() default true;

    ApplicationCommandOption.Type type() default ApplicationCommandOption.Type.UNKNOWN;
}

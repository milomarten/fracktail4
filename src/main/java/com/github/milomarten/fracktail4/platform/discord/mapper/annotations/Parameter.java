package com.github.milomarten.fracktail4.platform.discord.mapper.annotations;

import discord4j.core.object.command.ApplicationCommandOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    String description();
    ApplicationCommandOption.Type type() default ApplicationCommandOption.Type.UNKNOWN;
}

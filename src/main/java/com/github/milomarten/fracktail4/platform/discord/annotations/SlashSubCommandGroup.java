package com.github.milomarten.fracktail4.platform.discord.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashSubCommandGroup {
    String name() default "*";
    LocalizedString[] nameLocalizations() default {};
    String description() default "*";
    LocalizedString[] descriptionLocalizations() default {};

    SlashSubCommand[] subcommands();
}

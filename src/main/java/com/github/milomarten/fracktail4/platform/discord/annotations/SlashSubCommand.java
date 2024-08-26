package com.github.milomarten.fracktail4.platform.discord.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashSubCommand {
    String name() default "*";
    LocalizedString[] nameLocalizations() default {};
    String description() default "*";
    LocalizedString[] descriptionLocalizations() default {};

    String[] subcommands();
}

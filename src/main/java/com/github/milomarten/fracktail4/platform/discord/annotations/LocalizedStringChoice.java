package com.github.milomarten.fracktail4.platform.discord.annotations;

public @interface LocalizedStringChoice {
    String name() default "*";
    LocalizedString[] nameLocalizations() default {};
    String value();
}

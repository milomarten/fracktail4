package com.github.milomarten.fracktail4.platform.discord.annotations;

public @interface LocalizedIntegerChoice {
    String name() default "*";
    LocalizedString[] nameLocalizations() default {};
    int value();
}

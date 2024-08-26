package com.github.milomarten.fracktail4.platform.discord.annotations;

public @interface LocalizedDoubleChoice {
    String name() default "*";
    LocalizedString[] nameLocalizations() default {};
    double value();
}

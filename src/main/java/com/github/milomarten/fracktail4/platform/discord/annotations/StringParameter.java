package com.github.milomarten.fracktail4.platform.discord.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface StringParameter {
    String DEFAULT = "*";

    String name() default DEFAULT;

    LocalizedString[] nameLocalizations() default {};

    String description() default DEFAULT;

    LocalizedString[] descriptionLocalizations() default {};

    boolean required() default true;

    String fallback() default "";

    LocalizedStringChoice[] choices() default {};

    int minLength() default -1;

    int maxLength() default -1;

    boolean autocomplete() default false;
}

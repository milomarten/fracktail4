package com.github.milomarten.fracktail4.platform.discord.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface IntegerParameter {
    String DEFAULT = "*";

    String name() default DEFAULT;

    LocalizedString[] nameLocalizations() default {};

    String description() default DEFAULT;

    LocalizedString[] descriptionLocalizations() default {};

    boolean required() default true;

    int fallback() default -1;

    LocalizedIntegerChoice[] choices() default {};

    int minValue() default Integer.MIN_VALUE;

    int maxValue() default Integer.MAX_VALUE;

    boolean autocomplete() default false;
}

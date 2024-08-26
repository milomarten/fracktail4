package com.github.milomarten.fracktail4.platform.discord.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DoubleParameter {
    String DEFAULT = "*";

    String name() default DEFAULT;

    LocalizedString[] nameLocalizations() default {};

    String description() default DEFAULT;

    LocalizedString[] descriptionLocalizations() default {};

    boolean required() default true;

    double fallback() default -1;

    LocalizedDoubleChoice[] choices() default {};

    double minValue() default Double.MIN_VALUE;

    double maxValue() default Double.MAX_VALUE;

    boolean autocomplete() default false;
}

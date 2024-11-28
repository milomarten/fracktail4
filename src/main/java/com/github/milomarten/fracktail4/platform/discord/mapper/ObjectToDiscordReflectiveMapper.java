package com.github.milomarten.fracktail4.platform.discord.mapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import discord4j.common.util.Snowflake;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Component
public class ObjectToDiscordReflectiveMapper {
    public <T> List<ApplicationCommandOptionData> toParams(Class<T> clazz) {
        var params = new ArrayList<ApplicationCommandOptionData>();

        if (clazz == Void.class || clazz == Void.TYPE) {
            // Special case to allow parameterless commands to use this abstraction layer.
            return params;
        }

        // Support polymorphism!
        if (clazz.isAnnotationPresent(JsonSubTypes.class)) {
            var subTypes = clazz.getAnnotation(JsonSubTypes.class);
            for (var subType : subTypes.value()) {
                var concreteType = subType.value();
                if (concreteType.isAnnotationPresent(Parameter.class)) {
                    var parameter = concreteType.getAnnotation(Parameter.class);
                    var description = parameter.description();
                    if (description.isBlank()) {
                        throw new IllegalArgumentException("Description is required for a parameter.");
                    }

                    params.add(ApplicationCommandOptionData.builder()
                            .name(subType.name())
                            .description(description)
                            .type(parameter.type().getValue())
                            .options(toParams(concreteType))
                            .build());
                }
            }
        }

        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            if (field.isAnnotationPresent(Parameter.class)) {
                var param = field.getAnnotation(Parameter.class);
                var description = param.description();
                if (description.isBlank()) {
                    throw new IllegalArgumentException("Description is required for a parameter.");
                }

                if (param.type() == ApplicationCommandOption.Type.SUB_COMMAND || param.type() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP) {
                    params.add(ApplicationCommandOptionData.builder()
                            .name(field.getName())
                            .description(description)
                            .type(param.type().getValue())
                            .options(toParams(field.getType()))
                            .build()
                    );
                } else {
                    params.add(ApplicationCommandOptionData.builder()
                            .name(field.getName())
                            .description(description)
                            .type(getTypeForField(param, field.getType()).getValue())
                            .required(isRequired(field))
                            .minLength(getMinLength(field))
                            .maxLength(getMaxLength(field))
                            .minValue(getMinValue(field))
                            .maxValue(getMaxValue(field))
                            .build()
                    );
                }
            }
        }
        if (params.isEmpty()) {
            log.warn("Class {} has no annotated fields. Did you forget to annotate with @Parameter", clazz.getCanonicalName());
        }

        return params;
    }

    private ApplicationCommandOption.Type getTypeForField(Parameter pAnnot, Class<?> clazz) {
        if (pAnnot.type() != ApplicationCommandOption.Type.UNKNOWN) {
            return pAnnot.type();
        }

        if (clazz.isAssignableFrom(String.class)) {
            return ApplicationCommandOption.Type.STRING;
        } else if (clazz.isAssignableFrom(Integer.class) || clazz.equals(Integer.TYPE) || clazz.equals(BigInteger.class)) {
            return ApplicationCommandOption.Type.INTEGER;
        } else if (clazz.isAssignableFrom(Double.class) || clazz.equals(Double.TYPE) || clazz.equals(BigDecimal.class)) {
            return ApplicationCommandOption.Type.NUMBER;
        } else if (clazz.isAssignableFrom(Boolean.class) || clazz.equals(Boolean.TYPE)) {
            return ApplicationCommandOption.Type.BOOLEAN;
        } else if (clazz.isAssignableFrom(Snowflake.class)) {
            throw new DiscordMapperException("Field type Snowflake is not enough. Please specify directly the Discord Type");
        } else {
            throw new DiscordMapperException("Illegal type " + clazz.getCanonicalName() + ".");
        }
    }
    private boolean isRequired(Field field) {
        var clazz = field.getType();
        if (ClassUtils.isPrimitiveWrapper(clazz)) {
            return false;
        } else if (clazz.equals(Optional.class) || clazz.equals(OptionalInt.class) || clazz.equals(OptionalDouble.class)) {
            return false;
        } else {
            return field.isAnnotationPresent(NotNull.class);
        }
    }

    private Possible<Integer> getMinLength(Field f) {
        if (f.isAnnotationPresent(Size.class)) {
            return Possible.of(f.getAnnotation(Size.class).min());
        }
        return Possible.absent();
    }

    private Possible<Integer> getMaxLength(Field f) {
        if (f.isAnnotationPresent(Size.class)) {
            return Possible.of(f.getAnnotation(Size.class).max());
        }
        return Possible.absent();
    }

    private Possible<Double> getMinValue(Field f) {
        if (f.isAnnotationPresent(Min.class)) {
            return Possible.of((double) f.getAnnotation(Min.class).value());
        }
        if (f.isAnnotationPresent(DecimalMin.class)) {
            var asStr = f.getAnnotation(DecimalMin.class).value();
            return Possible.of(new BigDecimal(asStr).doubleValue());
        }
        if (f.isAnnotationPresent(Positive.class)) {
            return Possible.of(1d);
        }
        if (f.isAnnotationPresent(PositiveOrZero.class)) {
            return Possible.of(0d);
        }
        return Possible.absent();
    }

    private Possible<Double> getMaxValue(Field f) {
        if (f.isAnnotationPresent(Max.class)) {
            return Possible.of((double) f.getAnnotation(Max.class).value());
        }
        if (f.isAnnotationPresent(DecimalMax.class)) {
            var asStr = f.getAnnotation(DecimalMax.class).value();
            return Possible.of(new BigDecimal(asStr).doubleValue());
        }
        if (f.isAnnotationPresent(Negative.class)) {
            return Possible.of(-1d);
        }
        if (f.isAnnotationPresent(NegativeOrZero.class)) {
            return Possible.of(0d);
        }
        return Possible.absent();
    }
}

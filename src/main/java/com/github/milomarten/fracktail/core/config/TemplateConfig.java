package com.github.milomarten.fracktail.core.config;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.milomarten.fracktail.core.discord.DiscordTimestampFormat;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@Configuration
public class TemplateConfig {
    @Bean
    public Handlebars handlebars() {
        TemplateLoader tl = new ClassPathTemplateLoader("/templates", ".hbs");
        return new Handlebars(tl)
                .with(EscapingStrategy.NOOP)
                .registerHelper("capitalize", StringHelpers.capitalize)
                .registerHelper("bearing", new Helper<String>() {
                    @Override
                    public Object apply(String s, Options options) throws IOException {
                        return switch (s) {
                            case "N" -> "north";
                            case "S" -> "south";
                            case "E" -> "east";
                            case "W" -> "west";
                            case "NE" -> "northeast";
                            case "NW" -> "northwest";
                            case "SE" -> "southeast";
                            case "SW" -> "southwest";
                            default -> s;
                        };
                    }
                })
                .registerHelper("timestamp", new Helper<Object>() {
                    @Override
                    public Object apply(Object o, Options options) throws IOException {
                        if (o instanceof TemporalAccessor ta) {
                            String rawFormat = options.hash("format", "LONG_DATE_SHORT_TIME");
                            DiscordTimestampFormat format = EnumUtils.getEnum(DiscordTimestampFormat.class, rawFormat, DiscordTimestampFormat.LONG_DATE_SHORT_TIME);

                            return format.toDiscord(ta);
                        }
                        return "";
                    }
                })
                .registerHelper("amtrakDateTime", new Helper<Object>() {
                    private static final DateTimeFormatter MINI_FORMATTER = DateTimeFormatter.ofPattern("'at' hh:mm a");
                    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("'on' MMM dd 'at' hh:mm a");

                    @Override
                    public Object apply(Object o, Options options) throws IOException {
                        if (o instanceof TemporalAccessor t) {
                            var now = MonthDay.now(ZoneOffset.from(t));
                            if (now.equals(MonthDay.from(t))) {
                                return MINI_FORMATTER.format(t);
                            } else {
                                return FULL_FORMATTER.format(t);
                            }
                        }
                        return "";
                    }
                })
                .registerHelper("amtrakDuration", new Helper<Object>() {
                    @Override
                    public Object apply(Object o, Options options) throws IOException {
                        if (o instanceof Duration d) {
                            if (d.isZero()) {
                                return "on schedule";
                            } else if (d.isNegative()) {
                                d = d.negated();
                                return "%d minutes early".formatted(d.getSeconds() / 60);
                            } else {
                                return "%d minutes late".formatted(d.getSeconds() / 60);
                            }
                        }
                        return "";
                    }
                })
                .registerHelper("amtrakSpeed", new Helper<Object>() {
                    @Override
                    public Object apply(Object o, Options options) throws IOException {
                        if (o instanceof Number num) {
                            return num.intValue() + " mph";
                        }
                        return "";
                    }
                })
                ;
    }
}

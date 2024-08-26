package com.github.milomarten.fracktail4.platform.discord.annotations;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class TestCommand {
    private final ConversionService conversionService;

    @SlashCommand
    public String algebra(@StringParameter String expression) {
        return "The answer is 3x+1.";
    }

    @PostConstruct
    private void tryAndMakeOne() throws Exception {
        var method = this.getClass().getMethod("math", String.class);

        var cmd = new AnnotatedSlashCommandWrapper(
                method.getAnnotation(SlashCommand.class),
                this, method, conversionService
                );
        System.out.println(cmd.getRequest());
    }
}

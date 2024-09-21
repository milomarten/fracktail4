package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.SimpleCommand;
import com.github.milomarten.fracktail4.base.TranslatedSimpleCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandBootstrap {
    @Bean
    public TranslatedSimpleCommand math() {
        return new TranslatedSimpleCommand("math",
                "Perform spectacular feats of math!",
                "command.math.result");
    }
}

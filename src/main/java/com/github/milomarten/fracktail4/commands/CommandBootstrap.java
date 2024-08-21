package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.SimpleCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandBootstrap {
    @Bean
    public SimpleCommand math() {
        return new SimpleCommand("math",
                "Perform spectacular feats of math!",
                "The answer is three.");
    }
}

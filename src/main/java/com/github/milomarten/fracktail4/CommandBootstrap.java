package com.github.milomarten.fracktail4;

import com.github.milomarten.fracktail4.base.SimpleCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandBootstrap {
    @Bean
    public SimpleCommand ping() {
        return new SimpleCommand("math", "The answer is three.");
    }
}

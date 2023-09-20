package com.github.milomarten.fracktail4;

import com.github.milomarten.fracktail4.base.SimplePrefixedCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandBootstrap {
    @Bean
    public SimplePrefixedCommand ping() {
        return new SimplePrefixedCommand("math", "The answer is three.");
    }
}

package com.github.milomarten.fracktail4.base.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommandFilterConfiguration {
    @Bean
    public CommandFilterChain commandFilterChain(List<CommandFilter> filters) {
        return new CommandFilterChain(filters);
    }
}

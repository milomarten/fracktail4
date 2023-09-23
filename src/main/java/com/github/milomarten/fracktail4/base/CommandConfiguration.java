package com.github.milomarten.fracktail4.base;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "command")
@Data
public class CommandConfiguration {
    private String prefix = "!";
    private String delimiter = " ";
}

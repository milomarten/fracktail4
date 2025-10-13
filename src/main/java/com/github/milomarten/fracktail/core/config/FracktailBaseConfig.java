package com.github.milomarten.fracktail.core.config;

import com.github.milomarten.fracktail.core.FracktailImplementation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FracktailBaseConfig {
    public FracktailImplementation getFracktailImplementation(@Value("${app.version}") String raw) {
        log.info("Booting up Fracktail version {}", raw);
        return FracktailImplementation.get(raw);
    }
}

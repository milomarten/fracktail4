package com.github.milomarten.fracktail4.config;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateConfig {
    @Bean
    public Handlebars handlebars() {
        TemplateLoader tl = new ClassPathTemplateLoader("/templates", ".hbs");
        return new Handlebars(tl);
    }
}

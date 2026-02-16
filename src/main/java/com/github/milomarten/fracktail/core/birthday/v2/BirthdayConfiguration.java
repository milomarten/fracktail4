package com.github.milomarten.fracktail.core.birthday.v2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "birthdays")
@Data
public class BirthdayConfiguration {
    private List<HardCodedBirthdayEventInstance> hardCoded;
}

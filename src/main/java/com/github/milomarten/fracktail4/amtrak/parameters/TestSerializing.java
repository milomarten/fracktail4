package com.github.milomarten.fracktail4.amtrak.parameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordParameterHelper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestSerializing {
    private final DiscordParameterHelper helper;

    @PostConstruct
    public void test() throws JsonProcessingException {
        var spec = helper.toParameterSpec(AmtrakCommandParameters.class);
        System.out.println(spec);
    }
}

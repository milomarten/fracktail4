package com.github.milomarten.fracktail5.platform.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
public class TextCommandContext {
    private final Instant timestamp;
    private final String channel;
    private final String username;
    private final String command;
    private final String[] parameters;
}

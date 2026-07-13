package com.github.milomarten.fracktail5.platform.text;

public interface TextChannel<T extends TextCommandContext> {
    TextCommandResponse<T> handle(T context);
}

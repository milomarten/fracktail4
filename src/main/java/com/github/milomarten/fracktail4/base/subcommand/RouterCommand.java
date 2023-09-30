package com.github.milomarten.fracktail4.base.subcommand;

import com.github.milomarten.fracktail4.base.Command;
import com.github.milomarten.fracktail4.base.CommandData;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public abstract class RouterCommand<CONTEXT> implements Command {
    private final String id;
    private final Set<String> aliases = new HashSet<>();
    private final Router<CONTEXT> router;

    @Override
    public CommandData getCommandData() {
        return null;
    }
}

package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.hook.GatewayVisitor;

public interface Command extends GatewayVisitor {
    CommandData getCommandData();
}

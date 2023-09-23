package com.github.milomarten.fracktail4.base.parameter;

import com.github.milomarten.fracktail4.base.CommandConfiguration;
import com.github.milomarten.fracktail4.base.Parameters;

public interface ParameterParser {
    Parameters parse(CommandConfiguration configuration, String contents);
}

package com.github.milomarten.fracktail4.commands.amtrak.parameters;

import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import discord4j.core.object.command.ApplicationCommandOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmtrakCommandParameters {
    @Parameter(description = "Lookup Amtrak or VIA info", type = ApplicationCommandOption.Type.SUB_COMMAND_GROUP)
    private AmtrakLookup lookup;
}

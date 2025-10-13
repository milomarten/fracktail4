package com.github.milomarten.fracktail4.commands.remind;

import com.github.milomarten.fracktail.core.remind.PrettyDuration;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import discord4j.common.util.Snowflake;
import discord4j.core.object.command.ApplicationCommandOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemindMeParameters {
    @Parameter(description = "When the reminder should fire. ex: 1h30m", type = ApplicationCommandOption.Type.STRING)
    @NotNull
    @PrettyDuration
    private String when;
    @Parameter(description = "What the reminder should say")
    @NotNull
    @NotBlank(message = "Reminder message cannot be blank")
    private String message;
    @Parameter(description = "Where the reminder should be. Default: DM", type = ApplicationCommandOption.Type.CHANNEL)
    private Snowflake where;
}

package com.github.milomarten.fracktail4.commands.matchup;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.object.command.ApplicationCommandOption;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonTypeName("scar")
@Parameter(description = "Lookup matchups for the SCAR system", type = ApplicationCommandOption.Type.SUB_COMMAND)
public class SCARMatchupParams implements MatchupParams {
    @Parameter(description = "The attack's type")
    @NotNull
    private SCARType attackingtype;
    @Parameter(description = "The defender's primary type")
    @NotNull
    private SCARType defendingtypeone;

    @Parameter(description = "The defender's secondary type")
    private SCARType defendingtypetwo;

    @Override
    public SlashCommandResponse visit(MatchupCommand matchupCommand) {
        var result = SCARType.getMATCHUPS()
                .getMatchup(attackingtype, defendingtypeone, defendingtypetwo);
        String defenderPretty = (defendingtypetwo == null || defendingtypetwo == defendingtypeone) ?
                defendingtypeone.toString() :
                defendingtypeone.toString() + "/" + defendingtypetwo.toString();

        if (result.isEmpty()) {
            return Responses.reply("A %s type attack would be totally ineffective on a %s type".formatted(attackingtype, defenderPretty));
        } else {
            var realResult = result.getAsInt();
            if (realResult == 0) {
                return Responses.reply("A %s type attack would be normal effectiveness to a %s type".formatted(attackingtype, defenderPretty));
            } else if (realResult < 0) {
                return Responses.reply("A %s type attack would be %dx ineffective to a %s type".formatted(attackingtype, -realResult, defenderPretty));
            } else {
                return Responses.reply("A %s type attack would be %dx effective to a %s type".formatted(attackingtype, realResult, defenderPretty));
            }
        }
    }
}

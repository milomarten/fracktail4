package com.github.milomarten.fracktail4.commands.dnd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefenseParameters {
    @Builder.Default private Type defenderPrimaryType = Type.NONE;
    @Builder.Default private Type defenderSecondaryType = Type.NONE;
    private int defenseStat;
    private int defenseCS;
}

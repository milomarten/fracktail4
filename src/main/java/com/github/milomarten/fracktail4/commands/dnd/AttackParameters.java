package com.github.milomarten.fracktail4.commands.dnd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttackParameters {
    private int damageBase;
    @Builder.Default private Type attackType = Type.NONE;
    @Builder.Default private Type attackerPrimaryType = Type.NONE;
    @Builder.Default private Type attackerSecondaryType = Type.NONE;
    private int attackStat;
    private int attackCS;
    @Builder.Default private boolean criticalHit = false;

    public boolean isSTAB() {
        return attackType == attackerPrimaryType || (attackerSecondaryType != Type.NONE && attackType == attackerSecondaryType);
    }
}

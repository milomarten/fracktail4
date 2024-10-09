package com.github.milomarten.fracktail4.commands.dnd;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BattleRollTest {
    @Test
    public void range_JustDamageBaseBasically() {
        var attacker = AttackParameters.builder()
                .damageBase(3)
                .attackStat(10)
                .attackerPrimaryType(Type.NORMAL)
                .build();
        var defender = DefenseParameters.builder()
                .defenderPrimaryType(Type.NORMAL)
                .defenseStat(10)
                .build();

        var result = BattleRoll.doAttackRoll(attacker, defender);
        assertEquals(6, result.getRange().low().intValue());
        assertEquals(11, result.getRange().high().intValue());
        assertTrue(isInRange(result.getValue(), result.getRange().low(), result.getRange().high()));
    }

    @Test
    public void range_CritNormalAttack() {
        var attacker = AttackParameters.builder()
                .damageBase(3)
                .attackStat(10)
                .attackerPrimaryType(Type.NORMAL)
                .criticalHit(true)
                .build();
        var defender = DefenseParameters.builder()
                .defenderPrimaryType(Type.NORMAL)
                .defenseStat(10)
                .build();

        var result = BattleRoll.doAttackRoll(attacker, defender);
        assertEquals(12, result.getRange().low().intValue());
        assertEquals(22, result.getRange().high().intValue());
        assertTrue(isInRange(result.getValue(), result.getRange().low(), result.getRange().high()));
    }

    @Test
    public void range_JustDamageBaseSuperEffective() {
        var attacker = AttackParameters.builder()
                .damageBase(3)
                .attackStat(10)
                .attackType(Type.FIRE)
                .attackerPrimaryType(Type.NORMAL)
                .build();
        var defender = DefenseParameters.builder()
                .defenderPrimaryType(Type.GRASS)
                .defenseStat(10)
                .build();

        var result = BattleRoll.doAttackRoll(attacker, defender);
        assertEquals(12, result.getRange().low().intValue());
        assertEquals(22, result.getRange().high().intValue());
        assertTrue(isInRange(result.getValue(), result.getRange().low(), result.getRange().high()));
    }

    private boolean isInRange(BigDecimal val, BigDecimal low, BigDecimal high) {
        // return val >= low && val <= high
        return val.compareTo(low) >= 0 && val.compareTo(high) <= 0;
    }
}
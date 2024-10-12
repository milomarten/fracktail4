package com.github.milomarten.fracktail4.commands.dnd;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.DiceExpressionEvaluator;
import com.github.milomarten.fracktail4.commands.dice.term.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

public class BattleRoll {
    private static final DB[] DAMAGE_BASES = {
            new DB(0, 0, 0),
            new DB(1, 6, 1),
            new DB(1, 6, 3),
            new DB(1, 6, 5),
            new DB(1, 8, 6),
            new DB(1, 8, 8),
            new DB(2, 6, 8),
            new DB(2, 6, 10),
            new DB(2, 8, 10),
            new DB(2, 10, 10),
            new DB(3, 8, 10),
            new DB(3, 10, 10),
            new DB(3, 12, 10),
            new DB(4, 10, 10),
            new DB(4, 10, 15),
            new DB(4, 10, 20),
            new DB(5, 10, 20),
            new DB(5, 12, 25),
            new DB(6, 12, 25),
            new DB(6, 12, 30),
            new DB(6, 12, 35),
            new DB(6, 12, 40),
            new DB(6, 12, 45),
            new DB(6, 12, 50),
            new DB(6, 12, 55),
            new DB(6, 12, 60),
            new DB(7, 12, 65),
            new DB(8, 12, 70),
            new DB(8, 12, 80)
    };

    private static final BigDecimal[] CS_MULTIPLIERS = {
            new BigDecimal("0.4"),
            new BigDecimal("0.5"),
            new BigDecimal("0.6"),
            new BigDecimal("0.7"),
            new BigDecimal("0.8"),
            new BigDecimal("0.9"),
            BigDecimal.ONE,
            new BigDecimal("1.2"),
            new BigDecimal("1.4"),
            new BigDecimal("1.6"),
            new BigDecimal("1.8"),
            new BigDecimal("2"),
            new BigDecimal("2.2")
    };

    public static TotalHolder doAttackRoll(AttackParameters attacker, DefenseParameters defense) {
        int db = attacker.getDamageBase();
        if (attacker.isSTAB()) {
            db += 2;
        }
        var options = DiceEvaluatorOptions.builder().build();
        var damageRoll = getDB(db).totalHolder(options);
        if (attacker.isCriticalHit()) {
            damageRoll.add(getDB(db).totalHolder(options));
        }

        var baseAttack = new BigDecimal(attacker.getAttackStat());
        var attackMultiplier = getMultiplierForCS(attacker.getAttackCS());
        damageRoll.add(baseAttack.multiply(attackMultiplier));
        var baseDefense = new BigDecimal(defense.getDefenseStat());
        var defenseMultiplier = getMultiplierForCS(defense.getDefenseCS());
        damageRoll.subtract(baseDefense.multiply(defenseMultiplier));

        damageRoll.multiply(getMatchup(attacker.getAttackType(), defense.getDefenderPrimaryType()));
        damageRoll.multiply(getMatchup(attacker.getAttackType(), defense.getDefenderSecondaryType()));

        return damageRoll;
    }

    private static DB getDB(int value) {
        if (value <= 0) {
            return DAMAGE_BASES[1];
        } else if (value < DAMAGE_BASES.length) {
            return DAMAGE_BASES[value];
        } else {
            return DAMAGE_BASES[DAMAGE_BASES.length - 1];
        }
    }

    private static BigDecimal getMultiplierForCS(int cs) {
        return CS_MULTIPLIERS[cs + 6];
    }

    private static BigDecimal getMatchup(Type attack, Type defense) {
        return switch (TypeMatchup.getMatchup(attack, defense)) {
            case IMMUNE -> BigDecimal.ZERO;
            case HALF -> new BigDecimal("0.5");
            case NORMAL -> BigDecimal.ONE;
            case SUPER -> new BigDecimal("2");
        };
    }

    private record DB(int numDice, int diceFaces, int bonus) {
        public TermEvaluationResult eval(DiceEvaluatorOptions opts) {
            var dice = DiceExpression.builder()
                    .numberOfDice(numDice)
                    .numberOfSides(diceFaces)
                    .build();
            var bonus = ConstantTerm.of(this.bonus);

            var eval = new DiceExpressionEvaluator(opts);
            eval.push(dice);
            eval.push(Operation.ADD);
            eval.push(bonus);

            return eval.finish();
        }

        public Range range() {
            var faceRange = new Range(BigDecimal.ONE, BigDecimal.valueOf(this.diceFaces));
            return faceRange.multiply(BigDecimal.valueOf(this.numDice))
                    .add(BigDecimal.valueOf(this.bonus));
        }

        public TotalHolder totalHolder(DiceEvaluatorOptions opts) {
            return new TotalHolder(eval(opts).value(), range());
        }
    }

    @Data
    @AllArgsConstructor
    public static class TotalHolder {
        private BigDecimal value;
        private Range range;

        public void add(BigDecimal constant) {
            this.value = this.value.add(constant);
            this.range = this.range.add(constant);
        }

        public void add(TotalHolder holder) {
            this.value = this.value.add(holder.value);
            this.range = this.range.add(holder.range);
        }

        public void subtract(BigDecimal constant) {
            this.value = this.value.subtract(constant);
            this.range = this.range.subtract(constant);
        }

        public void multiply(BigDecimal constant) {
            this.value = this.value.multiply(constant);
            this.range = this.range.multiply(constant);
        }
    }
}

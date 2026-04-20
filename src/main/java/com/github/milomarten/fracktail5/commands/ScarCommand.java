package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail.core.dice.die.DiceExpression;
import com.github.milomarten.fracktail.core.dice.die.DicePoolTerm;
import com.github.milomarten.fracktail.core.dice.die.Die;
import com.github.milomarten.fracktail.core.dice.die.DotStrategy;
import com.github.milomarten.fracktail5.exception.DiscordResponseException;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.argument.*;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import lombok.*;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class ScarCommand implements DiscordSlashCommand {
    private static final Argument<Integer> DIFFICULTY = new IntArgumentParser("difficulty", "The difficulty of the roll, static or delta")
            .min(-2).max(9).defaultTo(7);
    private static final Argument<Integer> EXPLODE_AT = new IntArgumentParser("explode-at", "The explosion threshold")
            .min(8).max(10).defaultTo(10);
    private static final Argument<String> COMMENT = new StringArgumentParser("comment", "Describe what the roll is for")
            .defaultToEmpty();
    private static final Argument<Boolean> VISIBLE = new BooleanArgumentParser("visible", "If this roll is visible or just for you")
            .defaultTo(true);
    private final Details DISCORD_SPEC = new DiscordSubCommand(
                "scar",
                "Run a variety of SCAR commands"
            )
            .addBranch(new DiscordArgumentSubCommandDetails<>(
                    "dot",
                    "Do a dot roll",
                    new PojoParser<>(DotRollParameter::new)
                            .addField(
                                    new IntArgumentParser("num-dots", "The number of dots to roll")
                                            .min(1).required(),
                                    DotRollParameter::setNumDots
                            )
                            .addField(DIFFICULTY, DotRollParameter::setDifficulty)
                            .addField(EXPLODE_AT, DotRollParameter::setExplodeAt)
                            .addField(COMMENT, RollHelper::setComment)
                            .addField(VISIBLE, RollHelper::setVisible)
                    ,
                    RollHelper::respond
            ))
            .addBranch(new DiscordArgumentSubCommandDetails<>(
                    "skill",
                    "Do a skill check",
                    new PojoParser<>(SkillCheckParameter::new)
                            .addField(
                                    new IntArgumentParser("attribute", "The number of dots in the attribute")
                                            .min(1).max(10).required(),
                                    SkillCheckParameter::setAttribute
                            )
                            .addField(
                                    new IntArgumentParser("expertise", "The number of dots in the expertise")
                                            .min(0).max(5).required(),
                                    SkillCheckParameter::setExpertise
                            )
                            .addField(
                                    new IntArgumentParser("skill", "The number of dots in the skill")
                                            .min(0).max(5).required(),
                                    SkillCheckParameter::setSkill
                            )
                            .addField(
                                    new IntArgumentParser("bonus", "Any bonus dice for the pool")
                                            .min(-10).max(10).defaultTo(0),
                                    SkillCheckParameter::setBonus
                            )
                            .addField(DIFFICULTY, SkillCheckParameter::setDifficulty)
                            .addField(EXPLODE_AT, SkillCheckParameter::setExplodeAt)
                            .addField(COMMENT, RollHelper::setComment)
                            .addField(VISIBLE, RollHelper::setVisible)
                    ,
                    RollHelper::respond
            ))
            ;

    @Override
    public Details getDiscordSlashCommandDetails() {
        return DISCORD_SPEC;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    private static abstract class RollHelper {
        private String comment;
        private boolean visible;

        protected abstract int getNumDots();
        protected abstract int getDifficulty();
        protected abstract int getExplodeAt();
        protected abstract String getRollLine();

        private int getNormalizedDifficulty() {
            var difficulty = getDifficulty();
            return switch (difficulty) {
                case -2, -1, 0, 1, 2: yield 7 + difficulty;
                case 5, 6, 7, 8, 9: yield difficulty;
                default: throw new DiscordResponseException("Difficulty should be -2 to 2 (relative to 7), or 5 to 9 (absolute)");
            };
        }

        public DiscordResponse respond() {
            var difficulty = getNormalizedDifficulty();

            var strategy = new DotStrategy();
            strategy.setSuccessThreshold(difficulty);
            strategy.setFailureThreshold(1);

            var dotDice = DiceExpression.builder()
                    .numberOfDice(getNumDots())
                    .die(new Die(10))
                    .explodeAt(getExplodeAt()).infiniteExplode(true)
                    .totalingStrategy(strategy)
                    .build();

            var result = dotDice.evaluate(DiceEvaluatorOptions.builder()
                    .roundingMode(RoundingMode.HALF_UP) // unnecessary, but technically correct.
                    .build());

            var toUser = this.comment.isEmpty() ? "" : this.comment + "\n";
            toUser += getRollLine() + " -> " + result.representation() + " -> **" + result.valueAsInt(RoundingMode.HALF_UP) + "**";

            return visible ? DiscordResponses.reply(toUser) : DiscordResponses.replyEphemeral(toUser);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DotRollParameter extends RollHelper {
        private int numDots;
        private int difficulty = 7;
        private int explodeAt = 10;

        @Override
        protected String getRollLine() {
            var rollString = this.numDots + "•d10";
            if (difficulty != 7) {
                rollString += ", difficulty " + difficulty;
            }
            if (explodeAt != 10) {
                rollString += ", explode at " + explodeAt;
            }
            return rollString;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SkillCheckParameter extends RollHelper {
        private int attribute;
        private int expertise;
        private int skill;

        private int bonus = 0;
        private int difficulty = 7;
        private int explodeAt = 10;

        @Override
        protected int getNumDots() {
            return attribute + expertise + skill + bonus;
        }

        @Override
        protected String getRollLine() {
            var rollString = String.format("(Attr %d + Exp %d + Skill %d + Bonus %d)•d10", attribute, expertise, skill, bonus);
            if (difficulty != 7) {
                rollString += ", difficulty " + difficulty;
            }
            if (explodeAt != 10) {
                rollString += ", explode at " + explodeAt;
            }
            return rollString;
        }
    }
}

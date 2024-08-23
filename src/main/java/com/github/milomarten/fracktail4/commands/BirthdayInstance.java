package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.birthday.BirthdayCritter;
import com.github.milomarten.fracktail4.birthday.BirthdayUtils;
import discord4j.core.object.entity.Member;

import java.time.Year;
import java.util.OptionalInt;

public record BirthdayInstance(BirthdayCritter celebrator, Member member) {
    public String getBirthdayAsString() {
        return BirthdayUtils.getDisplayBirthday(celebrator.getDay());
    }

    public String getName() {
        return BirthdaySlashCommand.getName(this.member);
    }

    public OptionalInt getAge(Year now) {
        int age = celebrator.getYear()
                .map(birthYear -> now.getValue() - birthYear.getValue())
                .orElse(-1);
        return age < 0 ? OptionalInt.empty() : OptionalInt.of(age);
    }
}

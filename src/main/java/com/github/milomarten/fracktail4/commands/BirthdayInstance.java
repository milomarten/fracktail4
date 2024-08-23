package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.birthday.BirthdayCritter;
import com.github.milomarten.fracktail4.birthday.BirthdayUtils;
import discord4j.core.object.entity.Member;

import java.time.Year;
import java.util.Optional;
import java.util.OptionalInt;

public record BirthdayInstance(BirthdayCritter celebrator, Member member) {
    public String getBirthdayAsString() {
        return BirthdayUtils.getDisplayBirthday(celebrator.getDay());
    }

    public String getName() {
        return BirthdayUtils.getName(this.member);
    }

    public Optional<Integer> getAge(Year now) {
        return celebrator.getYear()
                .map(birthYear -> now.getValue() - birthYear.getValue());
    }
}

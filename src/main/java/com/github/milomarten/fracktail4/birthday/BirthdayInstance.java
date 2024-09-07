package com.github.milomarten.fracktail4.birthday;

import com.github.milomarten.fracktail4.commands.BirthdaySlashCommand;
import discord4j.core.object.entity.Member;

import java.time.Year;
import java.util.OptionalInt;

public record BirthdayInstance(BirthdayCritter celebrator, Member member) {
    public String getBirthdayAsString() {
        return BirthdayUtils.getDisplayBirthday(celebrator.getDay());
    }

    public String getName() {
        String username = member.getUsername();
        return member.getNickname()
                .map(n -> n + " (AKA " + username + ")")
                .orElse(username);
    }

    public OptionalInt getAge(Year now) {
        int age = celebrator.getYear()
                .map(birthYear -> now.getValue() - birthYear.getValue())
                .orElse(-1);
        return age < 0 ? OptionalInt.empty() : OptionalInt.of(age);
    }
}

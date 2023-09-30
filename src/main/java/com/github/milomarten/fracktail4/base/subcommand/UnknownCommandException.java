package com.github.milomarten.fracktail4.base.subcommand;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnknownCommandException extends RuntimeException {
    private final String attempt;
}

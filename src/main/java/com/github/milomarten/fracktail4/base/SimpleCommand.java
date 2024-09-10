package com.github.milomarten.fracktail4.base;

/**
 * Represents the most basic command, which, when invoked, gives a canned response.
 * On its own, a bean of this type does nothing. It is up to the platform to adapt a command
 * of this type into one that fits within it's platform's functionality.
 * @param command The command name
 * @param description A description of the command's purpose
 * @param response The response to return.
 */
public record SimpleCommand(String command, String description, String response) {
}

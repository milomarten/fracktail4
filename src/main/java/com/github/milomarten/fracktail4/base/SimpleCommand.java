package com.github.milomarten.fracktail4.base;

/**
 * The most basic command, which simply outputs a canned response with no parameters.
 * On its own, this command isn't fully enough. Platforms should implement their own handling for all basic commands,
 * such as this one.
 * @param command The command name
 * @param description The command description
 * @param response The response to output.
 */
public record SimpleCommand(String command, String description, String response) {
}

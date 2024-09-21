package com.github.milomarten.fracktail4.base;

/**
 * Represents the most basic command, which, when invoked, gives a canned response with translation.
 * On its own, a bean of this type does nothing. It is up to the platform to adapt a command
 * of this type into one that fits within it's platform's functionality.
 */
public class TranslatedSimpleCommand extends SimpleNoParameterCommand implements Translate {
    private final String response;

    /**
     * @param command The command name
     * @param description A description of the command's purpose
     * @param response The response to return.
     */
    public TranslatedSimpleCommand(String command, String description, String response) {
        super(command, description);
        this.response = response;
    }

    @Override
    public String getResponse() {
        return response;
    }
}

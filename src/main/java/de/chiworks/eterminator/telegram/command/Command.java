package de.chiworks.eterminator.telegram.command;

import de.chiworks.eterminator.telegram.user.User;
import lombok.Data;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Data
public abstract class Command<T extends CommandStatus> {

    private final static Map<String, Command<?>> COMMANDS = new HashMap<>();

    public static Optional<Command<?>> get(String command) {
        return ofNullable(COMMANDS.get(command));
    }

    public static Collection<Command<?>> getAll() {
        return COMMANDS.values();
    }

    public Command() {
        COMMANDS.put(getCommandString(), this);
    }

    @NonNull
    abstract public String getCommandString();

    @NonNull
    abstract public String getDescription();

    @NonNull
    abstract public CommandInProgress<T> execute(User user);

    abstract public void continueExec(User user, @NonNull String command, T status);
}

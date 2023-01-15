package de.chiworks.eterminator.telegram.user;

import de.chiworks.eterminator.eterminservice.data.SearchParameters;
import de.chiworks.eterminator.telegram.command.CommandInProgress;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Data
public class User {

    private final static Map<Long, User> USERS = new HashMap<>();

    public static Optional<User> get(long userId) {
        return ofNullable(USERS.get(userId));
    }

    private final long id;
    private final String name;
    private CommandInProgress<?> commandInProgress;
    private SearchParameters searchParameters;


    public User(long userId, @NonNull String name) {
        id = userId;
        this.name = name;
        USERS.put(userId, this);
    }

    public boolean continueLastCommand(@NonNull String commandString) {
        if (finishedPreviousCommand()) {
            return false;
        }
        commandInProgress.continueExec(commandString);
        return true;
    }

    private boolean finishedPreviousCommand() {
        return !hasPreviousCommand() || commandInProgress.isFinished();
    }

    private boolean hasPreviousCommand() {
        return commandInProgress != null;
    }

}

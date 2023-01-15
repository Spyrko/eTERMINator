package de.chiworks.eterminator.telegram.command;

import de.chiworks.eterminator.telegram.user.User;
import lombok.NonNull;

public record CommandInProgress<T extends CommandStatus>(Command<T> command, T status, User user) {

    public boolean isFinished() {
        return status.isFinished();
    }

    public void continueExec(@NonNull String commandString) {
        command.continueExec(user, commandString, status);
    }
}

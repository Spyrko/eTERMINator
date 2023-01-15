package de.chiworks.eterminator.telegram.command;

public interface CommandStatus {
    default boolean isFinished() {
        return true;
    }
}

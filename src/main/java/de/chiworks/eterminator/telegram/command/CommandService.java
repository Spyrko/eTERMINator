package de.chiworks.eterminator.telegram.command;

import de.chiworks.eterminator.error.CommandException;
import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.chiworks.eterminator.telegram.command.CommandStrings.HELP_COMMAND;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final SendService sendService;

    public void continueCommand(@NonNull User user, @NonNull String commandString) throws CommandException {
        if (!user.continueLastCommand(commandString)) {
            executeCommand(user, commandString);
        }
    }

    public void executeCommand(@NonNull User user, @NonNull String commandString) throws CommandException {
        Command<?> command = Command.get(commandString).
                or(() -> forwardToHelpCommand(user)).
                orElseThrow(() -> new CommandException("Help Command not found"));
        CommandInProgress<?> commandInProgress = command.execute(user);
        user.setCommandInProgress(commandInProgress);
    }

    private Optional<Command<?>> forwardToHelpCommand(@NonNull User user) {
        sendService.send(user, "Sorry, I did not understand you.");
        return Command.get(HELP_COMMAND);
    }
}

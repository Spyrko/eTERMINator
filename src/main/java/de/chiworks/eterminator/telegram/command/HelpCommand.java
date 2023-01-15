package de.chiworks.eterminator.telegram.command;

import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static de.chiworks.eterminator.telegram.command.CommandStrings.HELP_COMMAND;
import static de.chiworks.eterminator.telegram.command.VoidStatus.VOID_STATUS;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
@Component
@Slf4j
public class HelpCommand extends Command<VoidStatus> {

    private final SendService sendService;

    @Override
    public @NonNull String getCommandString() {
        return HELP_COMMAND;
    }

    @Override
    public @NonNull String getDescription() {
        return "Show this list of all commands";
    }

    @Override
    public @NonNull CommandInProgress<VoidStatus> execute(User user) {
        String commandBlock = Command.getAll().stream().map(this::toCommandAndDescription).collect(joining("\n"));
        sendService.send(user, "You can use the following commands:\n\n" + commandBlock);
        return new CommandInProgress<>(this, VOID_STATUS, user);
    }

    private String toCommandAndDescription(Command<?> command) {
        return format("    %s - %s", command.getCommandString(), command.getDescription());
    }

    @Override
    public void continueExec(User user, @NonNull String command, VoidStatus status) {
    }

}

package de.chiworks.eterminator.telegram.command;

import de.chiworks.eterminator.eterminservice.service.SearchService;
import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static de.chiworks.eterminator.telegram.command.CommandStrings.SEARCH_COMMAND;
import static de.chiworks.eterminator.telegram.command.CommandStrings.START_COMMAND;
import static de.chiworks.eterminator.telegram.command.VoidStatus.VOID_STATUS;

@Component
@RequiredArgsConstructor
public class SearchCommand extends Command<VoidStatus> {

    private final SearchService searchService;
    private final SendService sendService;

    @Override
    public @NonNull String getCommandString() {
        return SEARCH_COMMAND;
    }

    @Override
    public @NonNull String getDescription() {
        return "Trigger a manual search for Appointments";
    }

    @Override
    public @NonNull CommandInProgress<VoidStatus> execute(User user) {
        if (user.getSearchParameters() == null) {
            sendService.send(user, "Please configure your search first. You can do that with " + START_COMMAND);
        }
        if (searchService.checkForAppointments(user)) {
            sendService.send(user, "Yay! There are appointments available right now!");
            sendService.send(user, "You should check https://eterminservice.de/terminservice and book your appointment there.");
        } else {
            sendService.send(user, "Sadly there are no appointments available at the moment.\n" +
                    "You can try again later or just relax. I will check regularly and text you, as soon as an " +
                    "appointment is available.");
        }
        return new CommandInProgress<>(this, VOID_STATUS, user);
    }

    @Override
    public void continueExec(User user, @NonNull String command, VoidStatus status) {
    }
}

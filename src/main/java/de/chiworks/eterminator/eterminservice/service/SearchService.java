package de.chiworks.eterminator.eterminservice.service;

import de.chiworks.eterminator.error.CodeExpiredException;
import de.chiworks.eterminator.eterminservice.data.SearchQueryResult;
import de.chiworks.eterminator.quartz.QuartzService;
import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final TerminService terminService;
    private final QuartzService quartzService;
    private final SendService sendService;

    public boolean checkForAppointments(User user) {
        SearchQueryResult appointments = terminService.getAppointments(user.getSearchParameters())
                .doOnError(CodeExpiredException.class, e -> {
                    sendService.send(user, "I'm afraid your code has expired. Please get in touch with the doctor administering it to you, to get a new one.");
                    stopSearching(user);
                })
                .onErrorComplete()
                .block();
        return !Optional.ofNullable(appointments).map(SearchQueryResult::getPraxen).map(Map::isEmpty).orElse(true);
    }

    public void stopSearching(User user) {
        sendService.send(user, "I will stop looking for appointments now.");
        quartzService.stop(user);
    }
}

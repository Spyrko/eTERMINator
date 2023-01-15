package de.chiworks.eterminator.eterminservice.service;

import de.chiworks.eterminator.eterminservice.data.SearchQueryResult;
import de.chiworks.eterminator.telegram.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final TerminService terminService;

    public boolean checkForAppointments(User user) {
        SearchQueryResult appointments = terminService.getAppointments(user.getSearchParameters());
        return !appointments.getPraxen().isEmpty();
    }
}

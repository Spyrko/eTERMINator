package de.chiworks.eterminator.quartz;

import de.chiworks.eterminator.eterminservice.service.SearchService;
import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchJob implements Job {

    private final SearchService searchService;

    private final SendService sendService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        User user = (User) jobExecutionContext.getJobDetail().getJobDataMap().get("user");
        log.debug("Running search job for user '{}'", user.getName());
        if (searchService.checkForAppointments(user)) {
            log.trace("Appointment found");
            sendService.send(user, "Hey %s,\nthere is an open slot for you!\nPlease visit https://eterminservice.de/terminservice and schedule your appointment.".formatted(user.getName()));
            searchService.stopSearching(user);
        } else {
            log.trace("No appointment found");
        }
    }
}

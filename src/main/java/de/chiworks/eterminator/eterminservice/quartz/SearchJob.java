package de.chiworks.eterminator.eterminservice.quartz;

import de.chiworks.eterminator.eterminservice.service.SearchService;
import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class SearchJob implements Job {

    SearchService searchService;

    SendService sendService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        User user = (User) jobExecutionContext.getJobDetail().getJobDataMap().get("user");
        if (searchService.checkForAppointments(user)) {
            sendService.send(user, format("Hey %s,\nwe there is an open slot for you!\nPlease visit https://eterminservice.de/terminservice and schedule your appointment.", user.getName()));
            sendService.send(user, "I will stop looking for appointments now.");
            //remove job
        }

    }
}

package de.chiworks.eterminator.quartz;

import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.command.CommandStrings;
import de.chiworks.eterminator.telegram.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuartzService {

    private final SendService sendService;
    private final Scheduler scheduler;
    private static final String SEARCH_GROUP = "SEARCH";

    //TODO use user id as job id instead of username

    public void scheduleSearchJob(User user) {
        log.debug("Scheduling search job for user '{}'", user.getName());
        JobDetail job = createSearchJob(user);
        try {
            scheduleJob(job);
        } catch (SchedulerException e) {
            log.error("Unable to start job", e);
            sendService.send(user, "Looks like i had some trouble to schedule your search request. Please try again in a few minutes.\nYou can try again with %s".formatted(CommandStrings.RESTART_COMMAND));
        }
    }


    private void scheduleJob(JobDetail job)
            throws SchedulerException {
        scheduler.deleteJob(job.getKey());
        Trigger trigger = createTrigger(job);
        scheduler.scheduleJob(job, trigger);
        log.debug("Scheduled job with key '{}'", job.getKey());
    }

    public JobDetail createSearchJob(User user) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("user", user);
        return JobBuilder.newJob().ofType(SearchJob.class)
                .storeDurably()
                .withIdentity(user.getName(), SEARCH_GROUP)
                .withDescription("Search job of user '%s'".formatted(user.getName()))
                .usingJobData(dataMap)
                .build();
    }

    private Trigger createTrigger(JobDetail job) {
        log.debug("Creating trigger for job '{}'", job.getKey());
        return TriggerBuilder.newTrigger().forJob(job)
                .withIdentity("%s_trigger".formatted(job.getKey()))
                .withDescription("Hourly trigger for job '%s'".formatted(job.getKey()))
                .withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(10))
                .build();
    }

    public void stop(User user) {
        try {
            JobKey jobKey = JobKey.jobKey(user.getName(), SEARCH_GROUP);
            log.debug("Stopping job of user '{}' with key '{}'", user.getName(), jobKey);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("Unable to stop job", e);
        }
    }
}

package de.chiworks.eterminator;

import de.chiworks.eterminator.eterminservice.quartz.SearchJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@SpringBootApplication
public class ETermiNatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ETermiNatorApplication.class, args);
    }

    @Bean
    public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(SearchJob.class);
        jobDetailFactory.setDescription("Search for open appointments");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

}

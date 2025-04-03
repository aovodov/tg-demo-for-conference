package org.example.quartz;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerStarter {
    private final Scheduler scheduler;
    @PostConstruct
    public void startScheduler() {
        try {
            scheduler.start();
            System.out.println("Quartz Scheduler started!");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

}

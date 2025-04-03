package org.example.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSchedulerService {
    private final Scheduler scheduler;
    private final QuartzConfig quartzConfig;

    public void scheduleMessage(Long chatId, String messageType, LocalDateTime scheduledTime) {
        try {
            JobDetail jobDetail = quartzConfig.buildJobDetail(chatId, messageType);
            Trigger trigger = quartzConfig.buildJobTrigger(jobDetail, scheduledTime);
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Planned delayed task for chatId: {}, type: {}, time: {}", chatId, messageType, scheduledTime);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

}

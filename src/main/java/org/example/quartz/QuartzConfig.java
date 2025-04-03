package org.example.quartz;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class QuartzConfig {
    public JobDetail buildJobDetail(Long chatId, String messageType) {
        return JobBuilder.newJob(MessageJob.class)
                .withIdentity("messageJob-" + chatId + messageType)
                .usingJobData("chatId", chatId)
                .usingJobData("messageType", messageType)
                .build();
    }

    public Trigger buildJobTrigger(JobDetail jobDetail, LocalDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName() + "-trigger")
                .startAt(java.sql.Timestamp.valueOf(startAt))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withMisfireHandlingInstructionFireNow()
                )
                .build();
    }

}

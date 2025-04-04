package org.example.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ChatUser;
import org.example.TelegramDemoBot;
import org.example.believe.BelieveService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.example.Constants.JOB_ACTION;
import static org.example.Constants.JOB_INFO;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageJob implements Job {
    private final TelegramDemoBot telegramDemoBot;
    private final BelieveService believeButtons;

    @Override
    public void execute(JobExecutionContext context) {
        Long chatId = context.getJobDetail().getJobDataMap().getLong("chatId");
        String messageType = context.getJobDetail().getJobDataMap().getString("messageType");
        telegramDemoBot.checkUser(chatId);
        ChatUser user = telegramDemoBot.userCache.get(chatId);
        switch (messageType) {
            case JOB_INFO:
                telegramDemoBot.sendMessageToChat(SendMessage.builder()
                        .chatId(chatId.toString())
                        .text(
                                user.getBelieveQuestionId() == 3
                                        ? "\u203C Старт второго раунда через 10 минут! Будь на связи, не забывай — всего 15 секунд на ответ."
                                        : "\u203C Старт третьего раунда через 10 минут! Будь на связи, не забывай — всего 15 секунд на ответ."
                        )
                        .build());
                break;
            case JOB_ACTION:
                telegramDemoBot.sendMessageToChat(SendMessage.builder().chatId(user.getChatId()).text(
                        user.getBelieveQuestionId() == 3
                                ? "⚡️ Начинаем второй раунд! Удачи!"
                                : "⚡️ Начинаем третий раунд! Удачи!"
                ).build());
                telegramDemoBot.sendMessageToChat(believeButtons.getBelieveQuestion(user));
                user.setBelieveQuestionScheduled(false);
                break;
            default:
                break;
        }
        log.info("Firing trigger for delayed message sending, chatId: {}, messageType: {}", chatId, messageType);
    }
}

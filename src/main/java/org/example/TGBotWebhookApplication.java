package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication(scanBasePackages = "org.example")
@Slf4j
public class TGBotWebhookApplication {
    public static void main(String[] args) {
        SpringApplication.run(TGBotWebhookApplication.class, args);
    }

    @Bean
    public TelegramBotsApi startWebhookBot(TelegramDemoBot bot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot, bot.getSetWebhook());
            log.info("Webhook-bot started!");
            return botsApi;
        } catch (Exception e) {
            log.error("Unable to start Webhook-bot", e);
            throw new RuntimeException(e);
        }

    }

}
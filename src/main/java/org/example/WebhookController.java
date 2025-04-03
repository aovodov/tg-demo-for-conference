package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    @Value("${telegram.bot.secret}")
    private String botSecret;

    private final TelegramDemoBot bot;
    @PostMapping("/bot")
    public BotApiMethod<?> onUpdateReceived(
            @RequestBody Update update,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = true) String secretToken) {

        if (!botSecret.equals(secretToken)) {
            throw new RuntimeException("Invalid secret token");
        }

        return bot.onWebhookUpdateReceived(update);
    }

}

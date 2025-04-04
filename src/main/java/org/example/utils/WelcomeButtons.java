package org.example.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static org.example.Constants.WELCOME_BELIEVE;
import static org.example.Constants.WELCOME_QUIZ;
import static org.example.Constants.WELCOME_VOTE;
import static org.example.utils.ButtonUtils.addInlineButton;
import static org.example.utils.ButtonUtils.addSubscribeButton;

public class WelcomeButtons {
    public static SendMessage getWelcomeButtons(Long chatId, String messageText) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(List.of(addInlineButton("Квиз", WELCOME_QUIZ, null)));
        rowsInline.add(List.of(addInlineButton("Верю не Верю", WELCOME_BELIEVE, null)));
        rowsInline.add(List.of(addInlineButton("Развитие ТестОпс", WELCOME_VOTE, null)));
        rowsInline.add(List.of(addSubscribeButton()));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Привет, это Тестопс Бот! Чтобы начать, выбери активность");
        if (messageText != null) {
            message.setText(messageText);
        }
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }
}

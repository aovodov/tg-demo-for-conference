package org.example.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static org.example.Constants.BACK_TO_WELCOME;

public class ButtonUtils {

    public static InlineKeyboardButton addInlineButton(String text, String button) {
        return addInlineButton(text, button, null);
    }

    public static InlineKeyboardButton addInlineButton(String text, String button, String url) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(button);
        if (url != null) {
            inlineKeyboardButton.setUrl(url);
        }
        return inlineKeyboardButton;
    }

    public static SendMessage getInitActivityButtons(Long chatId, String caption, String callBack, String messageText) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(addInlineButton(caption, callBack)));
        rowsInline.add(List.of(addBackButton()));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(messageText);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    public static InlineKeyboardButton addBackButton() {
        return addInlineButton("🔙️ Вернуться в главное меню", BACK_TO_WELCOME);
    }

    public static InlineKeyboardButton addSubscribeButton() {
        return addInlineButton("Telegram-канал ТестОпс", "button4", "https://t.me/+7DIrNVa1aJ05ZjUy");
    }
}

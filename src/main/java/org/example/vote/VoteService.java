package org.example.vote;

import lombok.RequiredArgsConstructor;
import org.example.ChatUser;
import org.example.repository.ChatUserRepository;
import org.example.utils.WelcomeButtons;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.example.Constants.OPTION1;
import static org.example.Constants.OPTION2;
import static org.example.Constants.OPTION3;
import static org.example.Constants.OPTION4;
import static org.example.Constants.OPTION5;
import static org.example.Constants.OPTION6;
import static org.example.Constants.OPTION7;
import static org.example.Constants.OPTION8;
import static org.example.Constants.VOTE_SAVE;
import static org.example.Constants.VOTE_TESTOPS_NOUSER;
import static org.example.Constants.VOTE_TESTOPS_USER;
import static org.example.utils.ButtonUtils.addBackButton;
import static org.example.utils.ButtonUtils.addInlineButton;
import static org.example.utils.ButtonUtils.addSubscribeButton;
import static org.example.utils.WelcomeButtons.getWelcomeButtons;

@Component
@RequiredArgsConstructor
public class VoteService {
    private final VoteResultService voteResultService;
    private final ChatUserRepository chatUserRepository;

    public SendMessage getTestOpsUserButtons(Long chatId) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(
                addInlineButton("Да", VOTE_TESTOPS_USER),
                addInlineButton("Нет", VOTE_TESTOPS_NOUSER)
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Ты пользователь ТестОпс?");
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    public SendMessage getInitVoteButtons(ChatUser user) {
        List<List<InlineKeyboardButton>> rowsInline = generateButtons(user.getSelected());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        String messageText = """
                1. Инновации и AI — умные подсказки, автогенерация тестов, приоритизация с помощью ИИ
                
                2. Аналитика и инсайты — эффективность команды, стоимость запусков, динамика, узкие места
                
                3. Quality Gates — контроль качества на уровне CI/CD, правила прохождения, автоматические проверки
                
                4. Требования и покрытие — работа с требованиями, связь с кейсами, анализ покрытия
                
                5. Релизы — управление тестированием релизов, прогресс, готовность, релизные отчёты
                
                6. Производительность — ускорение загрузки интерфейса, деревьев, дашбордов, запусков и работы с большими объёмами данных
                
                7. Улучшения по фидбэку — доработки и UX-фиксы в сценариях, дефектах, запусках, тест-планах и других разделах
                
                8. Другое
                """;
        message.setText(messageText);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    private List<List<InlineKeyboardButton>> generateButtons(EnumSet<Vote> selected) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(
                addInlineButton(selected.contains(Vote.OPTION1) ? "☑️1️⃣" : "1️⃣", OPTION1),
                addInlineButton(selected.contains(Vote.OPTION2) ? "☑️2️⃣" : "2️⃣", OPTION2),
                addInlineButton(selected.contains(Vote.OPTION3) ? "☑️3️⃣" : "3️⃣", OPTION3),
                addInlineButton(selected.contains(Vote.OPTION4) ? "☑️4️⃣" : "4️⃣", OPTION4)
        ));
        rowsInline.add(List.of(
                addInlineButton(selected.contains(Vote.OPTION5) ? "☑️5️⃣" : "5️⃣", OPTION5),
                addInlineButton(selected.contains(Vote.OPTION6) ? "☑️6️⃣" : "6️⃣", OPTION6),
                addInlineButton(selected.contains(Vote.OPTION7) ? "☑️7️⃣" : "7️⃣", OPTION7),
                addInlineButton(selected.contains(Vote.OPTION8) ? "☑️8️⃣" : "8️⃣", OPTION8)
        ));
        rowsInline.add(List.of(addInlineButton("✅ Сохранить", VOTE_SAVE), addBackButton()));
        return rowsInline;
    }

    public EditMessageReplyMarkup changeKeys(ChatUser user, Integer messageId) {
        List<List<InlineKeyboardButton>> buttons = generateButtons(user.getSelected());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttons);
        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .chatId(user.getChatId())
                .messageId(messageId)
                .replyMarkup(inlineKeyboardMarkup)
                .build();
        return editMarkup;
    }

    @Transactional
    public SendMessage saveVoteResult(ChatUser user) {
        if (user.getVoteCounted()) {
            return getWelcomeButtons(user.getChatId(), null);
        }
        voteResultService.saveVoteResult(user);
        user.setVoteCounted(true);
        chatUserRepository.save(user);
        return getVotedMessage(user);
    }

    public SendMessage getVotedMessage(ChatUser user) {
        String messageText;
        if (user.selected.contains(Vote.OPTION8)) {
            messageText = "Спасибо за ответ! Вы выбрали пункт \"другое\", приходи на стенд, чтобы поделиться фидбеком и оставить feature request.\n" +
                    "Чтобы оставаться в курсе событий, подписывайтесь на наш тг канал";
        } else {
            messageText = "Спасибо за ответ! Чтобы оставаться в курсе событий, подписывайтесь на наш тг канал";
        }
        return getWelcomeButtons(user.getChatId(), messageText);
    }
}

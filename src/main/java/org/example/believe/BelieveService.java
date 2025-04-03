package org.example.believe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ChatUser;
import org.example.QuestionLoader;
import org.example.quartz.MessageSchedulerService;
import org.example.repository.ChatUserRepository;
import org.example.utils.ButtonUtils;
import org.example.utils.WelcomeButtons;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.Constants.BELIEVE_FALSE;
import static org.example.Constants.BELIEVE_INIT;
import static org.example.Constants.BELIEVE_TRUE;
import static org.example.Constants.JOB_ACTION;
import static org.example.Constants.JOB_INFO;
import static org.example.utils.ButtonUtils.addBackButton;
import static org.example.utils.ButtonUtils.addInlineButton;
import static org.example.utils.ButtonUtils.addSubscribeButton;

@Slf4j
@Component
@RequiredArgsConstructor
public class BelieveService {
    private final QuestionLoader questionLoader;
    private final ChatUserRepository chatUserRepository;
    private final MessageSchedulerService messageSchedulerService;

    public SendMessage getInitActivityButtons(Long chatId) {
        return ButtonUtils.getInitActivityButtons(chatId,
                "✅ Начать", BELIEVE_INIT,
                """
                        Угадай, веришь или нет?
                        9 фактов, 3 раунда, 15 секунд на ответ.
                        Не мешкай и выигрывай мерч ТестОпс, погнали!""");
    }

    public SendMessage getBelieveQuestion(ChatUser user, boolean answeredInTime) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(
                addInlineButton("✅ Верю", BELIEVE_TRUE),
                addInlineButton("🚫 Не верю", BELIEVE_FALSE)
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        user.setBelieveQuestionAskedTimestamp(System.currentTimeMillis());
        BelieveQuestion question = questionLoader.getBelieveQuestions().get(user.getBelieveQuestionId());
        String messageText = answeredInTime
                ? question.text()
                : String.format("Прошло больше 15 секунд, вы не успели с ответом\n%s", question.text());//todo add normal text
        message.setText(messageText);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    public SendMessage checkAnswer(ChatUser user, boolean answer) {
        if (user.getBelieveQuestionScheduled()) {
            return getButtonsForLaterQuestion(user, false);
        }
        if (user.getBelieveQuestionId() == -1) {
            return WelcomeButtons.getWelcomeButtons(user.getChatId(), String.format("Спасибо за игру! Ваш результат %d из %d!", user.getBelieveScore(), questionLoader.getBelieveQuestions().size()));
        }
        if (user.getBelieveQuestionAskedTimestamp() == 0)
            log.error("ACHTUNG!!!! user.getBelieveQuestionAskedTimestamp() == 0");
        boolean answeredInTime = System.currentTimeMillis() - user.getBelieveQuestionAskedTimestamp() < 15000;
        BelieveQuestion question = questionLoader.getBelieveQuestions().get(user.getBelieveQuestionId());
        if (answeredInTime && question.correct().equals(answer)) {
            user.setBelieveScore(user.getBelieveScore() + 1);
        }
        user.setBelieveQuestionId(user.getBelieveQuestionId() + 1);
        boolean askLater = user.getBelieveQuestionId() == 3
                || user.getBelieveQuestionId() == 6;
        if (askLater) {
            user.setBelieveQuestionScheduled(true);
            scheduleSending(user.getChatId());
            chatUserRepository.save(user);
            return getButtonsForLaterQuestion(user, true);
        }
        if (user.getBelieveQuestionId() == questionLoader.getBelieveQuestions().size()) {
            user.setBelieveQuestionId(-1);
            chatUserRepository.save(user);
            return WelcomeButtons.getWelcomeButtons(user.getChatId(), String.format("Спасибо за игру! Ваш результат %d из %d!", user.getBelieveScore(), questionLoader.getBelieveQuestions().size()));
        }
        return getBelieveQuestion(user, answeredInTime);
    }

    private void scheduleSending(Long chatId) {
        messageSchedulerService.scheduleMessage(chatId, JOB_INFO, LocalDateTime.now().plusSeconds(15));
        messageSchedulerService.scheduleMessage(chatId, JOB_ACTION, LocalDateTime.now().plusSeconds(30));
    }

    public SendMessage getButtonsForLaterQuestion(ChatUser user, boolean showTime) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(
                addBackButton(),
                addSubscribeButton()
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        String text = String.format("Ваш результат %d из %d!", user.getBelieveScore(), user.getBelieveQuestionId());
        if (showTime) {
            text += " Следующий раунд через час!";
        }
        message.setText(text);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }
}

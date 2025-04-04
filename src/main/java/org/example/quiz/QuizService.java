package org.example.quiz;

import lombok.RequiredArgsConstructor;
import org.example.ChatUser;
import org.example.QuestionLoader;
import org.example.utils.WelcomeButtons;
import org.example.repository.ChatUserRepository;
import org.example.utils.ButtonUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static org.example.Constants.QUIZ_ANSWER0;
import static org.example.Constants.QUIZ_ANSWER1;
import static org.example.Constants.QUIZ_ANSWER2;
import static org.example.Constants.QUIZ_ANSWER3;
import static org.example.Constants.QUIZ_INIT;
import static org.example.utils.ButtonUtils.addInlineButton;

@Component
@RequiredArgsConstructor
public class QuizService {
    private final QuestionLoader questionLoader;
    private final ChatUserRepository chatUserRepository;

    public SendMessage getQuizQuestion(ChatUser user) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(
                addInlineButton("1️⃣", QUIZ_ANSWER0),
                addInlineButton("2️⃣", QUIZ_ANSWER1),
                addInlineButton("3️⃣", QUIZ_ANSWER2),
                addInlineButton("4️⃣", QUIZ_ANSWER3)
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        QuizQuestion question = questionLoader.getQuizQuestions().get(user.getQuizQuestionId());
        String messageText = String.format("%s\n1. %s\n2. %s\n3. %s\n4. %s",
                question.text(), question.answers()[0], question.answers()[1], question.answers()[2], question.answers()[3]);
        message.setText(messageText);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    public SendMessage checkQuizAnswer(ChatUser user, int answer) {
        if (user.getQuizQuestionId() == -1) {
            return WelcomeButtons.getWelcomeButtons(user.getChatId(), String.format("🎉 Спасибо за участие в квизе! Твой результат %d из %d!\n" +
                    "Подарки вручаем на стенде ТестОпс за 12 правильных ответов 🤓", user.getQuizScore(), questionLoader.getQuizQuestions().size()));
        }
        if (questionLoader.getQuizQuestions().get(user.getQuizQuestionId()).correctAnswer() == answer) {
            user.setQuizScore(user.getQuizScore() + 1);
        }
        user.setQuizQuestionId(user.getQuizQuestionId() + 1);
        if (user.getQuizQuestionId() == questionLoader.getQuizQuestions().size()) {
            user.setQuizQuestionId(-1);
            chatUserRepository.save(user);
            return WelcomeButtons.getWelcomeButtons(user.getChatId(), String.format("🎉 Спасибо за участие в квизе! Твой результат %d из %d!\n" +
                    "Подарки вручаем на стенде ТестОпс за 12 правильных ответов 🤓", user.getQuizScore(), questionLoader.getQuizQuestions().size()));
        }
        return getQuizQuestion(user);
    }

    public SendMessage getInitActivityButtons(Long chatId) {
        return ButtonUtils.getInitActivityButtons(chatId, "✅ Начать", QUIZ_INIT,
                "Квиз на эрудицию: тестирование и общие IT-факты.\n" +
                        "Отвечай на вопросы и забирай мерч от ТестОпс. Нужно минимум 12 правильных ответов 🤓");
    }
}

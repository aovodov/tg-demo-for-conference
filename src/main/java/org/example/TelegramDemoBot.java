package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.believe.BelieveService;
import org.example.quiz.QuizService;
import org.example.repository.ChatUserRepository;
import org.example.utils.WelcomeButtons;
import org.example.vote.Vote;
import org.example.vote.VoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.Constants.BACK_TO_WELCOME;
import static org.example.Constants.BELIEVE_FALSE;
import static org.example.Constants.BELIEVE_INIT;
import static org.example.Constants.BELIEVE_TRUE;
import static org.example.Constants.OPTION1;
import static org.example.Constants.OPTION2;
import static org.example.Constants.OPTION3;
import static org.example.Constants.OPTION4;
import static org.example.Constants.OPTION5;
import static org.example.Constants.OPTION6;
import static org.example.Constants.OPTION7;
import static org.example.Constants.OPTION8;
import static org.example.Constants.QUIZ_ANSWER0;
import static org.example.Constants.QUIZ_ANSWER1;
import static org.example.Constants.QUIZ_ANSWER2;
import static org.example.Constants.QUIZ_ANSWER3;
import static org.example.Constants.QUIZ_INIT;
import static org.example.Constants.VOTE_SAVE;
import static org.example.Constants.VOTE_TESTOPS_NOUSER;
import static org.example.Constants.VOTE_TESTOPS_USER;
import static org.example.Constants.WELCOME_BELIEVE;
import static org.example.Constants.WELCOME_QUIZ;
import static org.example.Constants.WELCOME_VOTE;

@Slf4j
@Component
public class TelegramDemoBot extends TelegramWebhookBot {

    private final QuizService quizService;
    private final BelieveService believeButtons;
    private final QuestionLoader questionLoader;
    private final VoteService voteService;
    private final ChatUserRepository chatUserRepository;

    public TelegramDemoBot(@Value("${telegram.bot.token}") String botToken,
                           QuizService quizService,
                           QuestionLoader questionLoader,
                           BelieveService believeButtons,
                           ChatUserRepository chatUserRepository,
                           VoteService voteService) {
        super(botToken);
        this.setWebhook = SetWebhook.builder().url(botPath + "/webhook").build();
        this.quizService = quizService;
        this.questionLoader = questionLoader;
        this.believeButtons = believeButtons;
        this.chatUserRepository = chatUserRepository;
        this.voteService = voteService;
    }

    private final SetWebhook setWebhook;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.path}")
    private String botPath;

    public Map<Long, ChatUser> userCache = new ConcurrentHashMap<>();

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            checkUser(chatId);
            ChatUser user = userCache.get(chatId);
            switch (callbackData) {
                case WELCOME_QUIZ:
                    if (user.getQuizQuestionId() == -1) {
                        return new SendMessage(chatId.toString(),
                                String.format("🎉 Спасибо за участие в квизе! Твой результат %d из %d!\n" +
                                                "Подарки вручаем на стенде ТестОпс за 13 правильных ответов 🤓",
                                        user.getQuizScore(), questionLoader.getQuizQuestions().size()));
                    }
                    removeButtons(chatId, messageId);
                    return quizService.getInitActivityButtons(chatId);
                case QUIZ_INIT:
                    removeButtons(chatId, messageId);
                    return quizService.getQuizQuestion(user);
                case QUIZ_ANSWER0:
                    removeMessage(chatId, messageId);
                    return quizService.checkQuizAnswer(user, 0);
                case QUIZ_ANSWER1:
                    removeMessage(chatId, messageId);
                    return quizService.checkQuizAnswer(user, 1);
                case QUIZ_ANSWER2:
                    removeMessage(chatId, messageId);
                    return quizService.checkQuizAnswer(user, 2);
                case QUIZ_ANSWER3:
                    removeMessage(chatId, messageId);
                    return quizService.checkQuizAnswer(user, 3);
                case WELCOME_BELIEVE:
                    if (user.getBelieveQuestionId() == 3
                            || user.getBelieveQuestionId() == 6) {
                        removeButtons(chatId, messageId);
                        return believeButtons.getButtonsForLaterQuestion(user, false);
                    }
                    if (user.getBelieveQuestionId() == -1) {
                        return new SendMessage(chatId.toString(),
                                String.format("Спасибо за участие в игре Верю не Верю! Твоя интуиция справилась на %d из %d! ⭐️\n" +
                                                "Подарки вручаем на стенде ТестОпс за 6 правильных ответов 🤓",
                                        user.getBelieveScore(), questionLoader.getBelieveQuestions().size()));
                    }
                    removeButtons(chatId, messageId);
                    return believeButtons.getInitActivityButtons(chatId);
                case BELIEVE_INIT:
                    removeButtons(chatId, messageId);
                    return believeButtons.getBelieveQuestion(user);
                case BELIEVE_TRUE:
                    removeMessage(chatId, messageId);
                    return checkBelieveMessage(user, chatId, true);
                case BELIEVE_FALSE:
                    removeMessage(chatId, messageId);
                    return checkBelieveMessage(user, chatId, false);
                case WELCOME_VOTE:
                    if (user.getVoteCounted()) {
                        removeButtons(chatId, messageId);
                        return voteService.getVotedMessage(user);
                    }
                    removeButtons(chatId, messageId);
                    sendMessageToChat(new SendMessage(chatId.toString(), "Голосуй за направления развития TMS ТестОпc! Можно выбрать от 1 до 3 пунктов."));
                    return voteService.getTestOpsUserButtons(chatId);
                case VOTE_TESTOPS_USER:
                    user.setTestOpsUser(true);
                    removeButtons(chatId, messageId);
                    return voteService.getInitVoteButtons(user);
                case VOTE_TESTOPS_NOUSER:
                    user.setTestOpsUser(false);
                    removeButtons(chatId, messageId);
                    return voteService.getInitVoteButtons(user);
                case OPTION1:
                    countVote(user, messageId, Vote.OPTION1);
                    break;
                case OPTION2:
                    countVote(user, messageId, Vote.OPTION2);
                    break;
                case OPTION3:
                    countVote(user, messageId, Vote.OPTION3);
                    break;
                case OPTION4:
                    countVote(user, messageId, Vote.OPTION4);
                    break;
                case OPTION5:
                    countVote(user, messageId, Vote.OPTION5);
                    break;
                case OPTION6:
                    countVote(user, messageId, Vote.OPTION6);
                    break;
                case OPTION7:
                    countVote(user, messageId, Vote.OPTION7);
                    break;
                case OPTION8:
                    countVote(user, messageId, Vote.OPTION8);
                    break;
                case VOTE_SAVE:
                    removeButtons(chatId, messageId);
                    return voteService.saveVoteResult(user);
                case BACK_TO_WELCOME:
                    removeButtons(chatId, messageId);
                    return WelcomeButtons.getWelcomeButtons(chatId, null);
            }
        }
        if (update != null && update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            checkUser(chatId);
            String userMessage = update.getMessage().getText();
            if (userMessage.equals("/start")) {
                return WelcomeButtons.getWelcomeButtons(chatId, null);
            }
        }
        return null;
    }

    private SendMessage checkBelieveMessage(ChatUser user, Long chatId, boolean answer) {
        boolean answeredInTime = System.currentTimeMillis() - user.getBelieveQuestionAskedTimestamp() < 15000;
        if (!answeredInTime) {
            sendMessageToChat(new SendMessage(chatId.toString(),
                    "Ой, время вышло! ⏳ Не переживай, впереди ещё много интересного! Не мешкай и лови следующий факт!"));
        }
        return believeButtons.checkAnswer(user, answer, answeredInTime);
    }

    private void removeButtons(Long chatId, Integer messageId) {
        EditMessageReplyMarkup removeKeyboard = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(null)
                .build();
        changeKeyboard(removeKeyboard);
    }

    private void countVote(ChatUser user, Integer messageId, Vote option) {
        if (user.getVoteCounted()) {
            return;
        }
        if (user.selected.contains(option)) {
            user.selected.remove(option);
            changeKeyboard(voteService.changeKeys(user, messageId));
        } else if (user.selected.size() < 3) {
            user.selected.add(option);
            changeKeyboard(voteService.changeKeys(user, messageId));
        }
    }

    public void sendMessageToChat(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void changeKeyboard(EditMessageReplyMarkup editedMarkup) {
        try {
            execute(editedMarkup);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void removeMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }


    public void checkUser(Long chatId) {
        if (!userCache.containsKey(chatId)) {
            chatUserRepository.findByChatId(chatId).
                    ifPresentOrElse(
                            chatUser -> userCache.put(chatId, chatUser),
                            () -> {
                                ChatUser chatUser = new ChatUser().setChatId(chatId);
                                chatUserRepository.save(chatUser);
                                userCache.put(chatId, chatUser);
                                log.info("New chat user: {}", chatUser.getChatId());
                            });
        }
    }

    public SetWebhook getSetWebhook() {
        return setWebhook;
    }


    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
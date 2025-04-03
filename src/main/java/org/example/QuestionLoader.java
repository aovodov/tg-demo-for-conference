package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.believe.BelieveQuestion;
import org.example.quiz.QuizQuestion;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@Getter
public class QuestionLoader {

    private static final String QUIZ_QUESTIONS_FILE = "quiz-questions.json";
    private static final String BELIEVE_QUESTIONS_FILE = "believe-questions.json";

    private List<BelieveQuestion> believeQuestions;
    private List<QuizQuestion> quizQuestions;

    @PostConstruct
    private void loadQuestions() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            quizQuestions = List.of(objectMapper.readValue(
                    getClass().getClassLoader().getResourceAsStream(QUIZ_QUESTIONS_FILE),
                    QuizQuestion[].class
            ));
            believeQuestions = List.of(objectMapper.readValue(
                    getClass().getClassLoader().getResourceAsStream(BELIEVE_QUESTIONS_FILE),
                    BelieveQuestion[].class
            ));
            log.info("questions loaded");
        } catch (IOException e) {
            throw new RuntimeException("Unable to load questions", e);
        }
    }
}
package org.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.example.vote.Vote;

import java.util.EnumSet;

@Getter
@Setter
@Accessors(chain = true)
@Entity
public class ChatUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private Integer quizQuestionId = 0;
    private Integer quizScore = 0;
    private Integer believeQuestionId = 0;
    private Integer believeScore = 0;
    private Long believeQuestionAskedTimestamp = 0L;
    private Boolean believeQuestionScheduled = false;
    private Boolean testOpsUser;
    private Boolean voteCounted = false;

    @Transient
    public EnumSet<Vote> selected = EnumSet.noneOf(Vote.class);
}

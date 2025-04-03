package org.example.vote;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.ChatUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VoteResultService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveVoteResult(ChatUser user) {
        boolean isTestOpsUser = Boolean.TRUE.equals(user.getTestOpsUser());
        for (Vote vote : user.getSelected()) {
            // Ищем существующую запись для данной опции и типа пользователя
            VoteResult existingVoteResult = entityManager.createQuery(
                            "SELECT v FROM VoteResult v WHERE v.voteOption = :voteOption AND v.isTestOpsUser = :isTestOpsUser",
                            VoteResult.class
                    )
                    .setParameter("voteOption", vote.name())
                    .setParameter("isTestOpsUser", isTestOpsUser)
                    .getResultStream()
                    .findFirst() // Возвращаем первый результат или null
                    .orElse(null);

            if (existingVoteResult != null) {
                // Если запись найдена, увеличиваем счётчик голосов
                existingVoteResult.setVotesCount(existingVoteResult.getVotesCount() + 1);
                entityManager.merge(existingVoteResult);
            } else {
                // Если записи нет, создаём новую
                VoteResult newVoteResult = new VoteResult();
                newVoteResult.setVoteOption(vote.name());
                newVoteResult.setVotesCount(1);
                newVoteResult.setIsTestOpsUser(isTestOpsUser);
                entityManager.persist(newVoteResult);
            }
        }
    }

}

package com.kobi.smartbot.repository;

import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.repository.entity.MessageJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DialogRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DialogRepository.class);

    @PersistenceContext
    private EntityManager em;

    public DialogRepository() {
    }

    public List<MessageJpa> getLastDialog(AbstractMessage msg) {
        return em.createQuery("SELECT m FROM MessageJpa m WHERE m.userId = :userId AND m.peerId = :peerId AND m.status = 'new' ORDER BY m.createStamp DESC", MessageJpa.class)
                .setParameter("userId", msg.getUserId())
                .setParameter("peerId", msg.getPeerId())
                .setMaxResults(7)
                .getResultList()
                .stream()
                .filter(m -> ChronoUnit.HOURS.between(m.getCreateStamp(), LocalDateTime.now()) <= 1)
                .sorted(Comparator.comparing(MessageJpa::getCreateStamp))
                .collect(Collectors.toList());
    }

    public void deleteDialog(Long userId) {
        em.createQuery("update MessageJpa m set m.status = 'delete' where m.userId = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public boolean searchMessages(Long userId, String searchText) {
        List<MessageJpa> messages = em.createQuery("SELECT m FROM MessageJpa m WHERE m.userId = :userId AND m.text = :searchText AND m.status = 'new' AND m.createStamp = (SELECT MAX(n.createStamp) FROM MessageJpa n WHERE n.userId = :userId)", MessageJpa.class)
                .setParameter("userId", userId)
                .setParameter("searchText", searchText)
                .getResultList();
        return messages.isEmpty();
    }

    public void saveMessage(MessageJpa message) {
        try {
            this.em.persist(message);
        } catch (Exception e) {
            LOG.error("saveMessage", e);
        }
    }
}

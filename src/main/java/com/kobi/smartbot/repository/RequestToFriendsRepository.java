package com.kobi.smartbot.repository;

import com.kobi.smartbot.repository.entity.FriendRequestCounterJpa;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class RequestToFriendsRepository {

    @PersistenceContext
    private EntityManager em;

    public RequestToFriendsRepository() {
    }

    public Optional<FriendRequestCounterJpa> getCounterByDate(LocalDate date) {
        try {
            FriendRequestCounterJpa counter = em.createQuery("select c from FriendRequestCounterJpa c where c.requestDate = :date", FriendRequestCounterJpa.class)
                    .setParameter("date", date)
                    .getSingleResult();
            return Optional.of(counter);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public FriendRequestCounterJpa saveOrUpdate(FriendRequestCounterJpa counter) {
        if (counter.getId() == null) {
            em.persist(counter);
            return counter;
        } else {
            return em.merge(counter);
        }
    }
}

package com.kobi.smartbot.service.vk;

import com.kobi.smartbot.repository.RequestToFriendsRepository;
import com.kobi.smartbot.repository.entity.FriendRequestCounterJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Service("FriendRequestLimiter")
public class FriendRequestLimiter {

    private final int dailyLimit = 40;

    private final RequestToFriendsRepository counterService;
    private FriendRequestCounterJpa counter;

    @Autowired
    public FriendRequestLimiter(RequestToFriendsRepository counterService) {
        this.counterService = counterService;
    }

    public boolean canSendRequest() {
        resetCounterIfNecessary();
        return counter.getRequestsSent() < dailyLimit;
    }

    public void sentRequest() {
        resetCounterIfNecessary();
        counter.setRequestsSent(counter.getRequestsSent() + 1);
        counterService.saveOrUpdate(counter);
    }

    private void resetCounterIfNecessary() {
        if (!counter.getRequestDate().equals(LocalDate.now())) {
            loadCounter();
        }
    }

    @PostConstruct
    private void loadCounter() {
        LocalDate today = LocalDate.now();
        counter = counterService.getCounterByDate(today).orElseGet(() -> {
            FriendRequestCounterJpa newCounter = new FriendRequestCounterJpa();
            newCounter.setRequestDate(today);
            newCounter.setRequestsSent(0);
            return counterService.saveOrUpdate(newCounter);
        });
    }

    public int getRemainingRequests() {
        resetCounterIfNecessary();
        return dailyLimit - counter.getRequestsSent();
    }
}
package com.kobi.smartbot.service;


import com.kobi.smartbot.listeners.telegram.TgBotListener;
import com.kobi.smartbot.listeners.vk.VkBotListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;


@Service("RunnerBotService")
@DependsOn("dataLoader")
public class RunnerBotService {

    private static final Logger LOG = LoggerFactory.getLogger(RunnerBotService.class);

    private final TgBotListener tgBotListener;
    private final VkBotListener vkBotListener;

    @Autowired
    public RunnerBotService(TgBotListener tgBotListener, VkBotListener vkBotListener) {
        this.tgBotListener = tgBotListener;
        this.vkBotListener = vkBotListener;
    }


    @PostConstruct
    public void startBot() {

// ========== Телеграм
        Runnable tgBotListenerStart = () -> {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(tgBotListener);
            } catch (TelegramApiException e){
                LOG.error("", e);
            }
        };
        new Thread(tgBotListenerStart).start();

// ========== Вк беседы
        Runnable chatListenerStart = vkBotListener::startCycleForChat;
        new Thread(chatListenerStart).start();


// ========== Вк группы
//        Runnable groupListenerStart = () -> {
//            groupService.startCycleForGroups();
//        };
//        new Thread(groupListenerStart).start();
    }
}

package com.kobi.smartbot.service;

import com.kobi.smartbot.model.AbstractMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MessageManager {

    private static final Logger LOG = LoggerFactory.getLogger(MessageManager.class);

    private final CommandExecuterService commandExecuterService;
    private final TypologizationService typologizator;

    @Autowired
    public MessageManager(CommandExecuterService commandExecuterService, TypologizationService typologizator) {
        this.commandExecuterService = commandExecuterService;
        this.typologizator = typologizator;
    }

    public List<AbstractMessage> handleMessages(AbstractMessage msg) {
        LOG.debug("==========================================================");
        LOG.debug("Источник: " + msg.getSubject() + ", Юзер:" + msg.getUserId());
        LOG.debug("Получено сообщение: " + msg.getText());

        List<AbstractMessage> listMsg = new ArrayList<>();
        msg.setMessageType(typologizator.getTypeMsg(msg));
        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (msg.getMessageType() != null) {
            Future<List<AbstractMessage>> future = executor.submit(() -> {
                try {
                    return commandExecuterService.execute(msg);
                } catch (Exception e) {
                    LOG.error("", e);
                    return new ArrayList<>();
                }
            });
            try {
                listMsg = future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("", e);
            }
            executor.shutdown();
        } else {
            LOG.debug("Не распознано...");
        }
        return listMsg;
    }
}

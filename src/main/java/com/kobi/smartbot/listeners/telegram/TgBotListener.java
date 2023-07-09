package com.kobi.smartbot.listeners.telegram;

import com.kobi.smartbot.config.TgBotProperties;
import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.TgMessage;
import com.kobi.smartbot.model.enums.SourceTypeEnum;
import com.kobi.smartbot.service.MessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class TgBotListener extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(TgBotListener.class);

    private final TgBotProperties tgBotProperties;
    private final MessageManager manager;

    @Autowired
    public TgBotListener(TgBotProperties tgBotProperties, MessageManager manager) {
        this.tgBotProperties = tgBotProperties;
        this.manager = manager;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Создаем экземпляр TgMessage и заполняем его данными
            TgMessage msg = createTgMessage(update);

            String answer;
            if (update.getMessage().getText().equals("/start")) {
                answer = "Готов к работе";
                SendMessage response = new SendMessage();
                response.setChatId(msg.getPeerId().toString());
                response.setText(answer);
                sendMsg(response);
            } else {
                process(msg);
            }
        }
    }

    private TgMessage createTgMessage(Update update) {
        Long rootId = 490190368L;
        TgMessage msg = new TgMessage();
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        msg.setMode(rootId.equals(userId) && rootId.equals(chatId));

        boolean isPrivate = "private".equals(update.getMessage().getChat().getType());
        msg.setPrivateChat(isPrivate);
        if (!isPrivate) {
            msg.setMessageId(update.getMessage().getMessageId());
        }
        msg.setText(update.getMessage().getText());
        msg.setSourceType(SourceTypeEnum.TELEGRAM);
        msg.setPeerId(chatId);
        msg.setUserId(userId);
        msg.setUserName(update.getMessage().getFrom().getUserName());
        msg.setSubject(update.getMessage().getChat().getTitle());
        msg.setFromUserId(update.getMessage().getReplyToMessage() != null ? update.getMessage().getReplyToMessage().getFrom().getId().toString() : null);
        return msg;
    }

    private void process(AbstractMessage msg) {
        List<AbstractMessage> listMsg = manager.handleMessages(msg);
        if (listMsg.size() > 0) {
            for (AbstractMessage answerMsg : listMsg) {
                TgMessage tgMessage = (TgMessage) answerMsg;
                String chatId = msg.getPeerId().toString();
                if (tgMessage.getPhotos() != null) {
                    SendMediaGroup responseImage = new SendMediaGroup();
                    responseImage.setChatId(chatId);
                    responseImage.setMedias(tgMessage.getPhotos());
                    responseImage.setReplyToMessageId(msg.getMessageId());
                    sendMedia(responseImage);
                } else if (tgMessage.getInputFile() != null) {
                    SendVoice responseVoice = new SendVoice();
                    responseVoice.setChatId(chatId);
                    responseVoice.setVoice(tgMessage.getInputFile());
                    responseVoice.setReplyToMessageId(msg.getMessageId());
                    sendVoice(responseVoice);
                } else {
                    SendMessage responseText = new SendMessage();
                    responseText.setChatId(chatId);
                    responseText.setText(answerMsg.getText() == null ? "Сбой" : answerMsg.getText());
                    responseText.setReplyToMessageId(msg.getMessageId());
                    sendMsg(responseText);
                }
            }
        }
    }

    public void sendMedia(SendMediaGroup mediaGroup) {
        try {
            execute(mediaGroup);
        } catch (TelegramApiException e) {
            LOG.error("", e);
        }
    }

    public void sendMsg(SendMessage responseText) {
        try {
            execute(responseText);
        } catch (TelegramApiException e) {
            LOG.error("", e);
        }
    }

    public void sendVoice(SendVoice responseVoice) {
        try {
            execute(responseVoice);
        } catch (TelegramApiException e) {
            LOG.error("", e);
        }
    }


    @Override
    public String getBotUsername() {
        return tgBotProperties.getUsername();
    }

    @Override
    public String getBotToken() {
        return tgBotProperties.getToken();
    }
}

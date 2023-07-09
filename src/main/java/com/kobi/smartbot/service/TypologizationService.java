package com.kobi.smartbot.service;

import com.kobi.smartbot.config.TgBotProperties;
import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.TgMessage;
import com.kobi.smartbot.model.VkMessage;
import com.kobi.smartbot.model.enums.CommandEnum;
import com.kobi.smartbot.model.enums.MessageTypeEnum;
import com.kobi.smartbot.model.enums.OtherEnum;
import com.kobi.smartbot.model.enums.UserEnum;
import com.kobi.smartbot.util.BotResources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class TypologizationService {

    private final BotResources botResources;
    private final VkBotProperties vkBotProperties;
    private final TgBotProperties tgBotProperties;

    @Autowired
    public TypologizationService(BotResources botResources, VkBotProperties vkBotProperties, TgBotProperties tgBotProperties) {
        this.botResources = botResources;
        this.vkBotProperties = vkBotProperties;
        this.tgBotProperties = tgBotProperties;
    }

    public MessageTypeEnum getTypeMsg(AbstractMessage msg) {
        MessageTypeEnum messageType = checkUser(msg.getUserId());
        if (messageType != null) {
            return messageType;
        }

        List<String> botNames;
        String botId;
        if (msg instanceof TgMessage) {
            botNames = tgBotProperties.getTriggerName();
            botId = tgBotProperties.getBotId();
        } else if (msg instanceof VkMessage) {
            botNames = vkBotProperties.getTriggerName();
            botId = vkBotProperties.getBotId().toString();
        } else {
            botNames = List.of();
            botId = "";
        }

        if (checkPrefix(msg, botNames) || msg.isPrivateChat() || botId.equals(msg.getFromUserId())) {
            messageType = checkCommand(msg);
            return messageType != null ? messageType : OtherEnum.NAME_BOT;
        }
        return null;
    }

    private MessageTypeEnum checkUser(Long userId) {
        if (userId < 0 || botResources.getUserIgnore().contains(userId.toString())) {
            return UserEnum.USER_IGNORE;
        }
        return null;
    }

    private MessageTypeEnum checkCommand(AbstractMessage msg) {
        String text = msg.getText();
        String prefixMsg = text.split(" ")[0].replace(",", "").toLowerCase();

        Stream<CommandEnum> commandEnumStream = Arrays.stream(CommandEnum.values());

        if (msg instanceof TgMessage) {
            commandEnumStream = commandEnumStream
                    .filter(commandEnum -> "all".equals(commandEnum.getType()));
        }
        CommandEnum typeEnum = commandEnumStream
                .filter(commandEnum -> commandEnum.equalsCode(prefixMsg))
                .findFirst()
                .orElse(null);

        if (typeEnum != null) {
            msg.setText(StringUtils.replaceIgnoreCase(text, typeEnum.getCode(), "").trim());
        }
        return typeEnum;
    }

    private boolean checkPrefix(AbstractMessage msg, List<String> botNames) {
        String originalText = msg.getText();
        String newText = checkBotNames(originalText, botNames);
        if (newText != null) {
            msg.setText(newText);
            return true;
        }
        return false;
    }

    private String checkBotNames(String message, List<String> botNames) {
        String text = message.length() > 20 ? message.substring(0, 20) : message;
        String lowerText = text.toLowerCase();
        return botNames.stream()
                .flatMap(substring -> Stream.of(substring + " ", substring + ","))
                .filter(lowerText::startsWith)
                .findFirst()
                .map(substring -> message.substring(substring.length()))
                .orElse(null);
    }
}
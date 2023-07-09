package com.kobi.smartbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TgBotProperties {

    private String token;
    private String username;
    private String botId;
    private List<String> triggerName;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public List<String> getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        List<String> items = new ArrayList<>();
        if (triggerName != null && !triggerName.isEmpty()) {
            items = Arrays.asList(triggerName.split(";"));
        }
        this.triggerName = items;
    }

}
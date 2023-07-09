package com.kobi.smartbot.config;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "vkontakte.api")
public class VkBotProperties {
    private String accessToken;
    private float version;
    private Integer botId;
    private VkApiClient vkClient;
    private List<String> triggerName;
    private UserActor actor;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public Integer getBotId() {
        return botId;
    }

    public void setBotId(Integer botId) {
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

    public VkApiClient getVkClient() {
        if (vkClient == null) {
            vkClient = new VkApiClient(HttpTransportClient.getInstance());
        }
        return vkClient;
    }

    public UserActor getActor() {
        if (actor == null) {
            actor = new UserActor(botId, accessToken);
        }
        return actor;
    }
}
package com.kobi.smartbot.listeners.vk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VkBotListener {

    private static final Logger LOG = LoggerFactory.getLogger(VkBotListener.class);

    private final VkLongPollService vkLongPollService;

    @Autowired
    public VkBotListener(VkLongPollService vkLongPollService) {
        this.vkLongPollService = vkLongPollService;
    }

    public void startCycleForChat() {
        try {
            vkLongPollService.processCycle();
        } catch (Exception e) {
            LOG.error("Сбой", e);
            startCycleForChat();
        }
    }
}

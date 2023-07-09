package com.kobi.smartbot.service.vk;

import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.model.AbstractMessage;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.friends.responses.GetRequestsResponse;
import com.vk.api.sdk.objects.users.UserFull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendsService {

    private static final Logger LOG = LoggerFactory.getLogger(FriendsService.class);

    private final FriendRequestLimiter limiter;
    private final VkBotProperties vkBotProperties;

    @Autowired
    public FriendsService(FriendRequestLimiter limiter, VkBotProperties vkBotProperties) {
        this.limiter = limiter;
        this.vkBotProperties = vkBotProperties;
    }

    public void checkRequestToFriends(AbstractMessage msg) {
        acceptAllFriendRequests();
        if (limiter.canSendRequest()) {
            int userId = msg.getUserId().intValue();
            if (isUserInFriendList(userId)) {
                sendFriendRequest(userId);
            }
            sendFriendRequestsToRecommendations();
        }
    }

    public void acceptAllFriendRequests() {
        try {
            GetRequestsResponse friendRequests = vkBotProperties.getVkClient().friends().getRequests(vkBotProperties.getActor()).execute();
            List<Integer> userIds = friendRequests.getItems();
            for (Integer userId : userIds) {
                vkBotProperties.getVkClient().friends().add(vkBotProperties.getActor()).userId(userId).execute();
            }
        } catch (ApiException | ClientException e) {
            LOG.error("Error accepting friend requests: " + e.getMessage());
        }
    }


    public boolean isUserInFriendList(int userId) {
        try {
            List<Integer> friends = vkBotProperties.getVkClient().friends().get(vkBotProperties.getActor()).execute().getItems();
            return friends.stream().noneMatch(id -> id == userId);
        } catch (ApiException | ClientException e) {
            LOG.error("Error getting friend list: " + e.getMessage());
        }
        return false;
    }


    public void sendFriendRequest(int userId) {
        try {
            if (limiter.canSendRequest()) {
                vkBotProperties.getVkClient().friends().add(vkBotProperties.getActor()).userId(userId).execute();
                limiter.sentRequest();
            }
        } catch (ApiException | ClientException e) {
            LOG.error("Error sending friend request: " + e.getMessage());
        }
    }

    public void sendFriendRequestsToRecommendations() {
        try {
            List<UserFull> recommendations = vkBotProperties.getVkClient().friends().getSuggestions(vkBotProperties.getActor()).execute().getItems();
            int invitationsSent = 0;

            LOG.debug("++++++++++++++++++++++++Заявки++++++++++++++++++++++++");
            for (UserFull user : recommendations) {
                if (limiter.canSendRequest()) {
                    vkBotProperties.getVkClient().friends().add(vkBotProperties.getActor()).userId(user.getId()).execute();
                    limiter.sentRequest();
                    invitationsSent++;
                    LOG.debug("Заявка в друзья ID: " + user.getId());
                } else {
                    LOG.debug("Достигнут дневной лимит. Не удается отправить больше запросов в друзья.");
                    break;
                }
                if (invitationsSent >= 3) {
                    LOG.debug("Отправлено 3 заявки в друзья.");
                    break;
                }
            }
            LOG.debug("++++++++++++++++++++++++++END+++++++++++++++++++++++++");
        } catch (ApiException | ClientException e) {
            LOG.error("Error sending friend requests to recommendations: " + e.getMessage());
        }
    }
}

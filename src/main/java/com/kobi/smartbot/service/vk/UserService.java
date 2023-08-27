package com.kobi.smartbot.service.vk;

import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.model.User;
import com.kobi.smartbot.repository.UserRepository;
import com.kobi.smartbot.repository.entity.UserJpa;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.ConversationMember;
import com.vk.api.sdk.objects.messages.ConversationWithMessage;
import com.vk.api.sdk.objects.messages.responses.GetConversationsResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final VkBotProperties vkBotProperties;
    private final UserRepository userRepository;

    @Autowired
    public UserService(VkBotProperties vkBotProperties, UserRepository userRepository) {
        this.vkBotProperties = vkBotProperties;
        this.userRepository = userRepository;
    }

    public Map<String, String> getMapUsers(List<String> userIds) {
        Map<String, String> userNames = new HashMap<>();
        try {
            List<GetResponse> userList = vkBotProperties.getVkClient().users()
                    .get(vkBotProperties.getActor())
                    .unsafeParam("v", vkBotProperties.getVersion())
                    .userIds(userIds)
                    .execute();
            for (GetResponse item : userList) {
                userNames.put(item.getId().toString(), item.getFirstName() + " " + item.getLastName());
            }
        } catch (ApiException | ClientException e) {
            LOG.error("",e);
        }
        return userNames;
    }

    public List<User> getUsers(List<String> userIds) {
        List<User> users = new ArrayList<>();
        try {
            List<GetResponse> userList = vkBotProperties.getVkClient().users()
                    .get(vkBotProperties.getActor())
                    .unsafeParam("v", vkBotProperties.getVersion())
                    .userIds(userIds)
                    .unsafeParam("fields", "sex")
                    .execute();
            for (GetResponse item : userList) {
                users.add(new User(item));
            }
        } catch (ApiException | ClientException e) {
            LOG.error("",e);
        }
        return users;
    }

    public String getFio(Long userId, String source) {
        UserJpa user = userRepository.findByUserIdAndSource(userId, source).orElse(null);
        if (user == null) {
            Map<String, String> newUsers;
            if (source.equals("VK")) {
                newUsers = getMapUsers(Collections.singletonList(userId.toString()));
            } else {
                return null;
            }
            String username = newUsers.get(userId.toString());
            if (username == null || username.isEmpty()) {
                return null;
            }
            user = new UserJpa();
            user.setUserId(userId);
            user.setUsername(username);
            user.setSource(source);
            userRepository.save(user);

        }
        return user.getUsername();
    }

    public void fillUsers()  {
        List<Integer> peerIds = null;
        try {
            GetConversationsResponse conversationsResponse = vkBotProperties.getVkClient().messages()
                    .getConversations(vkBotProperties.getActor())
                    .execute();

            List<ConversationWithMessage> conversationsWithMessages = conversationsResponse.getItems();

            peerIds = conversationsWithMessages.stream()
                    .map(conversationWithMessage -> conversationWithMessage.getConversation().getPeer().getId())
                    .collect(Collectors.toList());

        } catch (ApiException | ClientException e) {
            LOG.error("", e);
        }

        for (Integer peerId : peerIds) {
            saveNewUsersFromConversation(peerId.toString());
        }
    }

    private void saveNewUsersFromConversation(String peerId) {
        try {
            List<ConversationMember> members = vkBotProperties.getVkClient().messages()
                    .getConversationMembers(vkBotProperties.getActor(), Integer.parseInt(peerId))
                    .execute()
                    .getItems();

            List<String> userIds = members.stream()
                    .map(member -> String.valueOf(member.getMemberId()))
                    .collect(Collectors.toList());

            Map<String, String> fetchedUsers = getMapUsers(userIds);

            for (Map.Entry<String, String> entry : fetchedUsers.entrySet()) {
                Long userId = Long.valueOf(entry.getKey());
                UserJpa user = userRepository.findByUserIdAndSource(userId, "VK").orElse(null);

                if (user == null) {
                    user = new UserJpa();
                    user.setUserId(userId);
                    String username = entry.getValue();
                    if (username == null || username.isEmpty() ) {
                        continue;
                    }
                    user.setUsername(username);
                    user.setSource("VK");
                    userRepository.save(user);
                }
            }
        } catch (ApiException | ClientException e) {
            LOG.error("СБОЙ!",e);
        }
    }
}
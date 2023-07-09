package com.kobi.smartbot.service.vk;

import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.model.User;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final VkBotProperties vkBotProperties;

    @Autowired
    public UserService(VkBotProperties vkBotProperties) {
        this.vkBotProperties = vkBotProperties;
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
}
package com.kobi.smartbot.service.vk;


import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.model.Comment;
import com.kobi.smartbot.model.User;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.board.GetCommentsSort;
import com.vk.api.sdk.objects.board.TopicComment;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class GroupService {

    private final Logger LOG = LoggerFactory.getLogger(GroupService.class);


    private final VkBotProperties vkBotProperties;

    @Autowired
    public GroupService(VkBotProperties vkBotProperties) {
        this.vkBotProperties = vkBotProperties;
    }


    public List<User> getUserTopic(Integer groupId, Integer topicId) {
        List<User> users = new ArrayList<>();
        Set<User> usersSet = new HashSet<>();
        Integer commenId = null;
        int N = 0;
        try {
            int countComm = 0;
            while (N < 30) {
                String jsonStr = null;
                if (N == 0)
                    jsonStr = vkBotProperties.getVkClient().board().getComments(vkBotProperties.getActor(), groupId, topicId)
                            .count(100)
                            .unsafeParam("extended", "1")
                            .sort(GetCommentsSort.REVERSE_CHRONOLOGICAL)
                            .executeAsString();

                if (N > 0)
                    jsonStr = vkBotProperties.getVkClient().board().getComments(vkBotProperties.getActor(), groupId, topicId)
                            .count(100)
                            .unsafeParam("extended", "1")
                            .sort(GetCommentsSort.REVERSE_CHRONOLOGICAL)
                            .startCommentId(commenId)
                            .offset(100)
                            .executeAsString();

                JSONObject commentsJSON = (JSONObject) new JSONParser().parse(jsonStr);
                JSONObject response = (JSONObject) commentsJSON.get("response");
                if (response != null) {
                    commenId = Integer.parseInt(((JSONObject) ((JSONArray) response.get("items")).get(0)).get("id").toString());
                    JSONArray itemsJson = (JSONArray) response.get("items");
                    JSONArray usersJson = (JSONArray) response.get("profiles");
                    for (Object o : usersJson) {
                        JSONObject userJson = (JSONObject) o;
                        User user = new User(userJson);
                        usersSet.add(user);
                    }
                    countComm += itemsJson.size();
                    LOG.debug("Найдено сообщений: " + countComm);
                    LOG.debug("Найдено юзеров: " + usersSet.size());
                    if (itemsJson.size() < 100) {
                        break;
                    }
                } else
                    throw new RuntimeException("Неудалось получить список юзеров");
                TimeUnit.MILLISECONDS.sleep(200);
                N++;
            }
            users.addAll(usersSet);
        } catch (InterruptedException | ParseException | ClientException e) {
            LOG.error("",e);
        }
        return users;
    }

    public List<Comment> getComments(Integer groupId, Integer topicId, Integer limit) {
        Integer commentId = null;
        List<Comment> commentList = new ArrayList<>();
        int N = 0;
        if (limit == null) {
            limit = 40;
        } else {
            limit = limit / 100;
        }
        try {
            while (N < limit) {
                List<TopicComment> topicCommentList;
                if (N == 0) {
                    topicCommentList = vkBotProperties.getVkClient().board().getComments(vkBotProperties.getActor(), groupId, topicId)
                            .count(100)
                            .sort(GetCommentsSort.REVERSE_CHRONOLOGICAL)
                            .execute()
                            .getItems();
                } else {
                    topicCommentList = vkBotProperties.getVkClient().board().getComments(vkBotProperties.getActor(), groupId, topicId)
                            .count(100)
                            .sort(GetCommentsSort.REVERSE_CHRONOLOGICAL)
                            .startCommentId(commentId)
                            .offset(100)
                            .execute()
                            .getItems();
                }
                if (topicCommentList.size() > 1) {
                    commentId = topicCommentList.get(0).getId();
                    for (TopicComment item : topicCommentList) {
                        commentList.add(new Comment(item, groupId, topicId));
                    }
                } else {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(200);
                N++;
            }
        } catch (ApiException | ClientException | InterruptedException e) {
            LOG.error("",e);
        }
        return commentList;
    }

    @PostConstruct
    public void test() {
        LOG.debug("Создан бин GroupService");
    }
}

package com.kobi.smartbot.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Component
public class BotResources {

    private List<String> answersNegative;
    private List<String> userIgnore;
    private List<String> userNegative;

    private final DataLoader dataLoader;

    @Autowired
    public BotResources(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @PostConstruct
    public void loadResources() throws IOException {
        this.setAnswersNegative(dataLoader.loadFileLines("classpath:/public/answer/answers_negative.txt"));

        this.setUserIgnore(dataLoader.getIds(dataLoader.getSetting("users.ignore_list")));
        this.setUserNegative(dataLoader.getIds(dataLoader.getSetting("users.negative_list")));
    }

    public List<String> getAnswersNegative() {
        return answersNegative;
    }

    public void setAnswersNegative(List<String> answersNegative) {
        this.answersNegative = answersNegative;
    }

    public List<String> getUserIgnore() {
        return userIgnore;
    }

    public void setUserIgnore(List<String> userIgnore) {
        this.userIgnore = userIgnore;
    }

    public List<String> getUserNegative() {
        return userNegative;
    }

    public void setUserNegative(List<String> userNegative) {
        this.userNegative = userNegative;
    }

}

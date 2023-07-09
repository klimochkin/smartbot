package com.kobi.smartbot.integrations.gpt.request;


import com.kobi.smartbot.repository.entity.MessageJpa;

import java.util.ArrayList;
import java.util.List;

public class ReqChatGPT {
    private String model = "gpt-3.5-turbo";
    private List<Message> messages;
    double temperature;
    double presence_penalty = 0;
    double top_p;
    double frequency_penalty;
    boolean stream = false;
//    int max_tokens;


    public ReqChatGPT() {
    }

    public ReqChatGPT(String msqText) {
        Message msq = new Message(msqText);
        this.messages = new ArrayList<>();
        messages.add(msq);
    }

    public ReqChatGPT(List<MessageJpa> messageJpaList, boolean devMode) {
        this.messages = new ArrayList<>();

        if (devMode) {
//            messages.add(new Message("Ты ассистент java-программиста. Помогай в вопросах Java-разработки. Используй русский язык"));
            messages.add(new Message("Ты ассистент врача. Помогай в вопросах медицины. Используй русский язык"));
            temperature = 0.1;
            presence_penalty = 0;
            top_p = 0.1;
            frequency_penalty = 2;
        } else {
            messages.add(new Message("Используй русский язык, отвечай лаконично. Начинай каждый разговор с комплимента в первом сообщении"));
            temperature = 0.8;
            presence_penalty = 0;
            top_p = 0.8;
            frequency_penalty = 2;
//            max_tokens = 600;
        }
        for (MessageJpa msg : messageJpaList) {
            Message msq = new Message(msg);
            messages.add(msq);
        }
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(double presence_penalty) {
        this.presence_penalty = presence_penalty;
    }

    public double getTop_p() {
        return top_p;
    }

    public void setTop_p(double top_p) {
        this.top_p = top_p;
    }

    public double getFrequency_penalty() {
        return frequency_penalty;
    }

    public void setFrequency_penalty(int frequency_penalty) {
        this.frequency_penalty = frequency_penalty;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }


}
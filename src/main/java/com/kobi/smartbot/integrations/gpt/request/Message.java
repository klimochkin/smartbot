package com.kobi.smartbot.integrations.gpt.request;


import com.kobi.smartbot.repository.entity.MessageJpa;

public class Message {
    private String role;
    private String content;

    // constructors
    public Message() {
    }

    public Message(String content) {
        this.role = "system";
        this.content = content;
    }

    public Message(MessageJpa msg) {
        this.role = msg.getUserRole();
        this.content = msg.getText();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
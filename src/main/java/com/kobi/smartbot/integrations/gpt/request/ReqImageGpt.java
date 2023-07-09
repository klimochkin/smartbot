package com.kobi.smartbot.integrations.gpt.request;

public class ReqImageGpt {

    private String prompt;
    private int n = 4;

    public ReqImageGpt(String text) {
        this.prompt = text;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}

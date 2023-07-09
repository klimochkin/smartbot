package com.kobi.smartbot.listeners.vk.response;

import com.google.gson.JsonArray;

import java.util.List;


public class LongPollResponse {

    private Integer ts;
    private List<JsonArray> updates;

    public LongPollResponse() {
    }

    public Integer getTs() {
        return this.ts;
    }

    public List<JsonArray> getUpdates() {
        return this.updates;
    }


}

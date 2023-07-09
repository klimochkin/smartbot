package com.kobi.smartbot.integrations.gpt.response;

import java.util.List;

public class ImageGenerationResponse {
    private List<ImageData> data;

    public List<ImageData> getData() {
        return data;
    }

    public void setData(List<ImageData> data) {
        this.data = data;
    }

    public static class ImageData {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

package com.kobi.smartbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "openai.api")
public class OpenAIProperties {

    private GptConfig gpt4;
    private GptConfig gpt3;
    private GptConfig imageGen;

    public GptConfig getGpt4() {
        return gpt4;
    }

    public void setGpt4(GptConfig gpt4) {
        this.gpt4 = gpt4;
    }

    public GptConfig getGpt3() {
        return gpt3;
    }

    public void setGpt3(GptConfig gpt3) {
        this.gpt3 = gpt3;
    }

    public GptConfig getImageGen() {
        return imageGen;
    }

    public void setImageGen(GptConfig imageGen) {
        this.imageGen = imageGen;
    }

    // Вложенный класс для хранения конфигурации каждой модели GPT
    public static class GptConfig {
        private String endPoint;
        private String authorization;
        private String model;
        private String systemMsg;
        private Map<String, String> replace = new HashMap<>();

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }

        public String getAuthorization() {
            return authorization;
        }

        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSystemMsg() {
            return systemMsg;
        }

        public void setSystemMsg(String systemMsg) {
            this.systemMsg = systemMsg;
        }

        public Map<String, String> getReplace() {
            return replace;
        }

        public void setReplace(String replace) {
            this.replace = Arrays.stream(replace.split(";"))
                    .map(str -> str.split("="))
                    .filter(arr -> arr.length == 2)
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
        }
    }
}
package com.kobi.smartbot.integrations.gpt;

import com.google.gson.Gson;
import com.kobi.smartbot.config.OpenAIProperties;
import com.kobi.smartbot.integrations.gpt.request.ReqChatGPT;
import com.kobi.smartbot.integrations.gpt.request.ReqImageGpt;
import com.kobi.smartbot.integrations.gpt.response.ChatCompletion;
import com.kobi.smartbot.integrations.gpt.response.ImageGenerationResponse;
import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.Comment;
import com.kobi.smartbot.model.VkMessage;
import com.kobi.smartbot.model.enums.SourceTypeEnum;
import com.kobi.smartbot.repository.DialogRepository;
import com.kobi.smartbot.repository.entity.MessageJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.List;

@Service("GptService")
public class GptService {

    private static final Logger LOG = LoggerFactory.getLogger(GptService.class);

    private static final int MAX_RETRIES = 5;

    private final OpenAIProperties openAIProperties;
    private final DialogRepository dialogRepository;

    @Autowired
    public GptService(OpenAIProperties openAIProperties, DialogRepository dialogRepository) {
        this.openAIProperties = openAIProperties;
        this.dialogRepository = dialogRepository;
    }

    @Transactional
    public String resetDialog(AbstractMessage msg) {
        dialogRepository.deleteDialog(msg.getUserId());
        return "Ваш диалог сброшен";
    }

    @Transactional
    public String getAnswerChatGPT(AbstractMessage msg) {
        msg.setGptTextConfig(openAIProperties);
        saveMsgDb(msg, msg.getText(), "user");

        int retries = 3;
        while (retries < MAX_RETRIES) {
            try {
                return sendToGpt(msg);
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    LOG.error("Ошибка при добавлении таймаута", ie);
                }
                retries++;
                LOG.error("Сбой соединения. Попытка №" + retries, e);
            }
        }
        return "Сбой соединения. Повторите запрос.";
    }

    public ImageGenerationResponse getImageChatGPT(AbstractMessage msg) {
        int retries = 4;
        while (retries < MAX_RETRIES) {
            try {
                msg.setGptImageConfig(openAIProperties.getImageGen());
                return getImage(msg);
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    LOG.error("Ошибка при добавлении таймаута", ie);
                }
                retries++;
                LOG.error("Сбой соединения. Попытка №" + retries, e);
            }
        }
        return null;
    }

    private String sendToGpt(AbstractMessage msg) throws Exception {
        String url = "https://" + msg.getEndPointGpt() + "/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (msg.getAuthorizationGpt() != null) {
            headers.add("Authorization", msg.getAuthorizationGpt());
        }
        headers.add("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        List<MessageJpa> messageJpaList = dialogRepository.getLastDialog(msg);
        ReqChatGPT reqChatGPT = new ReqChatGPT(messageJpaList, msg.isMode());
        reqChatGPT.setModel(msg.getModelGpt());
        HttpEntity<ReqChatGPT> entity = new HttpEntity<>(reqChatGPT, headers);

        Gson gson = new Gson();
        LOG.debug("ChatGPT REQUEST: " + gson.toJson(entity.getBody()));

        RestTemplate restTemplate = new RestTemplate();

        int httpCode;
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(url, entity, String.class);

            LOG.debug("ChatGPT RESPONSE: " + responseEntity.getBody());
            httpCode = responseEntity.getStatusCodeValue();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOG.debug(e.getStatusCode().toString(), e);
            httpCode = e.getStatusCode().value();
        }
        String message;

        switch (httpCode) {
            case 429 -> message = "Вы превысили допустимое количество запросов в минуту";
            case 403 -> message = "Вы пытались использовать функциональность, которая вам недоступна или нарушить правила использования бота";
            case 401 -> message = "Попытка выполнить запрос без авторизации";
//            case 524 -> message = "Сбой соединения. Истекло время ожидания ответа. Повторите запрос.";
            case 413 -> {
                message = "Диалог достиг лимита и был завершен. Начат новый.\n\n";
                dialogRepository.deleteDialog(msg.getUserId());
                message += sendToGpt(msg);
            }
            case 200 -> {
                ChatCompletion chatCompletion = gson.fromJson(responseEntity.getBody(), ChatCompletion.class);
                message = chatCompletion.getChoices()[0].getMessage().getContent();
                saveMsgDb(msg, message, "assistant");
            }
            default -> throw new Exception();
        }
        return message;
    }

    private ImageGenerationResponse getImage(AbstractMessage msg) {
        if (msg.getText().isEmpty()) {
            return null;
        }

        int retries = 1;

        String url = "https://" + msg.getEndPointGpt() + "/v1/images/generations";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", msg.getAuthorizationGpt());
        headers.add("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        ReqImageGpt reqImageGpt = new ReqImageGpt(msg.getText());
        HttpEntity<ReqImageGpt> entity = new HttpEntity<>(reqImageGpt, headers);

        Gson gson = new Gson();
        LOG.debug("ChatGPT REQUEST: " + gson.toJson(entity.getBody()));
        while (retries < MAX_RETRIES) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
                LOG.debug("ChatGPT RESPONSE: " + responseEntity.getBody());

                return gson.fromJson(responseEntity.getBody(), ImageGenerationResponse.class);
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    LOG.error("Ошибка при добавлении таймаута", ie);
                }
                retries++;
                LOG.error("Сбой соединения. Попытка №" + retries, e);
            }
        }
        return null;
    }

    private void saveMsgDb(AbstractMessage msg, String msgText, String userRole) {
        if (userRole.equals("user") && !msgText.isEmpty() && dialogRepository.searchMessages(msg.getUserId(), msgText) || userRole.equals("assistant")) {
            if (msg.getSourceType().equals(SourceTypeEnum.VK_GROUP)) {
                MessageJpa msgJpa = new MessageJpa((Comment) msg, msgText, userRole);
                dialogRepository.saveMessage(msgJpa);
            }

            if (msg.getSourceType().equals(SourceTypeEnum.VK_CHAT)) {
                MessageJpa msgJpa = new MessageJpa((VkMessage) msg, msgText, userRole);
                dialogRepository.saveMessage(msgJpa);
            }

            if (msg.getSourceType().equals(SourceTypeEnum.TELEGRAM)) {
                MessageJpa msgJpa = new MessageJpa(msg, msgText, userRole);
                dialogRepository.saveMessage(msgJpa);
            }
        }
    }
}

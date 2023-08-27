package com.kobi.smartbot.listeners.vk;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.listeners.vk.response.LongPollResponse;
import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.VkMessage;
import com.kobi.smartbot.model.enums.SourceTypeEnum;
import com.kobi.smartbot.service.MessageManager;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.responses.GetLongPollServerResponse;
import com.vk.api.sdk.queries.longpoll.GetLongPollEventsQuery;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollServerQuery;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Random;


@Service
public class VkLongPollService {

    private static final Logger LOG = LoggerFactory.getLogger(VkLongPollService.class);

    private GetLongPollServerResponse longPollParams;

    private final MessageManager messageManager;
    private final VkBotProperties vkBotProperties;

    public VkLongPollService(VkBotProperties vkBotProperties, MessageManager messageManager) {
        this.vkBotProperties = vkBotProperties;
        this.messageManager = messageManager;
    }

    public void processCycle() throws ClientException, ApiException, ParseException, IOException, InterruptedException {
        initializeLongPollParams();

        GetLongPollEventsQuery eventsQuery;
        Integer timestamp = longPollParams.getTs();

        do {
            eventsQuery = createLongPollEventsQuery(timestamp);
            LongPollResponse response = executeAndGetResponse(eventsQuery);
            if (responseHasNoUpdates(response)) {
                LOG.debug("Протухание ключа LongPoll! Попытка повторного получения...");
                initializeLongPollParams();
                timestamp = longPollParams.getTs();
            } else {
                processEvents(response.getUpdates());
                timestamp = response.getTs();
                LOG.debug("Последнее событие: " + response.getTs().toString());
            }

        } while (true);
    }

    private GetLongPollEventsQuery createLongPollEventsQuery(Integer timestamp) {
        GetLongPollEventsQuery eventsQuery = vkBotProperties.getVkClient()
                .longPoll()
                .getEvents("https://" + longPollParams.getServer(), longPollParams.getKey(), timestamp.toString());
        eventsQuery.waitTime(25);
        eventsQuery.unsafeParam("mode", 2);
        return eventsQuery;
    }

    private LongPollResponse executeAndGetResponse(GetLongPollEventsQuery events) throws ClientException {
        String textResponse = events.executeAsString();
        return parseEventsResponse(textResponse);
    }

    private LongPollResponse parseEventsResponse(String eventResponseText) throws ClientException {
        JsonReader jsonReader = new JsonReader(new StringReader(eventResponseText));
        JsonObject jsonResponse = new JsonParser().parse(jsonReader).getAsJsonObject();

        if (jsonResponse.has("failed")) {
            handleFailedEventResponse(jsonResponse);
            return null;
        }

        return parseValidEventResponse(jsonResponse, eventResponseText);
    }

    private void handleFailedEventResponse(JsonObject jsonResponse) throws ClientException {
        int failureCode = jsonResponse.getAsJsonPrimitive("failed").getAsInt();

        if (failureCode == 1) {
            int ts = jsonResponse.getAsJsonPrimitive("ts").getAsInt();
            LOG.error("\'ts\' value is incorrect, minimal value is 1, maximal value is " + ts);
        } else if (failureCode != 2) {
            throw new ClientException("Unknown LongPollServer exception, something went wrong.");
        }
    }

    private LongPollResponse parseValidEventResponse(JsonObject jsonResponse, String eventResponseText) throws ClientException {
        try {
            return new Gson().fromJson(jsonResponse, LongPollResponse.class);
        } catch (JsonSyntaxException exception) {
            LOG.error("Invalid JSON: " + eventResponseText, exception);
            throw new ClientException("Can\'t parse json response");
        }
    }

    private boolean responseHasNoUpdates(LongPollResponse response) {
        return response == null || response.getUpdates().isEmpty();
    }

    private void initializeLongPollParams() {
        MessagesGetLongPollServerQuery longPollServer = vkBotProperties.getVkClient()
                .messages()
                .getLongPollServer(vkBotProperties.getActor())
                .lpVersion(3);
        longPollServer.unsafeParam("v", vkBotProperties.getVersion());
        longPollServer.needPts(true);
        try {
            this.longPollParams = longPollServer.execute();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void processEvents(List<JsonArray> updates) throws ClientException, ApiException, IOException, ParseException, InterruptedException {
        for (JsonArray arrayItem : updates) {
            int code = arrayItem.get(0).getAsInt();
            if (code == 4) {
                if ((Integer.parseInt(arrayItem.get(2).toString()) & 2) != 0) {
                    return;
                }
                VkMessage message = createVkMessage(arrayItem);
                List<AbstractMessage> listMsg = messageManager.handleMessages(message);
                if (listMsg.size() > 0) {
                    for (AbstractMessage answerMsg : listMsg) {
                        sendMessage((VkMessage) answerMsg);
                    }
                }
            }
        }
    }

    private VkMessage createVkMessage(JsonArray arrayItem) {
        VkMessage message = new VkMessage();
        message.setSourceType(SourceTypeEnum.VK_CHAT);
        message.setMessageId(Integer.parseInt(arrayItem.get(1).toString()));
        message.setFlags(Integer.parseInt(arrayItem.get(2).toString()));
        message.setTs(Long.parseLong(arrayItem.get(4).toString()));
        message.setSubject(arrayItem.get(5).toString());
        message.setText(arrayItem.get(6).toString().toLowerCase().replace("\"", ""));
        message.setPeerId(Long.parseLong(arrayItem.get(3).toString()));
        try {
            JsonObject item7 = arrayItem.get(7).getAsJsonObject();
            JsonElement from = item7.get("from");
            if (from != null) {
                Long userId = from.getAsLong();
                message.setUserId(userId);
            } else {
                boolean isPrivate = "\" ... \"".equals(message.getSubject());
                message.setPrivateChat(isPrivate);
                message.setUserId(isPrivate ? message.getPeerId() : 0L);
            }
            JsonElement jsonEl = item7.get("reply");
            if (jsonEl != null) {
                String convMsgJson = jsonEl.getAsString();
                Integer convMsgId = JsonParser.parseString(convMsgJson)
                        .getAsJsonObject()
                        .get("conversation_message_id")
                        .getAsInt();
                message.setFromUserId(getFromUserId(Math.toIntExact(message.getPeerId()), convMsgId));
            }
        } catch (Exception ignore) {
        }

        return message;
    }

    private String getFromUserId(int peerId, Integer convMsgId) {
        try {
            Integer fromUserId = vkBotProperties.getVkClient().messages().getByConversationMessageId(vkBotProperties.getActor(), peerId, convMsgId)
                    .execute()
                    .getItems()
                    .get(0)
                    .getFromId();
            return fromUserId != null ? fromUserId.toString() : null;
        } catch (ApiException | ClientException e) {
            LOG.error("",e);
        }
        return null;
    }

    private void sendMessage(VkMessage msg) throws ClientException, ApiException {
        Random random = new Random();
        MessagesSendQuery msgSend = vkBotProperties.getVkClient().messages().send(vkBotProperties.getActor())
                .peerId(msg.getPeerId().intValue())
                .message(msg.getText())
                .randomId(random.nextInt())
                .unsafeParam("v", vkBotProperties.getVersion());

        List<String> attachments = msg.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            String attachmentsString = String.join(",", attachments);
            msgSend.attachment(attachmentsString);
        }

        if (msg.isForward()) {
            msgSend.forwardMessages(msg.getMessageId()).execute();
        } else {
            msgSend.execute();
        }
        LOG.debug("Ответ: " + msg.getText() + " Вложения: " + msg.getAttachments());
    }
}

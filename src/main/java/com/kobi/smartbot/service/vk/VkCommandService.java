package com.kobi.smartbot.service.vk;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.integrations.gpt.GptService;
import com.kobi.smartbot.integrations.gpt.response.ImageGenerationResponse;
import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.Comment;
import com.kobi.smartbot.model.User;
import com.kobi.smartbot.model.VkMessage;
import com.kobi.smartbot.model.enums.SourceTypeEnum;
import com.kobi.smartbot.util.Helper;
import com.kobi.smartbot.util.MultipartUtility;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.messages.AudioMessage;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.responses.GetMessagesUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveMessagesPhotoResponse;
import com.vk.api.sdk.objects.video.SearchSort;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.video.responses.SearchResponse;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class VkCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(VkCommandService.class);
    
    private final Helper helper;
    private final ChatService chatService;
    private final GroupService groupService;
    private final VkBotProperties vkBotProperties;
    private final GptService gptService;


    @Autowired
    public VkCommandService(Helper helper, ChatService chatService, GroupService groupService, VkBotProperties vkBotProperties, GptService gptService) {
        this.helper = helper;
        this.chatService = chatService;
        this.groupService = groupService;
        this.vkBotProperties = vkBotProperties;
        this.gptService = gptService;
    }

    public AbstractMessage getAnswerCommandDVACH(AbstractMessage msg) throws ClientException, ApiException {
        Integer groupId = -22751485;
        Integer limit = 1985;
        AbstractMessage amsg = getPicture(groupId, limit);
        msg.setText(amsg.getText());
        msg.setAttachments(amsg.getAttachments());
        return msg;
    }

    public AbstractMessage getAnswerCommandMEM(AbstractMessage msg) throws ClientException, ApiException {
        Integer groupId = -45045130;
        Integer limit = 1985;
        AbstractMessage amsg = getPicture(groupId, limit);
        msg.setText(amsg.getText());
        msg.setAttachments(amsg.getAttachments());
        return msg;
    }

    public AbstractMessage getAnswerCommandGIF(AbstractMessage msg) throws ClientException, ApiException {
        String answer;
        String attach = null;

        if (!msg.getText().isEmpty()) {
            String text = msg.getText();
            com.vk.api.sdk.objects.docs.responses.SearchResponse response;
            response = vkBotProperties.getVkClient().docs().search(vkBotProperties.getActor(), text)
                    .unsafeParam("v", vkBotProperties.getVersion())
                    .count(10)
                    .offset(new Random().nextInt(300))
                    .execute();

            int size = response.getItems().size();
            if (size > 0) {
                Doc doc = response.getItems().get(new Random().nextInt(size));
                answer = "Вот, что я нашел";
                attach = "doc" + doc.getOwnerId() + "_" + doc.getId();
            } else
                answer = "Не смог ничего найти &#128546;";
        } else
            answer = "Что мне искать?";
        msg.setText(answer);
        List<String> attachList = new ArrayList<>();
        attachList.add(attach);
        msg.setAttachments(attachList);
        return msg;
    }

    public AbstractMessage getAnswerCommandVIDEO(AbstractMessage msg) throws ClientException, ApiException {
        String answer;
        String attach = null;
        if (!msg.getText().isEmpty()) {
            String text = msg.getText();
            SearchResponse response = vkBotProperties.getVkClient().videos()
                    .search(vkBotProperties.getActor(), text)
                    .unsafeParam("v", vkBotProperties.getVersion())
                    .sort(SearchSort.RELEVANCE)
                    .adult(false)
                    .execute();

            if (response != null) {
                int size = response.getItems().size();
                if (size != 0) {
                    int index = new Random().nextInt(size);
                    Video video = response.getItems().get(index);
                    answer = "Вот, что я нашел: ";
                    attach = "video" + video.getOwnerId() + "_" + video.getId();
                } else
                    answer = "Не смог ничего найти &#128546;";
            } else
                answer = "Ошибка поиска";
        } else
            answer = "Что мне искать?";

        msg.setText(answer);
        List<String> attachList = new ArrayList<>();
        attachList.add(attach);
        msg.setAttachments(attachList);
        return msg;
    }

    public String getAnswerCommandSAY(byte[] bytes) throws IOException, ParseException, ClientException, ApiException {
        URI uploadUrl = vkBotProperties.getVkClient().docs().getUploadServer(vkBotProperties.getActor())
                .unsafeParam("type", "audio_message")
                .execute()
                .getUploadUrl();
        MultipartUtility multipart = new MultipartUtility(uploadUrl.toURL(), "UTF-8");
        multipart.addBytesPart("file", bytes);
        List<String> multi = multipart.finish();
        String fileUpl = "";
        for (String line : multi) {
            JSONObject obj = (JSONObject) new JSONParser().parse(line);
            fileUpl = (String) obj.get("file");
        }

        AudioMessage doc = vkBotProperties.getVkClient().docs().save(vkBotProperties.getActor(), fileUpl)
                .execute()
                .getAudioMessage();

        return "doc" + doc.getOwnerId() + "_" + doc.getId();
    }

    public List<User> getAnswerCommandOnline(AbstractMessage msg) throws ApiException, ClientException {
        List<User> users = null;
        if (SourceTypeEnum.VK_CHAT.equals(msg.getSourceType())) {
            VkMessage chatVkMessage = (VkMessage) msg;
            users = chatService.getChatUsers(chatVkMessage.getPeerId().toString());
        } else if (SourceTypeEnum.VK_GROUP.equals(msg.getSourceType())) {
            Comment groupMessage = (Comment) msg;
            users = groupService.getUserTopic(groupMessage.getGroupId() * -1, groupMessage.getTopicId());
        }
        return users;
    }

    public  List<AbstractMessage> getAnswerCommandDETECTOR(AbstractMessage msg) {
        List<AbstractMessage> listAbsMsg = new ArrayList<>();
        if (SourceTypeEnum.VK_CHAT.equals(msg.getSourceType())) {
            VkMessage vkMessage = (VkMessage) msg;
            List<VkMessage> listMsg = chatService.getChatMessages(vkMessage.getPeerId().intValue());
            listAbsMsg.addAll(listMsg);
        }

        if (msg instanceof Comment) {
            Comment comment = (Comment) msg;
            Integer limit = null;
            String text = msg.getText();

            try {
                limit = Integer.parseInt(text);
            } catch (Exception ignored) {
            }
            List<Comment> listComm = groupService.getComments(comment.getGroupId() * -1, comment.getTopicId(), limit);
            listAbsMsg.addAll(listComm);
        }

        return listAbsMsg;
    }

    public List<String> getPhotos(ImageGenerationResponse images) {
        List<String> attachments = new ArrayList<>();
        try {
            MultiValueMap<String, Object> uploadBody = new LinkedMultiValueMap<>();
            final int[] imageIndex = {0};

            for (ImageGenerationResponse.ImageData imageData : images.getData()) {
                URL urlImage = new URL(imageData.getUrl());
                BufferedImage image = ImageIO.read(urlImage);

                ByteArrayResource imageResource = new ByteArrayResource(helper.createImageBytes(image)) {
                    @Override
                    public String getFilename() {
                        return "image" + imageIndex[0] + ".png";
                    }
                };

                uploadBody.add("file" + (imageIndex[0] + 1), imageResource);
                imageIndex[0]++;
            }

            GetMessagesUploadServerResponse uploadServer = vkBotProperties.getVkClient().photos().getMessagesUploadServer(vkBotProperties.getActor()).execute();
            URL uploadUrl = uploadServer.getUploadUrl().toURL();
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(uploadBody, uploadHeaders);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> uploadResponse = restTemplate.postForEntity(uploadUrl.toString(), uploadEntity, String.class);

            JsonParser parser = new JsonParser();
            JsonObject response = parser.parse(Objects.requireNonNull(uploadResponse.getBody())).getAsJsonObject();

            Integer server = response.get("server").getAsInt();
            String photo = response.get("photo").getAsString();
            String hash = response.get("hash").getAsString();

            List<SaveMessagesPhotoResponse> photos = vkBotProperties.getVkClient().photos().saveMessagesPhoto(vkBotProperties.getActor(), photo).server(server).hash(hash).execute();

            for (SaveMessagesPhotoResponse photoResponse : photos) {
                String attachment = "photo" + photoResponse.getOwnerId() + "_" + photoResponse.getId();
                attachments.add(attachment);
            }
        } catch (Exception e) {
            LOG.error("Сбой", e);
        }
        return attachments;
    }

    private AbstractMessage getPicture(Integer groupId, Integer limit) throws ClientException, ApiException {

        GetResponse response = vkBotProperties.getVkClient().wall().get(vkBotProperties.getActor())
                .unsafeParam("v", vkBotProperties.getVersion())
                .ownerId(groupId)
                .offset(new Random().nextInt(limit))
                .count(1)
                .execute();

        Photo photo = response.getItems().get(0).getAttachments().get(0).getPhoto();
        String text = response.getItems().get(0).getText();
        if (text == null || text.isEmpty())
            text = helper.getRandomItem("Ну держи!", "Не баян (баян)", "Каеф", "&#127770;");

        String attach = "photo" + photo.getOwnerId() + "_" + photo.getId() + "_" + photo.getAccessKey();

        List<String> attachList = new ArrayList<>();
        attachList.add(attach);

        return new AbstractMessage(text, null, null, attachList, null, null, null);
    }
}

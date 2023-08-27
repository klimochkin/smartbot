package com.kobi.smartbot.model;

import com.kobi.smartbot.config.OpenAIProperties;
import com.kobi.smartbot.model.enums.MessageTypeEnum;
import com.kobi.smartbot.model.enums.SourceTypeEnum;

import java.util.List;
import java.util.Map;

public class AbstractMessage implements Cloneable {
    private Integer messageId;
    private String text;
    private Long userId;
    private MessageTypeEnum messageType;
    private List<String> attachments;
    private SourceTypeEnum sourceType;
    private Integer date;
    private String modelGpt;
    private String authorizationGpt;
    private String endPointGpt;
    private Long peerId;
    private boolean mode;
    private String subject;
    private String userName;
    private String fio;
    private String fromUserId;
    private boolean privateChat;
    private String systemMsg;

    public AbstractMessage(){
    }

    public AbstractMessage(String text, Long userId, MessageTypeEnum messageType, List<String> attachments, SourceTypeEnum sourceType, Integer date, String userName) {
        this.text = text;
        this.userId = userId;
        this.messageType = messageType;
        this.attachments = attachments;
        this.sourceType = sourceType;
        this.date = date;
        this.userName = userName;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public SourceTypeEnum getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceTypeEnum sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getModelGpt() {
        return modelGpt;
    }

    public void setModelGpt(String modelGpt) {
        this.modelGpt = modelGpt;
    }

    public String getAuthorizationGpt() {
        return authorizationGpt;
    }

    public void setAuthorizationGpt(String authorizationGpt) {
        this.authorizationGpt = authorizationGpt;
    }

    public String getEndPointGpt() {
        return endPointGpt;
    }

    public void setEndPointGpt(String endPointGpt) {
        this.endPointGpt = endPointGpt;
    }

    public Long getPeerId() {
        return peerId;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }

    public boolean isMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public void setGptImageConfig(OpenAIProperties.GptConfig gptImageConfig) {
        this.endPointGpt = gptImageConfig.getEndPoint();
        this.authorizationGpt = gptImageConfig.getAuthorization();
        this.modelGpt = gptImageConfig.getModel();
        this.text = applyFilter(this.getText(), gptImageConfig.getReplace());
    }

    public void setGptTextConfig(OpenAIProperties properties) {
        OpenAIProperties.GptConfig gptTextConfig;
        if (isMode()) {
            gptTextConfig = properties.getGpt4();
        } else {
            gptTextConfig = properties.getGpt3();
        }
        this.endPointGpt = gptTextConfig.getEndPoint();
        this.authorizationGpt = gptTextConfig.getAuthorization();
        this.modelGpt = gptTextConfig.getModel();
        this.systemMsg = gptTextConfig.getSystemMsg();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public boolean isPrivateChat() {
        return privateChat;
    }

    public void setPrivateChat(boolean privateChat) {
        this.privateChat = privateChat;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getSystemMsg() {
        return systemMsg;
    }

    public void setSystemMsg(String systemMsg) {
        this.systemMsg = systemMsg;
    }

    public AbstractMessage clone() {
        try {
            return (AbstractMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone AbstractMessage");
        }
    }

    private String applyFilter(String sourceText, Map<String, String> replacements) {
        for (Map.Entry<String,String> entry : replacements.entrySet()) {
            sourceText = sourceText.replace(entry.getKey(),entry.getValue());
        }
        return sourceText;
    }

}

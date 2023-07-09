package com.kobi.smartbot.repository.entity;


import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.Comment;
import com.kobi.smartbot.model.VkMessage;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class MessageJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_message")
    @SequenceGenerator(name = "seq_message", sequenceName = "seq_message", allocationSize = 1)
    private Long id;

    private String text;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "peer_id")
    private Long peerId;

    private String subject;

    @Column(name = "create_stamp")
    private LocalDateTime createStamp;

    @Column(name = "parent_message_id")
    private Integer parentMessageId;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "status")
    private String status;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "username")
    private String username;

    public MessageJpa() {
    }

    public MessageJpa(VkMessage msg, String msgText, String userRole) {
        this.text = msgText;
        this.userId = msg.getUserId();
        this.messageId = msg.getMessageId();
        this.peerId = msg.getPeerId();
        this.subject = msg.getSubject();
        this.createStamp = LocalDateTime.now();
        this.userRole = userRole;
        this.username = msg.getUserName();
        this.messageType = msg.getSourceType().name();
        this.status = "new";
    }

    public MessageJpa(Comment msg, String msgText, String userRole) {
        this.text = msgText;
        this.userId = msg.getUserId();
        this.messageId = msg.getCommentId();
        this.peerId = Long.valueOf(msg.getGroupId());
        this.subject = msg.getTopicId().toString();
        this.createStamp = LocalDateTime.now();
        this.userRole = userRole;
        this.username = msg.getUserName();
        this.messageType = msg.getSourceType().name();
        this.status = "new";
    }

    public MessageJpa(AbstractMessage msg, String msgText, String userRole) {
        this.text = msgText;
        this.userId = msg.getUserId();
        this.peerId = msg.getPeerId();
        this.createStamp = LocalDateTime.now();
        this.userRole = userRole;
        this.username = msg.getUserName();
        this.subject = msg.getSubject();
        this.messageType = msg.getSourceType().name();
        this.status = "new";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Long getPeerId() {
        return peerId;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getCreateStamp() {
        return createStamp;
    }

    public void setCreateStamp(LocalDateTime createStamp) {
        this.createStamp = createStamp;
    }

    public Integer getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(Integer parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
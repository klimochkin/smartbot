package com.kobi.smartbot.model;


import com.kobi.smartbot.model.enums.SourceTypeEnum;
import com.vk.api.sdk.objects.board.TopicComment;
import org.json.simple.JSONObject;

public class Comment extends AbstractMessage {
    private Long id;
    private int commentId;
    private Integer groupId;
    private Integer topicId;


    public Comment(TopicComment item, Integer groupId, Integer topicId){

        super(item.getText(), item.getFromId().longValue(), null, null, SourceTypeEnum.VK_GROUP, item.getDate(),null);

        this.commentId = item.getId();
        this.groupId = groupId;
        this.topicId = topicId;
    }


/*
    public Comment(JSONObject item){

        idComment = Integer.parseInt(item.get("id").toString());
     //   idUser = Long.parseLong(item.get("from_id").toString());
        text = item.get("text").toString();
        date = item.get("id").toString();
        attachments = null;
    }
*/
    public Comment(JSONObject item, String userName, Integer groupId, Integer topicId) {
        super(item.get("text").toString().toLowerCase().replace("\"", ""), Long.parseLong(item.get("from_id").toString()), null, null, SourceTypeEnum.VK_GROUP,null, userName);

        commentId = Integer.parseInt(item.get("id").toString());
        this.groupId = groupId;
        this.topicId = topicId;
        this.id = Long.parseLong(item.get("id").toString());

    }


/*
    public Comment(NewsfeedItem item, UserFull profile, GroupFull group){

        idComment = item.getSourceId();

        idUser = item.
                idUser = Long.parseLong(item.get("from_id").toString());
        text = item.get("text").toString();
        date = item.get("id").toString();
        attachments = null;
    }
*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }
}

package com.kobi.smartbot.model;


import com.kobi.smartbot.model.enums.SourceTypeEnum;

public class VkMessage extends AbstractMessage implements Cloneable{
    private Integer flags;
    private Long ts;
    private boolean forward;

    public VkMessage(){}

    public VkMessage(com.vk.api.sdk.objects.messages.Message msg){
        super(msg.getText().toLowerCase().replace("\"", ""),
                msg.getFromId().longValue(),
                null,
                null,
                SourceTypeEnum.VK_CHAT,
                msg.getDate(),
                null
        );
        this.setMessageId(msg.getId());
        this.setPeerId(Long.valueOf(msg.getPeerId()));
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }


    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    @Override
    public VkMessage clone() {
        return (VkMessage) super.clone();
    }
}


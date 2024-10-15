package com.easychat.entity.dto;

import com.easychat.entity.po.Message;
import com.easychat.entity.po.Session;
import com.easychat.entity.po.SessionUser;

import java.util.List;

public class WsInitData {
    private List<SessionUser> chatSessionList;

    private List<Message> chatMessageList;

    private Integer applyCount;

    public List<SessionUser> getChatSessionList() {
        return chatSessionList;
    }

    public void setChatSessionList(List<SessionUser> chatSessionList) {
        this.chatSessionList = chatSessionList;
    }

    public List<Message> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<Message> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}

package com.easychat.entity.vo;

import com.easychat.entity.po.Contact;
import com.easychat.entity.po.GroupInfo;

import java.util.List;

public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<Contact> contactList;

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }
}

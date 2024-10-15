package com.easychat.entity.dto;

import java.io.Serializable;

public class TokenUserInfoDto implements Serializable {

    private static final long serialVersionUID = -3244262035649152692L;

    private String token;
    private String userId;
    private String nickName;
    private Boolean admin;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}

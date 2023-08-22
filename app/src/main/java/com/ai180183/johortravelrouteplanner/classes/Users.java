package com.ai180183.johortravelrouteplanner.classes;

import java.util.HashMap;
import java.util.Map;

public class Users {
    private String userId;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String imgUrl;
    private Boolean isUser;

    public Users() {
    }

    public Users(String userId, String fullName, String email, String phone, String password, Boolean isUser, String imgUrl) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.isUser = isUser;
        this.imgUrl = imgUrl;
    }

    public Users (String fullName, String imgUrl) {
        this.fullName = fullName;
        this.imgUrl = imgUrl;
    }

    public Boolean getUser() {
        return isUser;
    }

    public void setUser(Boolean user) {
        isUser = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Map toMap() {
        HashMap namelist = new HashMap<>();
        namelist.put("name", this.fullName);
        namelist.put("profileImage",this.imgUrl);

        return namelist;
    }
}

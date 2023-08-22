package com.ai180183.johortravelrouteplanner.classes;

import java.util.Date;

public class TravelLog {
    private String logID;
    private String logTitle;
    private Date logDate;
    private String logContent;
    private String imgUrl;
    private String userEmail;
    private Boolean isUser;

    public TravelLog() {
    }

    public TravelLog(String logID, String logTitle, Date logDate, String logContent, String imgUrl,
                     String userEmail, Boolean isUser) {
        this.logID = logID;
        this.logTitle = logTitle;
        this.logDate = logDate;
        this.logContent = logContent;
        this.imgUrl = imgUrl;
        this.userEmail = userEmail;
        this.isUser = isUser;
    }

    public Boolean getUser() {
        return isUser;
    }

    public void setUser(Boolean isUser) {
        this.isUser = isUser;
    }

    public String getLogID() {
        return logID;
    }

    public void setLogID(String logID) {
        this.logID = logID;
    }

    public String getLogTitle() {
        return logTitle;
    }

    public void setLogTitle(String logTitle) {
        this.logTitle = logTitle;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}

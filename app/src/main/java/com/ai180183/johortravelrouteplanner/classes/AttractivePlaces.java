package com.ai180183.johortravelrouteplanner.classes;


import java.util.Date;

public class AttractivePlaces {
    private double lat;
    private double lng;
    private String placeID;
    private String placeName;
    private String placeDesc;
    private Date placeDate;
    private String placeImg;
    private String district;
    private String category;
    private int openingHour;
    private int closingHour;

    public AttractivePlaces(double lat, double lng, String placeID, String placeName, String placeDesc,
                            Date placeDate, String placeImg, String district, String category, int workStart, int workEnd) {
        this.lat = lat;
        this.lng = lng;
        this.placeID = placeID;
        this.placeDesc = placeDesc;
        this.placeName = placeName;
        this.placeDate = placeDate;
        this.placeImg = placeImg;
        this.district = district;
        this.category = category;
        this.openingHour = workStart;
        this.closingHour = workEnd;
    }

    public AttractivePlaces() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Date getPlaceDate() {
        return placeDate;
    }

    public void setPlaceDate(Date placeDate) {
        this.placeDate = placeDate;
    }

    public String getPlaceDesc() {
        return placeDesc;
    }

    public void setPlaceDesc(String placeDesc) {
        this.placeDesc = placeDesc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlaceImg() {
        return placeImg;
    }

    public void setPlaceImg(String placeImg) {
        this.placeImg = placeImg;
    }

    public int getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(int openingHour) {
        this.openingHour = openingHour;
    }

    public int getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(int closingHour) {
        this.closingHour = closingHour;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}

package com.ai180183.johortravelrouteplanner.classes;

import java.util.ArrayList;

public class FeaturedLocation {
    String ImgLink0,ImgLink1,ImgLink2,ImgLink3,ImgLink4;
    ArrayList<String> ImgList;

    public FeaturedLocation () {}

    public FeaturedLocation(String imgLink0, String imgLink1, String imgLink2, String imgLink3, String imgLink4, ArrayList<String> imgList) {
        ImgLink0 = imgLink0;
        ImgLink1 = imgLink1;
        ImgLink2 = imgLink2;
        ImgLink3 = imgLink3;
        ImgLink4 = imgLink4;
        ImgList = imgList;
    }

    public String getImgLink0() {
        return ImgLink0;
    }

    public void setImgLink0(String imgLink0) {
        ImgLink0 = imgLink0;
    }

    public String getImgLink1() {
        return ImgLink1;
    }

    public void setImgLink1(String imgLink1) {
        ImgLink1 = imgLink1;
    }

    public String getImgLink2() {
        return ImgLink2;
    }

    public void setImgLink2(String imgLink2) {
        ImgLink2 = imgLink2;
    }

    public String getImgLink3() {
        return ImgLink3;
    }

    public void setImgLink3(String imgLink3) {
        ImgLink3 = imgLink3;
    }

    public String getImgLink4() {
        return ImgLink4;
    }

    public void setImgLink4(String imgLink4) {
        ImgLink4 = imgLink4;
    }

    public ArrayList<String> getImgList() {
        return ImgList;
    }

    public void setImgList(ArrayList<String> imgList) {
        ImgList = imgList;
    }
}

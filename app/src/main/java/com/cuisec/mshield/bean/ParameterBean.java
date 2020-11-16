package com.cuisec.mshield.bean;

public class ParameterBean {

    /**
     *  .addParams("phone", "13391826151")
     *                     .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
     *                     .addParams("appid", "100002")
     *                     .addParams("province", "河南省")
     *                     .addParams("city", "郑州市")
     *                     .addParams("sourceId", "中国采购招标网")
     */


    private String phone;
    private String sign;
    private String appid;
    private String province;
    private String sourceId;
    private String city;
    private String title;
    private String bidType;
    private int pageNum;

    public String getBidType() {
        return bidType;
    }

    public void setBidType(String bidType) {
        this.bidType = bidType;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }


    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}

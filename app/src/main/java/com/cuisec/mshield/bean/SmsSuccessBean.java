package com.cuisec.mshield.bean;

public class SmsSuccessBean {

    /**
     * resultcode : 0
     * resultdesc : 发送成功，请验证短信验证码
     * phone : 133****6151
     * bindId : 1
     */

    private String resultcode;
    private String resultdesc;
    private String phone;
    private String bindId;

    public String getResultcode() {
        return resultcode;
    }

    public void setResultcode(String resultcode) {
        this.resultcode = resultcode;
    }

    public String getResultdesc() {
        return resultdesc;
    }

    public void setResultdesc(String resultdesc) {
        this.resultdesc = resultdesc;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }
}

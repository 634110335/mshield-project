package com.cuisec.mshield.bean;

public class IpassSuccessBean {

    /**
     * resultcode : 0
     * resultdesc : 邦定成功！
     * phone : null
     * bindId : null
     */

    private String resultcode;
    private String resultdesc;
    private Object phone;
    private Object bindId;

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

    public Object getPhone() {
        return phone;
    }

    public void setPhone(Object phone) {
        this.phone = phone;
    }

    public Object getBindId() {
        return bindId;
    }

    public void setBindId(Object bindId) {
        this.bindId = bindId;
    }
}

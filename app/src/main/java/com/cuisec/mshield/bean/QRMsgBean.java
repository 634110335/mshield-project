package com.cuisec.mshield.bean;

public class QRMsgBean {
    private String bizSn;
    private String action;
    private String cert;
    private String signAlg;
    private String signValue;
    private String id;
    private String saveid;

    public String getSaveid() {
        return saveid;
    }

    public void setSaveid(String saveid) {
        this.saveid = saveid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBizSn() {
        return bizSn;
    }

    public void setBizSn(String bizSn) {
        this.bizSn = bizSn;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getSignAlg() {
        return signAlg;
    }

    public void setSignAlg(String signAlg) {
        this.signAlg = signAlg;
    }

    public String getSignValue() {
        return signValue;
    }

    public void setSignValue(String signValue) {
        this.signValue = signValue;
    }
}

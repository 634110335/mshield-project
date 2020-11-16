package com.cuisec.mshield.bean;

public class QrScanBean {
    private String  cert;
    private String  certSn;
    private String  uuid;
    private String signdata;
    private String signsrc;
    private String userId;

    public String getSigndata() {
        return signdata;
    }

    public void setSigndata(String signdata) {
        this.signdata = signdata;
    }

    public String getSignsrc() {
        return signsrc;
    }

    public void setSignsrc(String signsrc) {
        this.signsrc = signsrc;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getCertSn() {
        return certSn;
    }

    public void setCertSn(String certSn) {
        this.certSn = certSn;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

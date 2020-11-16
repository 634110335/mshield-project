package com.cuisec.mshield.bean;

/**
 * Created by licz on 2018/6/6.
 */

public class CertInfoBean {
    private String cert;
    private String certSn;
    private String startDate;
    private String endDate;
    private String certIssuer;
    private String certSubject;

    public String getCert() {
        return this.cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getCertSn() {
        return this.certSn;
    }

    public void setCertSn(String certSn) {
        this.certSn = certSn;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCertIssuer() {
        return this.certIssuer;
    }

    public void setCertIssuer(String certIssuer) {
        this.certIssuer = certIssuer;
    }

    public String getCertSubject() {
        return this.certSubject;
    }

    public void setCertSubject(String certSubject) {
        this.certSubject = certSubject;
    }
}

package com.cuisec.mshield.fragment;

public class ReturnPayResult {
    private String status;
    private int flag;
    public ReturnPayResult(String status, int flag) {
        this.status = status;
        this.flag = flag;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}

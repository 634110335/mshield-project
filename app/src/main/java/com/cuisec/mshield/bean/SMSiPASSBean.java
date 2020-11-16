package com.cuisec.mshield.bean;

public class SMSiPASSBean {


    /**
     * hasSuccess : false
     * code : 500
     * msg : 服务器开小差了，请稍后再试！
     * data : null
     */

    private boolean hasSuccess;
    private String code;
    private String msg;
    private Object data;

    public boolean isHasSuccess() {
        return hasSuccess;
    }

    public void setHasSuccess(boolean hasSuccess) {
        this.hasSuccess = hasSuccess;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

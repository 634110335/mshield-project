package com.cuisec.mshield.bean;

import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.utils.L;

import java.net.URLDecoder;

public class QRCodeBean {
    private String appId;
    private String action;
    private String bizSn;
    private String url;
    private String msg;
    private String msgWrapper;
    private String desc;
    private String mode;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBizSn() {
        return bizSn;
    }

    public void setBizSn(String bizSn) {
        this.bizSn = bizSn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgWrapper() {
        return msgWrapper;
    }

    public void setMsgWrapper(String msgWrapper) {
        this.msgWrapper = msgWrapper;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void urlDecode() {
        try {
            if(appId != null && !appId.equals("")){
                appId = URLDecoder.decode(appId, Config.UTF_8);
            }
            if(action != null && !action.equals("")){
                action = URLDecoder.decode(action, Config.UTF_8);
            }
            if(bizSn != null && !bizSn.equals("")){
                bizSn = URLDecoder.decode(bizSn, Config.UTF_8);
            }
            if(url != null && !url.equals("")){
                url = URLDecoder.decode(url, Config.UTF_8);
            }
            if(msg != null && !msg.equals("")){
                msg = URLDecoder.decode(msg, Config.UTF_8);
            }
            if(desc != null && !desc.equals("")){
                desc = URLDecoder.decode(desc, Config.UTF_8);
            }
            if(mode != null && !mode.equals("")){
                mode = URLDecoder.decode(mode, Config.UTF_8);
            }
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }
}

package com.cuisec.mshield.bean;

public class QrMsgInfo {

    /**
     * ret : 0
     * msg : success
     * data : {"appId":"16","action":"login","bizSn":"202009011234567890","msg":"202009011234567890","msgWrapper":"0","url":"http://192.168.13.88:9097/bidding/checkQr","mode":"forward"}
     */

    private int ret;
    private String msg;
    private DataBean data;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * appId : 16
         * action : login
         * bizSn : 202009011234567890
         * msg : 202009011234567890
         * msgWrapper : 0
         * url : http://192.168.13.88:9097/bidding/checkQr
         * mode : forward
         */

        private String appId;
        private String action;
        private String bizSn;
        private String msg;
        private String msgWrapper;
        private String url;
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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }
}

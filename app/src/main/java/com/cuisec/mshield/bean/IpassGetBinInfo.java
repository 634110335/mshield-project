package com.cuisec.mshield.bean;

import java.util.List;

public class IpassGetBinInfo {

    /**
     * hasSuccess : true
     * code : 200
     * msg : 操作成功
     * data : [{"bindid":5,"phone":"13391826151","euserid":"ff8080816422642b0164227076ca0003","ipassAccount":"zhangxiaowei","ipassno":"CUS32100148263","ipassphone":"13391826151","channel":"LT","status":"2"},{"bindid":1,"phone":"13391826151","euserid":"4028b2816b457956016b458579550001","ipassAccount":"CUS32100148263","ipassno":"zhangxiaowei","ipassphone":"13391826151","channel":"HD","status":"2"}]
     */

    private boolean hasSuccess;
    private String code;
    private String msg;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * bindid : 5
         * phone : 13391826151
         * euserid : ff8080816422642b0164227076ca0003
         * ipassAccount : zhangxiaowei
         * ipassno : CUS32100148263
         * ipassphone : 13391826151
         * channel : LT
         * status : 2
         */

        private int bindid;
        private String phone;
        private String euserid;
        private String ipassAccount;
        private String ipassno;
        private String ipassphone;
        private String channel;
        private String status;

        public int getBindid() {
            return bindid;
        }

        public void setBindid(int bindid) {
            this.bindid = bindid;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEuserid() {
            return euserid;
        }

        public void setEuserid(String euserid) {
            this.euserid = euserid;
        }

        public String getIpassAccount() {
            return ipassAccount;
        }

        public void setIpassAccount(String ipassAccount) {
            this.ipassAccount = ipassAccount;
        }

        public String getIpassno() {
            return ipassno;
        }

        public void setIpassno(String ipassno) {
            this.ipassno = ipassno;
        }

        public String getIpassphone() {
            return ipassphone;
        }

        public void setIpassphone(String ipassphone) {
            this.ipassphone = ipassphone;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

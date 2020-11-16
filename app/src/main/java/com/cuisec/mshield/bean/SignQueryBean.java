package com.cuisec.mshield.bean;

/**
 * Created by licz on 2018/6/8.
 */

public class SignQueryBean extends BaseBean {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String bizSn;
        private String desc;

        public String getBizSn() {
            return bizSn;
        }

        public void setBizSn(String bizSn) {
            this.bizSn = bizSn;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}

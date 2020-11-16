package com.cuisec.mshield.bean;

public class PushBean {

    /**
     * hasSuccess : true
     * code : 200
     * msg : 操作成功
     * data : {"count":2,"nextUpdateDate":"2020-05-20 14:53:08"}
     */

    private boolean hasSuccess;
    private String code;
    private String msg;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * count : 2
         * nextUpdateDate : 2020-05-20 14:53:08
         */

        private int count;
        private String nextUpdateDate;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getNextUpdateDate() {
            return nextUpdateDate;
        }

        public void setNextUpdateDate(String nextUpdateDate) {
            this.nextUpdateDate = nextUpdateDate;
        }
    }
}

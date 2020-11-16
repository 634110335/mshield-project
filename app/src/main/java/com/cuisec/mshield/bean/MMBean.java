package com.cuisec.mshield.bean;

public class MMBean extends BaseBean {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Integer trust;

        public Integer getTrust() {
            return trust;
        }

        public void setTrust(Integer trust) {
            this.trust = trust;
        }
    }
}

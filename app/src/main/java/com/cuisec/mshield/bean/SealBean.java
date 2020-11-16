package com.cuisec.mshield.bean;

public class SealBean extends BaseBean {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        public String getSeal() {
            return seal;
        }

        public void setSeal(String seal) {
            this.seal = seal;
        }

        private String seal;

    }

    @Override
    public String toString() {
        return "SealBean{" +
                "data=" + data.getSeal() +
                '}';
    }
}
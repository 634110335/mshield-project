package com.cuisec.mshield.bean;

import java.util.List;

public class Citys {

    private List<ProvinceBean> province;
    private List<CityBean> city;
    private List<DistrictBean> district;

    public List<ProvinceBean> getProvince() {
        return province;
    }

    public void setProvince(List<ProvinceBean> province) {
        this.province = province;
    }

    public List<CityBean> getCity() {
        return city;
    }

    public void setCity(List<CityBean> city) {
        this.city = city;
    }

    public List<DistrictBean> getDistrict() {
        return district;
    }

    public void setDistrict(List<DistrictBean> district) {
        this.district = district;
    }

    public static class ProvinceBean {
        /**
         * text : 北京市
         * id : 110000
         */

        private String text;
        private String id;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class CityBean {
        /**
         * text : 市辖区
         * id : 110100
         */

        private String text;
        private String id;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class DistrictBean {
        /**
         * text : 东莞市
         * id : 441901
         * zipcode : 523000
         */

        private String text;
        private String id;
        private String zipcode;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getZipcode() {
            return zipcode;
        }

        public void setZipcode(String zipcode) {
            this.zipcode = zipcode;
        }
    }
}

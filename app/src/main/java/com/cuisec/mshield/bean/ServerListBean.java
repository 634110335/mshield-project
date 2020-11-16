package com.cuisec.mshield.bean;

import java.util.List;

public class ServerListBean extends BaseBean {
    private List<ServerDetail> data;

    public List<ServerDetail> getData() {
        return data;
    }

    public void setData(List<ServerDetail> data) {
        this.data = data;
    }

    public static class ServerDetail {
        private String code;
        private String name;
        private String url;
        private String logo;
        private String sdkUrl;
        private String ifMarked;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getSdkUrl() {
            return sdkUrl;
        }

        public void setSdkUrl(String sdkUrl) {
            this.sdkUrl = sdkUrl;
        }

        public String getIfMarked() {
            return ifMarked;
        }

        public void setIfMarked(String ifMarked) {
            this.ifMarked = ifMarked;
        }
    }
}

package com.cuisec.mshield.bean;

/**
 * Created by licz on 16/7/5.
 */
public class UserLoginBean extends BaseBean {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String token;
        private String refreshToken;
        private String loginEndTime;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getLoginEndTime() {
            return loginEndTime;
        }

        public void setLoginEndTime(String loginEndTime) {
            this.loginEndTime = loginEndTime;
        }
    }
}


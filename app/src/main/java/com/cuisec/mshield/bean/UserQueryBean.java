package com.cuisec.mshield.bean;

/**
 * Created by licz on 16/7/6.
 */
public class UserQueryBean extends BaseBean {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String userId;       //用户ID
        private String uuid;         //用户唯一标识
        private String userName;     //用户名称
        private String nickName;     //用户昵称
        private String userType;     //用户类型：1个人，2企业
        private String idNo;         //证件号码
        private String email;        //电子邮箱地址
        private String phone;        //电话号码
        private String phoneSn;      //手机序列号
        private String company;      //公司
        private String province;     //省份
        private String city;         //城市
        private String address;      //地址
        private String logo;         //用户头像图片（Base64编码）
        private String industryCode; //行业标识
        private String authStatus;   //认证状态（1:未认证 2:弱认证 3:强认证）
        private String certStatus;   //证书状态 (1:没证书 2:有证书 3:注销)
        private String createTime;   //创建时间
        private String wechatId;     //微信ID
        private String algVersion;   //密钥分割版本 1:三方 2:两方

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getIdNo() {
            return idNo;
        }

        public void setIdNo(String idNo) {
            this.idNo = idNo;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = idNo;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPhoneSn() {
            return phoneSn;
        }

        public void setPhoneSn(String phoneSn) {
            this.phoneSn = phoneSn;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getIndustryCode() {
            return industryCode;
        }

        public void setIndustryCode(String industryCode) {
            this.industryCode = industryCode;
        }

        public String getAuthStatus() {
            return authStatus;
        }

        public void setAuthStatus(String authStatus) {
            this.authStatus = authStatus;
        }

        public String getCertStatus() {
            return certStatus;
        }

        public void setCertStatus(String certStatus) {
            this.certStatus = certStatus;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getWechatId() {
            return wechatId;
        }

        public void setWechatId(String wechatId) {
            this.wechatId = wechatId;
        }

        public String getAlgVersion() {
            return algVersion;
        }

        public void setAlgVersion(String algVersion) {
            this.algVersion = algVersion;
        }
    }
}
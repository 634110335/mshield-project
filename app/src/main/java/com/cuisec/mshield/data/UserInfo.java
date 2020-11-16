package com.cuisec.mshield.data;

import java.io.Serializable;

/**
 * Created by licz on 16/7/5.
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public String userId;       //用户ID
    public String uuid;         //用户唯一标识
    public String userName;     //用户名称
    public String nickName;     //用户昵称
    public String userType;     //用户类型：1个人，2企业
    public String idNo;         //证件号码
    public String email;        //电子邮箱地址
    public String phone;        //电话号码
    public String company;      //公司
    public String province;     //省份
    public String city;         //城市
    public String address;      //地址
    public String logo;         //用户头像图片（Base64编码）
    public String industryCode; //行业标识
    public String authStatus;   //认证状态（1：未认证 2：弱认证 3强认证）
    public String wxId;         //微信ID
    public String createTime;   //创建时间
    public String algVersion;   //密钥分割版本 1:三方 2:两方
}

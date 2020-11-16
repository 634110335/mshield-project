package com.cuisec.mshield.config;

/**
 * Created by licz on 2018/6/4.
 */

public class Constants {

    //appId
    public static final String APP_ID = "100002";
    //public static final String APP_ID_IPASS = "105104";
    public static final String APP_ID_IPASS = "100002";
    // 客户端私钥
    public static final String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCILj7EvlvL81LnteSCUUhx5X8HKjlkI8iFJrAUpYXtfO7RRRry7svxvzS1d7UqXFCUCg8WtJKMCzTGtqWA9B4AzUt8d2SdptNvt/CfJO/rLBUkNQRrNzKRT4NRV+vkIHNdmY2aAw4yqpdtENsT7alKuV1Pd+072Mp09Cnp3Po8vgR4+/7/wOvR+t8sGi9vQgU1e3ANN2bnvbg5xDefJWYd1wEmWnR3uBRGx7fMIkYPtZooZP4cQ3OuS+KfVSujKRF61q7prkIRaALQqm+8WjYkhVP1u3xJh8H27tr9XBpHMnz/8dEUfWB6GduNAXfLFctYy4Tg6Ip3uaszQ6rZ09TRAgMBAAECggEAW4wDJE339gZwcFZl9HliIzctiwqGpFwt290jN+4CN6HqBgLX2AJSVgS9EVUVFBIVFUAh3pirf7u00fVNE4CIfu+D1HuKxzS0JQFZoBK7urPazolZnB3yP59dXxyk9qoeBAlLGQL+026891QCgsYdLZ+pK9gqQjY9/Bt75bxo+NWlQkqLoi9R6atQ6hIxSTHX+zuXLScMWliipNyJ4APFNB45OeNNdmdkDsf/6PfgIM8PFo30urL0mNaBQ6ev7ZnjVgDkLyg9goJQDawYr40aHmhLGwoqT884kQNbKs3WAuuNlw5PpvlWMvPrhsyU42sZdLAMasL3TeQWqKZ7T4+wlQKBgQDF8PBCbTfu/wT3PJ0sSfyK6ahDzsUN9K9+QN3rVQA2fjj7xw0myOrz9sD6vlZchUpW9wI9r7oem5p485Z+fT6SRf0k0n5Nm6ghzl6wEJG1tbMPRV4robxKuVFObTUPYio9hInhxJc4hcruaW1/s6swNPU324SvMH5iF4HRNnR38wKBgQCwH9DCR7A2+ibwT+1Y+/CAbopMjXtc1xy5XjOUzC2aueP8Ld4ez1p5OG3NyexsHR6Xa6wTHJD1wZlTt6C+BsEpB32PvP3r2xgcRxlW1lX7PnH9FRFAZbr5FBHXq7HrE7G95qLqJoztOW/dOCubExNw7UQuArz5Y8/ZZPEM4dBVKwKBgQCRDYFjjFoC4Aspite7DvTsb1IJbsrk8TxxjuORgxbG2DS1/SZcF7xlz5uXpEfxQRQ7KoQP5GjF6U2H+Sl9xBZIZtWdWEuKX9kT/QFSkLHWsTJorHLYEovDTo6QpGLPxSYjMq9ALSqMFy+hCs1dWbLHfud8aXeQUj8rbwKI6BjtlQKBgCfi7KozBgtNV6gw0bXHSM4tOpDfscJyzQSF+vSF0QYrdyBTWuxrVTgCuYtTYYGSjq0IK24gmAuwDoNRIMwoR3J9SchVqsPRWIJ+75Qk8k/18ma4cLG+2Ds0el33elGcojI5m0ef92Z6DYVyBR4Xkd2PPlOVNyrbmeTH4XdjZIUFAoGAW4xjRY3UnW1lCa270D3RMWQkEHltac14jrIoOFN9XEgjLUffdH9dB6dFkwuVNQ3XVVvZTk95dFluZ/atl0JOrYy/YuCu/rBKuCmpLTCGn4dQTyplaIovKRtL8ZOzOUezB01JK9NXyaurjzSVFl68Xem+vPASmMVaNnMFP0AcFx4=";

    // 小米推送
    public static final String MI_APP_ID = "2882303761517849028";
    public static final String MI_APP_KEY = "5551784919028";

    // 微信登录ID和密码
    public static final String WECHAT_APPID = "wx7917a0a88a4e7334";
    public static final String WECHAT_SECRET = "68a8314638f7e55ff2f5176b7e3ee449";

    // 微信平台网址
    public static final String wx_access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?";

    // SDKCode广播
    public static final String HOME_SDK_CODE_BROADCAST = "HOME_SDK_CODE_BROADCAST";
    // 用户更新广播
    public static final String MINE_UPDATA_BROADCAST = "MINE_UPDATA_BROADCAST";

    // 微信登录广播
    public static final String WECHAT_LOGIN_BROADCAST = "WECHAT_LOGIN_BROADCAST";

    // 实名认证状态: 1:未认证 2:弱认证 3:强认证 4:人工审核
    public static final String AUTH_STATUS_NONE = "1";
    public static final String AUTH_STATUS_WEAK = "2";
    public static final String AUTH_STATUS_STRONG = "3";

    // 短信发送状态 1:注册 2:登录 3:找回密码 4:修改手机号
    public static final String SMS_SEND_REGISTER = "1";
    public static final String SMS_SEND_LOGIN = "2";
    public static final String SMS_SEND_FORGET = "3";
    public static final String SMS_SEND_CHANGE_PHONE = "4";
    public static final String SMS_SEND_BIND_PHONE = "5";

    // 扫码的几种模式
    public static final String QRCODE_SIGN = "sign";     // 扫码签名
    public static final String QRCODE_LOGIN = "login";   // 扫码登录
    public static final String QRCODE_CONFIRM = "confirm"; // 扫码确认

    // 日志类型：1. 扫码登录; 2. 扫码签名; 3. 推送签名
    public static final String LOG_TYPE_1 = "1";
    public static final String LOG_TYPE_2 = "2";
    public static final String LOG_TYPE_3 = "3";
    // 使用证书的模式
    public static final String LOG_TYPE_DESC_1 = "扫码登录";
    public static final String LOG_TYPE_DESC_2 = "扫码签名";
    public static final String LOG_TYPE_DESC_3 = "推送签名";

    // 签名值推送模式
    public static final String SIGN_MODE_REDIRECT = "redirect"; // 由平台服务器转给应用
    public static final String SIGN_MODE_FORWARD = "forward";   // 由APP转给应用

    public static final String YYQ_ALG_VERSION_1 = "1";         // 密钥分割3方版本
    public static final String YYQ_ALG_VERSION_2 = "2";         // 密钥分割2方版本

    // 手势或指纹设置提醒取消时间
    public static final String LOCK_TIP_TIMES_SP = "LOCK_TIP_TIMES_SP";
    // 手势或指纹设置提醒间隔
    public static final int YYQ_LOCK_TIP_DAYS = 15;

    // 证书过期提醒时间设置
    public static final int CERT_EXPIRED_DAYS = 30;

    public static final String YYQ_LOGIN_TYPE_SP = "YYQ_LOGIN_TYPE_SP"; // 手机盾登录类型
    public static final String YYQ_TEMP_TEMP_SP = "YYQ_TEMP_TEMP_SP";   // 手机盾临时登录时长
    public static final Integer YYQ_LOGIN_TYPE_TEMP = 1;        // 临时登录
    public static final Integer YYQ_LOGIN_TYPE_OTHER = 2;       // 其它登录

    // 第三方APP签名返回页面
    public static final String THREE_APP_SIGN_RESP = "com.custle.activity.YYQRespActivity";

    public static final String SCHEME_ERR_SUCCESS       = "0";          // 成功
    public static final String SCHEME_ERR_LOGIN         = "1000";       // 未登录
    public static final String SCHEME_ERR_CERT_EXPIRED  = "1010";       // 证书失效
    public static final String SCHEME_ERR_CERT_NOT      = "1011";       // 证书获取失败
    public static final String SCHEME_ERR_SIGN          = "1020";       // 签名失败


    public static final int ORG_NOT_FIND = 1036;        // 无对应机构，微信登录绑定手机号

    public static final String LOG_APPLY_CERT      = "LOG_APPLY_CERT";      // 申请证书日志
    public static final String LOG_UPDATE_CERT      = "LOG_UPDATE_CERT";    // 更新证书日志
    public static final String LOG_REVOKE_CERT      = "LOG_REVOKE_CERT";    // 注销证书日志
    public static final String LOG_VERIFY_PIN      = "LOG_VERIFY_PIN";      // 验证密码日志
    public static final String LOG_SIGN_VERIFY      = "LOG_SIGN_VERIFY";    // 验证签名日志

    public static final String GET_CITY = "GET_CITY";
    public static final String GET_LOCATION_CITY = "GET_LOCATION_CITY";
    public static final String SAVE_USER_PHONE = "user_phone";
    public static final String WEB_URL = "WEB_URL";
    public static final String SOURCE_INFO = "SOURCE_INFO";
    public static final String GET_CITY_SID = "city_sid";


    //ipsss证书系列
    public static final String SAVE_PASS_BIND_PHONE = "user_phone";
    public static final String SAVE_PASS_BIND_TYPE = "bind_type";
}

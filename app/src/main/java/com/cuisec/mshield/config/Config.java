package com.cuisec.mshield.config;

import com.cuisec.mshield.data.SPManager;

/**
 * Created by licz on 2018/6/4.
 */

public class Config {
    // 全局的接口字符编码
    public static final String UTF_8 = "UTF-8";

    // 平台接口地址
    public static final String https_server_url = "https://center.mkeysec.net/v1";      // 正式环境
    public static final String https_server_url_T = "http://106.15.40.128:8081/v1";    // 测试环境
    //服务接口
    public static final String https_base_service_url = "https://61.138.142.30:38081/";//智零盾
     //public static final String http_base_service_url = "https://111.203.206.158:38888/";//测试环境
    public static final String http_base_service_url = "http://192.168.13.88:8082/";//测试环境

    // 平台接口
    public static final String https_base_version = SPManager.getCenterUrl() + "/base/version";
    public static final String https_base_app_list = SPManager.getCenterUrl() + "/base/appList";


    // 服务器方法
    // 短信方法
    public static final String sms_send = "/sms/send";
    public static final String sms_verify = "/sms/verify";
    public static final String sms_check = "/sms/check";

    // 用户方法
    public static final String user_register = "/user/register";
    public static final String user_login = "/user/login";
    public static final String user_sms_login = "/user/smslogin";
    public static final String user_refresh = "/user/refresh";
    public static final String user_logout = "/user/logout";
    public static final String user_recoverpwd = "/user/recoverpwd";
    public static final String user_changepwd = "/user/changepwd";
    public static final String user_query = "/user/query";
    public static final String user_update = "/user/update";
    public static final String user_changephone = "/user/changephone";
    public static final String user_code = "/user/code";
    public static final String user_seal = "/user/seal";
    public static final String user_check = "/user/check";  // 验证找回证书密码授权码
    public static final String user_certloginstatus = "/user/certloginstatus";  //设置服务器当前用户使用证书
    public static final String get_seal_tip_status = "/user/configseal";  // 提醒用户是否需要设置手写签名图片

    // 认证方法
    public static final String auth_id = "/auth/id";

    // 查询签名内容
    public static final String sign_query = "/sign/query";
    // 记录日志
    public static final String sign_log = "/sign/log";
    // 上传错误日志
    public static final String base_log = "/base/log";
    // 签名值转发
    public static final String sign_redirect = "/sign/redirect";
    // 免密签名设置
    public static final String sign_trust_config = "/sign/trust/config";
    public static final String sign_trust_clean = "/sign/trust/clean";

    // SDK授权码获取
    public static final String sdk_auth = "/sdk/auth";

    // log页面
    public static final String log_page = "/log/page";

    // 微信接口
    public static final String wx_user_register = "/wx/user/register";  // 微信注册
    public static final String wx_user_bind = "/wx/user/bind";          // 微信绑定
    public static final String wx_user_unbind = "/wx/user/unbind";      // 微信解绑
    public static final String wx_token = "/wx/token";                  // 微信登录

    public static final String service_app_list = "app/list";
    public static final String query_list = "bidinfo/list";
    public static final String query_checkcount = "bidinfo/checkcount";
    public static final String source_list = "bidinfo/sourceList";
    public static final String system_list = "system/provinceData/list";
    public static final String version_app = "app/versioninfo";
    public static final String ipass_user_bind = "ipassBus/userBind";
    public static final String ipass_sms_code = "ipassBus/sendCode";
    public static final String ipass_channel = "ipassBus/getChannel";
    public static final String ipass_get_bind = "ipassBus/getBind";
    public static final String ipass_get_order = "ipassBus/getIpassOrder";
    public static final String ipass_readed = "ipassBus/ipassNoticeInfo/readed";
    public static final String ipass_details = "ipassBus/orderTrack";

    //二维码扫码登录
    public static final String QR_CODE_LOGIN = "/wx/qrcode";

}

package com.netease.nim.uikit.common;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.common.http.NimHttpClient;
import com.netease.nim.uikit.impl.NimUIKitImpl;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 通讯录数据获取协议的实现
 * <p/>
 * Created by huangjun on 2015/3/6.
 */
public class ContactHttpClient {
    // code
    private static final int RESULT_CODE_SUCCESS = 200;
    public static final int RESULT_SUCCESS = 0;

    // api
    private static final String API_NAME_REGISTER = "createDemoUser";

    // header
    private static final String HEADER_KEY_APP_KEY = "appkey";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT = "Connection";
    private static final String HEADER_USER_AGENT = "User-Agent";

    // request
    private static final String REQUEST_USER_NAME = "username";
    private static final String REQUEST_NICK_NAME = "nickname";
    private static final String REQUEST_PASSWORD = "password";

    // result
    private static final String RESULT_KEY_RES = "res";
    private static final String RESULT_KEY_ERROR_MSG = "errmsg";
    private static final String RESULT_CODE = "res";


    public interface ContactHttpCallback<T> {
        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }

    private static ContactHttpClient instance;

    public static synchronized ContactHttpClient getInstance() {
        if (instance == null) {
            instance = new ContactHttpClient();
        }

        return instance;
    }

    private ContactHttpClient() {
        NimHttpClient.getInstance().init(NimUIKitImpl.getContext());
    }

    public void register(String account, String smsCode, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(1).append("&")
                .append("mode").append("=").append(2).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("account").append("=").append(account).append("&")
                .append("SmsCode").append("=").append(smsCode).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        nimHttpGetRequest(body.toString(), callback);
    }

    /**
     * 获取自动领取红包的参数
     *
     * @param uid
     * @param mytoken
     * @param qunID
     * @param callback
     */
    public void autoGetRedpacketInfoRequest(String uid, String mytoken, String qunID, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(55).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    /**
     * 设置自动领取红包的参数
     *
     * @param uid
     * @param mytoken
     * @param qunID
     * @param callback
     */
    public void autoGetRedpacketSwitchRequest(String uid, String mytoken, String qunID, int open, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(54).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("qunID").append("=").append(qunID).append("&")
                .append("open").append("=").append(open);
        nimHttpGetRequest(body.toString(), callback);
    }

    /**
     * 微信登录
     *
     * @param wxCode
     * @param callback
     */
    public void wxRegister(String wxCode, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(1).append("&")
                .append("mode").append("=").append(3).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("wxcode").append("=").append(wxCode).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        nimHttpGetRequest(body.toString(), callback);
    }

    /**
     * 绑定手机号
     *
     * @param uid
     * @param mytoken
     * @param accid
     * @param mobile
     * @param smscode
     * @param callback
     */
    public void bindPhoe(String uid, String mytoken, String accid, String mobile, String smscode, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(52).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(EncodeUtils.urlEncode(accid)).append("&")
                .append("mobile").append("=").append(mobile).append("&")
                .append("smscode").append("=").append(smscode).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        nimHttpGetRequest(body.toString(), callback);
    }

    /**
     * 客户端验证授权授权code
     *
     * @param callback
     */
    public void verificationCodeRequest(String uid, String mytoken, String accid, final ContactHttpCallback<String> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(50).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(EncodeUtils.urlEncode(accid)).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        String bodyString = body.toString();
        Log.e("xx", "bodyString=" + bodyString);
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                Log.e("xx", "response=" + response + " code=" + code);
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int resCode = jsonObject.getInt("code");
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(jsonObject.getString("token"));
                    } else {
                        callback.onFailed(resCode, null);
                    }
                } catch (Exception e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 客户端群主管理员同意邀请认证
     *
     * @param uid
     * @param mytoken
     * @param owner     管理员的accid
     * @param qunid
     * @param fromaccid 邀请人的accid
     * @param toaccid   被邀请人的accid
     * @param msgid
     * @param callback
     */
    public void agreeInvite(String uid, String mytoken, String owner, String qunid, String fromaccid, String toaccid, String msgid, String nickname, String qunname, final ContactHttpCallback<String> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(58).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("owner").append("=").append(owner).append("&")
                .append("qunid").append("=").append(qunid).append("&")
                .append("fromaccid").append("=").append(fromaccid).append("&")
                .append("toaccid").append("=").append(toaccid).append("&")
                .append("msgid").append("=").append(msgid).append("&")
                .append("nickname").append("=").append(EncodeUtils.urlEncode(nickname)).append("&")
                .append("qunname").append("=").append(EncodeUtils.urlEncode(qunname)).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        String bodyString = body.toString();
        Log.e("xx", "bodyString=" + bodyString);
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                Log.e("xx", "response=" + response + " code=" + code);
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int resCode = jsonObject.getInt("code");
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(jsonObject.toString());
                    } else {
                        callback.onFailed(resCode, "同意失败");
                    }
                } catch (Exception e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 57查询被邀请人是否需要同意
     *
     * @param uid
     * @param mytoken
     * @param owner    管理员的accid
     * @param qunid
     * @param callback
     */
    public void inviteeIsAgree(String uid, String mytoken, String owner, String qunid, final ContactHttpCallback<String> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(57).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("owner").append("=").append(owner).append("&")
                .append("qunid").append("=").append(qunid).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        String bodyString = body.toString();
        Log.e("xx", "bodyString=" + bodyString);
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                Log.e("xx", "response=" + response + " code=" + code);
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int resCode = jsonObject.getInt("code");
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(jsonObject.getString("onoff"));//1，被邀请人需要同意，0，被邀请人不需要同意
                    } else {
                        callback.onFailed(resCode, "");
                    }
                } catch (Exception e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 客户端查询是否有管理员同意邀请认证
     *
     * @param callback
     */
    public void queryInviteStatus(String uid, String mytoken, String msgid, final ContactHttpCallback<String> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(59).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("msgid").append("=").append(msgid).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        String bodyString = body.toString();
        Log.e("xx", "queryInviteStatus bodyString=" + bodyString);
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                Log.e("xx", "queryInviteStatus response=" + response + " code=" + code);
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(-1, errMsg);
                    }
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int resCode = jsonObject.getInt("code");
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(jsonObject.getString("agree"));
                    } else {
                        callback.onFailed(resCode, null);
                    }
                } catch (Exception e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    public void login(String account, String password, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(1).append("&")
                .append("mode").append("=").append(1).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("account").append("=").append(account).append("&")
                .append("pwd").append("=").append(password).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        nimHttpGetRequest(body.toString(), callback);
    }

    public void sendVerifyCode(String account, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(2).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("account").append("=").append(account).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        nimHttpGetRequest(body.toString(), callback);
    }

    public void changePassword(String account, String password, String SmsCode, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(3).append("&")
                .append("PlatFlag").append("=").append(0).append("&")
                .append("account").append("=").append(account).append("&")
                .append("pwd").append("=").append(password).append("&")
                .append("SmsCode").append("=").append(SmsCode).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        nimHttpGetRequest(body.toString(), callback);
    }

    public void getAuthParameter(String uid, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(6).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void saveAlipayAccount(String uid, String aliuid, String authcode, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(7).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("aliuid").append("=").append(aliuid).append("&")
                .append("authcode").append("=").append(authcode).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void queryAlipayAccount(String uid, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(5).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void bindAlipayAccount(String uid, String aliuid, String alinickname, String aliavatar, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(4).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("aliuid").append("=").append(aliuid).append("&")
                .append("alinickname").append("=").append(alinickname).append("&")
                .append("aliavatar").append("=").append(aliavatar).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void sendRedpacket(String uid, String mytoken, String touid, String nickname, float amount, int type, String content, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(8).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("touid").append("=").append(EncodeUtils.urlEncode(touid)).append("&")
                .append("amount").append("=").append(amount).append("&")
                .append("nickname").append("=").append(EncodeUtils.urlEncode(nickname)).append("&")
                .append("type").append("=").append(type).append("&")
                .append("content").append("=").append(EncodeUtils.urlEncode(content)).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void sendRedpacket(String uid, String mytoken, String touid, String nickname, float amount, int type, String qunID, String qunName, String masterUID, String content, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(8).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("touid").append("=").append(EncodeUtils.urlEncode(touid)).append("&")
                .append("amount").append("=").append(amount).append("&")
                .append("nickname").append("=").append(EncodeUtils.urlEncode(nickname)).append("&")
                .append("type").append("=").append(type).append("&")
                .append("qunID").append("=").append(qunID).append("&")
                .append("qunName").append("=").append(EncodeUtils.urlEncode(qunName)).append("&")
                .append("masterUID").append("=").append(masterUID).append("&")
                .append("content").append("=").append(EncodeUtils.urlEncode(content)).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void attestationPay(Activity activity, String uid, String mytoken, String response, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(9).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("response").append("=").append(EncodeUtils.urlEncode(response)).append("&")
                .append("mytoken").append("=").append(mytoken);
        okHttpGetRequest(activity, body.toString(), callback);
    }

    //私聊收红包
    public void receiveRedpacket(String uid, String mytoken, String orderno, String nickname, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(10).append("&")
                .append("nickname").append("=").append(EncodeUtils.urlEncode(nickname)).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("orderno").append("=").append(orderno).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }


    //群聊收红包
    public void receiveRedpacket(String uid, String mytoken, String orderno, String nickname, String qunID, String qunName, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(10).append("&")
                .append("nickname").append("=").append(EncodeUtils.urlEncode(nickname)).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("orderno").append("=").append(orderno).append("&")
                .append("qunID").append("=").append(qunID).append("&")
                .append("qunName").append("=").append(EncodeUtils.urlEncode(qunName)).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    public void queryRedpacketStatus(String uid, String mytoken, String orderno, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(12).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("orderno").append("=").append(orderno).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    //查询红包历史记录
    public void queryRedpacketRecord(String uid, String mytoken, int hisType, String date, int page, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(13).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("hisType").append("=").append(hisType).append("&")
                .append("date").append("=").append(date).append("&")
                .append("page").append("=").append(page);
        nimHttpGetRequest(body.toString(), callback);
    }

    //支付宝解绑
    public void cancelAlipayAuth(String uid, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(14).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    /**
     * 检查版本更新
     *
     * @param callback
     */
    public void queryAppVersion(final ContactHttpCallback<UpdateInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(15).append("&")
                .append("MachineID").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getModel()).append("&")
                .append("os").append("=").append(1).append("&")
                .append("OSVer").append("=").append(String.valueOf(Build.VERSION.SDK_INT)).append("&")
                .append("version").append("=").append(AppUtils.getAppVersionName());
        //body.append("api_token").append("=").append(api_token);
        String bodyString = body.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                Log.e("xx", "update request=" + bodyString);
                Log.e("xx", "update response=" + response);
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    UpdateInfo updateInfo = gson.fromJson(response, new TypeToken<UpdateInfo>() {
                    }.getType());
                    if (updateInfo.getCode() == 0) {
                        callback.onSuccess(updateInfo);
                    } else {
                        callback.onFailed(-1, "更新地址为空");
                    }
                } catch (Exception e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    //查询按月统计
    public void queryAlipayMonth(String uid, String mytoken, String date, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(20).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("date").append("=").append(date);
        nimHttpGetRequest(body.toString(), callback);
    }

    //获取检索开关状态
    public void querySearchingSwitch(String uid, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(19).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken);
        nimHttpGetRequest(body.toString(), callback);
    }

    //开关检索功能
    public void setSearchingSwitch(String uid, String mytoken, int flag, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(18).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("flag").append("=").append(flag);
        nimHttpGetRequest(body.toString(), callback);
    }

    //修改空了吹号
    public void changeYchatAccount(String uid, String mytoken, String ychatNo, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(21).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("ychatNo").append("=").append(ychatNo);
        nimHttpGetRequest(body.toString(), callback);
    }

    //获取空了吹号
    public void getYchatAccount(String uid, String mytoken, String accid, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(22).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(accid);
        nimHttpGetRequest(body.toString(), callback);
    }

    //客户端上报头像、昵称、性别等资料变化 1 头像 2 昵称 3 性别
    public void updateUserInfo(String uid, String mytoken, int type, String content, final ContactHttpCallback<RequestInfo> callback) {
        String typeString;
        if (type == 1) {
            typeString = "icon";
        } else if (type == 2) {
            typeString = "nickname";
        } else {
            typeString = "gender";
        }
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(16).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append(typeString).append("=").append(EncodeUtils.urlEncode(content));
        nimHttpGetRequest(body.toString(), callback);
    }

    //获取检索开关状态
    public void querySearching(String uid, String mytoken, int mode, String key, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(17).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("mode").append("=").append(mode).append("&")
                .append("key").append("=").append(EncodeUtils.urlEncode(key));
        nimHttpGetRequest(body.toString(), callback);
    }

    //加入（退出）群
    public void updateTeamMemberChange(String uid, String mytoken, String DoAccID, int action, String qunID, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(24).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("DoAccID").append("=").append(EncodeUtils.urlEncode(DoAccID)).append("&")
                .append("action").append("=").append(action).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //加入群
    public void addTeamMemberChangeMethod(String uid, String mytoken, String qunid, String accid, String note, final ContactHttpCallback<TeamMethodInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(48).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(EncodeUtils.urlEncode(accid)).append("&")
                .append("type").append("=").append(1).append("&")
                .append("note").append("=").append(EncodeUtils.urlEncode(note)).append("&")
                .append("qunid").append("=").append(qunid);
        String bodyString = body.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    TeamMethodInfo loginInfo = gson.fromJson(response, new TypeToken<TeamMethodInfo>() {
                    }.getType());
                    int resCode = loginInfo.getCode();
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(loginInfo);
                    } else {
                        callback.onFailed(resCode, null);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    //退出群
    public void removeTeamMemberChangeMethod(String uid, String mytoken, String qunid, String accid, final ContactHttpCallback<TeamMethodInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(48).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(EncodeUtils.urlEncode(accid)).append("&")
                .append("type").append("=").append(3).append("&")
                .append("qunid").append("=").append(qunid);
        String bodyString = body.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    TeamMethodInfo loginInfo = gson.fromJson(response, new TypeToken<TeamMethodInfo>() {
                    }.getType());
                    int resCode = loginInfo.getCode();
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(loginInfo);
                    } else {
                        callback.onFailed(resCode, null);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    //查询进群方式
    public void queryTeamMemberChangeMethod(String uid, String mytoken, String qunid, String accid, final ContactHttpCallback<TeamMethodInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(48).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(EncodeUtils.urlEncode(accid)).append("&")
                .append("type").append("=").append(2).append("&")
                .append("qunid").append("=").append(qunid);
        String bodyString = body.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    TeamMethodInfo loginInfo = gson.fromJson(response, new TypeToken<TeamMethodInfo>() {
                    }.getType());
                    int resCode = loginInfo.getCode();
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(loginInfo);
                    } else {
                        callback.onFailed(resCode, null);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    //创建群或者解散群  action=1 代表创建  Action=2 代表解散
    public void updateTeamStatusService(String uid, String mytoken, int action, String qunID, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(23).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("action").append("=").append(action).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //群记录（入群 退群日志）
    public void teamMemberChangeLog(String uid, String mytoken, String qunID, int page, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(25).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("page").append("=").append(page).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //更新群成员发言时间戳
    public void updateMemberChatTime(String uid, String mytoken, long time, String qunID, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(26).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("time").append("=").append(time).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //获取一段时间不发言的玩家列表
    public void fetchChatTimeMemberList(String uid, String mytoken, long time1, long time2, String qunID, int page, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(27).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("time1").append("=").append(time1).append("&")
                .append("time2").append("=").append(time2).append("&")
                .append("page").append("=").append(page).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //清除群记录（入群 退群日志）
    public void clearTeamMemberChangeLog(String uid, String mytoken, String qunID, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(30).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //获取红包操作禁止列表
    public void fetchPreventRedpacketMemberList(String uid, String mytoken, String qunID, int page, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(29).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("page").append("=").append(page).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //设置能否发送接收红包
    public void updatePreventRedpacketMemberList(String uid, String mytoken, String qunID, int IsAdd, String DoAccID, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(28).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("IsAdd").append("=").append(IsAdd).append("&")
                .append("DoAccID").append("=").append(EncodeUtils.urlEncode(DoAccID)).append("&")
                .append("qunID").append("=").append(qunID);
        nimHttpGetRequest(body.toString(), callback);
    }

    //查询红包发送权限开关状态
    public void querySendRedpacket(String uid, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(35).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("MachineName").append("=").append(DeviceUtils.getAndroidID()).append("&")
                .append("os").append("=").append(1);
        nimHttpGetRequest(body.toString(), callback);
    }

    //查询未领取红包
    public void queryUnclaimedRedpacket(String uid, String mytoken, String qunID, int timeout, int page, final ContactHttpCallback<TeamMethodInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(37).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("qunID").append("=").append(qunID).append("&")
                .append("timeout").append("=").append(timeout).append("&")
                .append("page").append("=").append(page);
        String bodyString = body.toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    TeamMethodInfo loginInfo = gson.fromJson(response, new TypeToken<TeamMethodInfo>() {
                    }.getType());
                    int resCode = loginInfo.getCode();
                    if (resCode == RESULT_SUCCESS) {
                        callback.onSuccess(loginInfo);
                    } else {
                        callback.onFailed(resCode, null);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    //查询所有机器人
    public void queryAllRobot(String uid, String mytoken, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(42).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("type").append("=").append(1);
        nimHttpGetRequest(body.toString(), callback);
    }

    //查询群里机器人
    public void queryTeamRobot(String uid, String mytoken, String qunid, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(42).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("type").append("=").append(2).append("&")
                .append("qunid").append("=").append(qunid);
        nimHttpGetRequest(body.toString(), callback);
    }

    //删除群里机器人
    public void removeTeamRobot(String uid, String mytoken, String qunid, String accid, String ownerid, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(44).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(accid).append("&")
                .append("ownerid").append("=").append(ownerid).append("&")
                .append("qunid").append("=").append(qunid);
        nimHttpGetRequest(body.toString(), callback);
    }

    //添加群里机器人
    public void addTeamRobot(String uid, String mytoken, String qunid, String accid, String ownerid, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(43).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("accid").append("=").append(accid).append("&")
                .append("ownerid").append("=").append(ownerid).append("&")
                .append("qunid").append("=").append(qunid);
        nimHttpGetRequest(body.toString(), callback);
    }

    //查询红包是否支付成功
    public void verifyPaymentResult(String uid, String mytoken, String orderno, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.alipayServer());
        body.append("cmd").append("=").append(53).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("orderno").append("=").append(orderno);
        nimHttpGetRequest(body.toString(), callback);
    }

    //更新群头像
    public void updateGroupHead(String uid, String mytoken, String owner, String qunid, String headurl, final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(63).append("&")
                .append("uid").append("=").append(uid).append("&")
                .append("mytoken").append("=").append(mytoken).append("&")
                .append("owner").append("=").append(owner).append("&")
                .append("qunid").append("=").append(qunid).append("&")
                .append("headurl").append("=").append(EncodeUtils.urlEncode(headurl));
        nimHttpGetRequest(body.toString(), callback);
    }

    //获取小助手账号
    public void fetchAssistantAccount(final ContactHttpCallback<RequestInfo> callback) {
        StringBuilder body = new StringBuilder(DemoServers.apiServer());
        body.append("cmd").append("=").append(64);
        nimHttpGetRequest(body.toString(), callback);
    }

    private void nimHttpGetRequest(String bodyString, final ContactHttpCallback<RequestInfo> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        NimHttpClient.getInstance().execute(bodyString, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    RequestInfo info = gson.fromJson(response, new TypeToken<RequestInfo>() {
                    }.getType());
                    Log.e("xx", "info=" + info.toString());
                    int resCode = info.getCode();
                    if (resCode == RESULT_SUCCESS || resCode == 100020 || resCode == 100039) {
                        callback.onSuccess(info);
                    } else {
                        callback.onFailed(resCode, info.getErr_msg());
                    }
                } catch (Exception e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    private void okHttpGetRequest(Activity activity, String bodyString, final ContactHttpCallback<RequestInfo> callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryIntercepter(12))
                .build();
        Request request = new Request.Builder().get().url(bodyString).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.runOnUiThread(() -> {
                    callback.onFailed(-1, e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    Gson gson = new Gson();
                    final String responseStr = response.body().string();
                    RequestInfo loginInfo = gson.fromJson(responseStr, new TypeToken<RequestInfo>() {
                    }.getType());
                    int resCode = loginInfo.getCode();
                    if (resCode == RESULT_SUCCESS) {
                        activity.runOnUiThread(() -> {
                            callback.onSuccess(loginInfo);
                        });
                    } else {
                        activity.runOnUiThread(() -> {
                            callback.onFailed(resCode, null);
                        });
                    }
                } catch (Exception e) {
                    activity.runOnUiThread(() -> {
                        callback.onFailed(-1, e.getMessage());
                    });
                }
            }
        });
    }

    public class RetryIntercepter implements Interceptor {
        public int maxRetry;
        private int retryNum = 0;

        public RetryIntercepter(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            while (!response.isSuccessful() && retryNum < maxRetry) {
                retryNum++;
                response = chain.proceed(request);
            }
            return response;
        }
    }

}

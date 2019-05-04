package com.netease.nim.uikit.common;

public class DemoServers {

    //    private static final String API_SERVER_TEST = "http://39.105.147.173:8989/?"; // 线上
//    private static final String API_SERVER_ALIPAY = "http://39.105.147.173:8686/?"; // 线上
    //    private static final String API_SERVER_TEST = "http://39.106.41.0:8989/?"; // 线上
    //   private static final String API_SERVER_ALIPAY = "http://39.106.41.0:8686/?"; // 线上
    private static final String API_SERVER_TEST = "http://logon.yaoliaoapp.com:8989/?"; // 线上
    private static final String API_SERVER_ALIPAY = "http://pay.yaoliaoapp.com:8686/?"; // 线上
    private static final String LOCAL_AUTH = "http://192.168.1.223:8989/?"; // 本地微信登录授权测试
    private static final String LOCAL_SERVER_ALIPAY = "http://192.168.1.223:8686/?"; // 本地支付宝相关接口

    public static String apiServer() {
        return API_SERVER_TEST;
        //return LOCAL_AUTH;
    }

    public static String alipayServer() {
        return API_SERVER_ALIPAY;
        //return LOCAL_SERVER_ALIPAY;
    }

}

package com.potflesh.wenda.utils;

public class HttpStatusCode {
    // GET、PUT、Create、Modified 成功请求，并有正确回复
    public static int SUCCESS_STATUS = 200;
    // 回复内容为空
    public static int NO_CONTENT = 201;
    // token 已过期
    public static int Unauthorized = 401;
    // 服务器出现错误
    public static int SERVIC_ERROR = 999;
    // 传入参数错误
    public static int REQUEST_PARAMARY_ERROR = 401;
}

package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.Getter;

public enum MiniProgramApi {


    JSCODE2SESSION("APPID,SECRET,JSCODE",
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            "GET",
            "登录凭证校验"
    ),



    ACCESS_TOKEN("APPID, SECRET",
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
            "GET",
            "获取access_token"),


    MESSAGE_SEND("ACCESS_TOKEN",
            "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=%s",
            "POST",
            "发送模板消息"),

    PIC_CODE_UNLIMITED("ACCESS_TOKEN",
                         "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s",
                         "POST",
                         "获取小程序码");


    @Getter
    private String param;
    @Getter
    private String url;
    @Getter
    private String method;
    @Getter
    private String name;

    MiniProgramApi(String param, String url, String method, String name) {
        this.param = param;
        this.name = name;
        this.url = url;
        this.method = method;
    }


    public String url(Object... obj){
        return String.format(url, obj);

    }
}

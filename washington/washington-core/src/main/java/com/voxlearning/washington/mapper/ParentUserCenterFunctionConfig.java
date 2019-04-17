package com.voxlearning.washington.mapper;

import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Data;

/**
 * @author jiangpeng
 */
@Data
public class ParentUserCenterFunctionConfig {
    public ParentUserCenterFunctionConfig() {
    }

    private String title;   //功能标题
    private Integer order;   //功能顺序
    private String type;      //ParentUserCenterNativeFunction.name 对应
    private String url;        //跳转的url 不带主站地址。
    private Boolean needLogin;  //是否需要登录
    private String iconUrl;   //图标地址 带cdn的绝对地址

    private String startVersion; //某个版本开始显示 包括这个版本
    private String endVersion; //某个版本之前线上,不包括这个版本

    private String grayMain;  //灰度
    private String graySub;   //灰度

    private Boolean  iosAuthNoShow; //屏蔽ios送审帐号

    private String remindingId;   //提醒的id
    private String remindingType;   //提醒的类型 ParentUserCenterFunctionRemindingType
    private String remindingText;   //提醒文案`
    private Integer remindingNumber;  //提醒数字
    private Boolean functionRemindingForever;   //是否永久显示提醒,不受提醒id是否点过限制。


    public Boolean safeIosAuthNoShow(){
        return SafeConverter.toBoolean(iosAuthNoShow);
    }

}

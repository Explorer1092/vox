package com.voxlearning.washington.controller.thirdparty.base;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author chongfeng.qi
 * @data 20181204
 *
 * 第三方登录用户类
 */
@Getter
@Setter
public class VendorUserContext implements java.io.Serializable{

    private String vendorUserId; // vendor用户id

    private String userName; // 用户姓名

    private String mobile; // 用户手机号

    private Long userId; // 17用户

    private Boolean isBand; // 是否绑定

    private Boolean isRegister; // 是否注册

    private String code; // 获取token的code;

    private String token;

    private String appKey; // 当前应用标识

    private UserType userType; // 用户类型

    private List<Ktwelve> ktwelve; // 学段

    private Map<String, Object> data;

    private MapMessage mapMessage;

    public boolean fetchIsBand() {
        return SafeConverter.toBoolean(isBand);
    }

    public boolean fetchIsRegister() {
        return SafeConverter.toBoolean(isRegister);
    }
}

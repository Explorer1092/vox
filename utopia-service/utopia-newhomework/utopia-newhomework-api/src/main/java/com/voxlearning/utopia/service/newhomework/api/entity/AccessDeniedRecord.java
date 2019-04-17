package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_ACCESS_DENIED_RECORD")
public class AccessDeniedRecord extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -3775876181796336377L;

    @DocumentField("USER_ID") private Long userId;                          // 用户id
    @DocumentField("REQUEST_PATH") private String requestPath;              // 请求路径
    @DocumentField("LIMITATION_KEY") private String limitationKey;          // 限制的缓存key
    @DocumentField("LIMITATION_VALUE") private Integer limitationValue;     // 限制的缓存value
    @DocumentField("IP") private String ip;                                 // 访问ip
    @DocumentField("USER_AGENT") private String userAgent;                  // ua
    @DocumentField("IMEI") private String imei;                             // imei
    @DocumentField("UUID") private String uuid;                             // uuid
    @DocumentField("SYS") private String sys;                               // 系统（ANDROID、IOS）
    @DocumentField("APP_VERSION") private String appVersion;                // 客户端版本
    @DocumentField("MOBILE_REQUEST") private Boolean mobileRequest;         // 是否移动端请求

}

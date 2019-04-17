package com.voxlearning.utopia.service.crm.api.entities.crm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


/**
 * @author fugui.chang
 * @since 2016/11/22
 */

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_WORKFLOW_WECHAT_MESSAGE")
@UtopiaCacheRevision("20161122")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@NoArgsConstructor
@AllArgsConstructor
public class WechatWfMessage extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -7118431932746967356L;

    @UtopiaSqlColumn private String wechatType;           // 发送端
    @UtopiaSqlColumn private String firstInfo;            // 模板消息内容： 消息主题
    @UtopiaSqlColumn private String keyword1;             // 模板消息内容： keyword1
    @UtopiaSqlColumn private String keyword2;             // 模板消息内容： keyword2
    @UtopiaSqlColumn private String remark;               // 模板消息内容： 备注
    @UtopiaSqlColumn private String url;                  // 模板消息内容： 跳转链接
    @UtopiaSqlColumn private Date sendTime;               // 发送时间
    @UtopiaSqlColumn private Boolean isUstalk;            // 是否USTalk专用
    @UtopiaSqlColumn private String isWkt;       // 是否微课堂专用
    @UtopiaSqlColumn private String noticeType;           // 消息类型
    @UtopiaSqlColumn private Integer sendType;            // 用户类型，1-按ID发送，2-按附件发送
    @UtopiaSqlColumn private String fileUrl;              // 附件地址
    @UtopiaSqlColumn private String userIds;              // 用json的格式存用户id 例如 {1,2,3} {1}
    @UtopiaSqlColumn private Long recordId;               // 对应WorkFlowRecord的id
    @UtopiaSqlColumn private String status;               // 待审核状态

    @JsonIgnore
    public boolean isUtk() {
        return Boolean.TRUE.equals(isUstalk);
    }


    public UserType fetchUserType() {
        if (StringUtils.isBlank(wechatType)) {
            return null;
        }
        switch (wechatType) {
            case "PARENT":
                return UserType.PARENT;
            case "TEACHER":
                return UserType.TEACHER;
            default:
                return null;
        }

    }
}

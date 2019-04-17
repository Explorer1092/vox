

package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 新的模板消息记录表
 */
@Getter
@Setter
@DocumentConnection(configName = "wechat")
@DocumentTable(table = "VOX_WECHAT_TEMPLATE_MESSAGE_RECORD")
public class WechatTemplateMessageRecord extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 63695344319881637L;
    @UtopiaSqlColumn(name = "USER_ID") Long userId;
    @UtopiaSqlColumn(name = "OPEN_ID") String openId;
    @UtopiaSqlColumn(name = "WECHAT_TYPE") Integer wechatType;
    @UtopiaSqlColumn(name = "MESSAGE_ID") String messageId;
    @UtopiaSqlColumn(name = "MESSAGE") String message;
    @UtopiaSqlColumn(name = "MESSAGE_TYPE") Integer messageType;
    @UtopiaSqlColumn(name = "STATE") WechatNoticeState state;
    @UtopiaSqlColumn(name = "ERROR_CODE") String errorCode;

    public static WechatTemplateMessageRecord newInstance(Long userId,
                                                          String openId,
                                                          String message,
                                                          WechatTemplateMessageType messageType,
                                                          WechatNoticeState state) {
        if (userId == null) throw new NullPointerException();
        if (openId == null) throw new NullPointerException();
        if (message == null) throw new NullPointerException();
        if (messageType == null) throw new NullPointerException();
        if (state == null) throw new NullPointerException();
        WechatTemplateMessageRecord inst = new WechatTemplateMessageRecord();
        inst.setDisabled(false);
        inst.setUserId(userId);
        inst.setOpenId(openId);
        inst.setMessage(StringUtils.filterEmojiForMysql(message));
        inst.setMessageType(messageType.getType());
        inst.setState(state);
        inst.setWechatType(messageType.getWechatType().getType());
        inst.setCreateDatetime(new Date());
        inst.setUpdateDatetime(new Date());
        return inst;
    }
}

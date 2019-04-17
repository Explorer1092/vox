package com.voxlearning.utopia.service.crm.tools;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;

import java.util.List;

public class WechatWorkFlowUtils {

    public static MapMessage validateMessage(WechatWfMessage wechatMessage) {
        if (wechatMessage == null) {
            return MapMessage.errorMessage("开玩喜呢，啥都没有");
        }

        String wechatType = wechatMessage.getWechatType();
        if (StringUtils.isBlank(wechatType) || ("PARENT".equals(wechatType) && "TEACHER".equals(wechatType))) {
            return MapMessage.errorMessage("请选择正确的消息发送端");
        }

        String noticeType = wechatMessage.getNoticeType();
        if (StringUtils.isBlank(noticeType)) {
            return MapMessage.errorMessage("请选择正确的消息发送端");
        }

        if (StringUtils.isBlank(wechatMessage.getKeyword1()) && StringUtils.isBlank(wechatMessage.getKeyword2())) {
            return MapMessage.errorMessage("Keyword请至少填写一项");
        }

        int sendType = wechatMessage.getSendType();
        if (sendType != 1 && sendType != 2) {
            return MapMessage.errorMessage("请选择有效的用户信息");
        }
        // 指定用户投放，不超过30个，因为受限于数据库字段长度
        if (sendType == 1) {
            List<Long> userIds = JsonStringDeserializer.getInstance().deserializeList(wechatMessage.getUserIds(), Long.class);
            if (CollectionUtils.isEmpty(userIds)) {
                return MapMessage.errorMessage("请填写正确的用户ID");
            }
            if (userIds.size() > 30) {
                return MapMessage.errorMessage("超过30名用户请使用附件上传用户ID");
            }
        }

        // 使用附件上传
        if (sendType == 2) {
            if (StringUtils.isBlank(wechatMessage.getFileUrl())) {
                return MapMessage.errorMessage("无效的文件路径");
            }
        }

        return MapMessage.successMessage();
    }

    public static String generateContent(WechatWfMessage wechatMessage) {
        if (wechatMessage == null) {
            return "";
        }
        StringBuilder content = new StringBuilder();

        String wechatType = wechatMessage.getWechatType();
        if ("PARENT".equals(wechatType)) {
            content.append("发送端：微信家长通<br/>");
        } else if ("TEACHER".equals(wechatType)) {
            content.append("发送端：微信老师端<br/>");
        }

        content.append("内容:");
        if (StringUtils.isNotBlank(wechatMessage.getKeyword1())) {
            content.append(wechatMessage.getKeyword1()).append(";");
        }
        if (StringUtils.isNotBlank(wechatMessage.getKeyword2())) {
            content.append(wechatMessage.getKeyword2()).append(";");
        }
        if (StringUtils.isNotBlank(wechatMessage.getRemark())) {
            content.append(wechatMessage.getRemark()).append(";");
        }
        content.append("<br/>");

        return content.toString();
    }

}

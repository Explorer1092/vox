package com.voxlearning.utopia.service.crm.tools;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.constants.crm.AppPushMsgConstants;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;

import java.util.*;
import java.util.stream.Collectors;

public class AppPushWorkFlowUtils {

    public static MapMessage validateMessage(AppPushWfMessage appPushMsg) {
        if (appPushMsg == null) {
            return MapMessage.errorMessage("开玩喜呢，啥都没有");
        }
        //这里后来加的。环信消息。这个验证直接过
        if ("easeMob".equals(appPushMsg.getUserType())) {
            return MapMessage.successMessage();
        }

        // 0. 发送时间必填
        if (appPushMsg.getSendTime() == null) {
            return MapMessage.errorMessage("请选择发送时间");
        }

        // 0.1 跳转URL不能有两个[?], 就算参数需要带 url 自己先去encode
//        if (appPushMsg.getLink() != null && StringUtils.countMatches(appPushMsg.getLink(), "?") > 1) {
//            return MapMessage.errorMessage("无效的跳转链接");
//        }

        // 1. 学段必填
        // 没有学段的事儿了，以前弄这个多余
        /*if (validateKtwelve(appPushMsg.getKtwelve())) {
            return MapMessage.errorMessage("请选择学段");
        }*/

        // 2. 校验是否勾选了发送类型
        if (!appPushMsg.canSendPush() && !appPushMsg.canSendMsg()) {
            return MapMessage.errorMessage("什么都不发， 你要干嘛 ？");
        }

        // 3. 校验是否选择了有效的发送端
        UserType userType = appPushMsg.fetchTargetUserType();
        if (userType == null) {
            return MapMessage.errorMessage("无效的发送端：" + appPushMsg.getUserType() + ", idType=" + appPushMsg.getIdType());
        }

        // 4. 校验是否选择了有效的发送类型
        int pushType = appPushMsg.getPushType();
        if (!AppPushMsgConstants.validPushType.contains(pushType)) {
            return MapMessage.errorMessage("暂不支持该类型发送: " + pushType);
        }

        // 5. 投放指定用户时
        if (AppPushMsgConstants.TargetUser == pushType) {
            // 检查附件和用户ID只能支持一种模式
            if (StringUtils.isBlank(appPushMsg.getFileUrl()) && CollectionUtils.isEmpty(appPushMsg.getTargetUser())) {
                return MapMessage.errorMessage("请上传附件，或者填写用户ID");
            }
            if (StringUtils.isNotBlank(appPushMsg.getFileUrl()) && CollectionUtils.isNotEmpty(appPushMsg.getTargetUser())) {
                return MapMessage.errorMessage("填写用户ID和上传附件请谨慎选择其中一种方式");
            }
            // 用户ID不能超过100条，因为这个是直接存数据库的
            if (CollectionUtils.isNotEmpty(appPushMsg.getTargetUser())) {
                if (appPushMsg.getTargetUser().size() > 100) {
                    return MapMessage.errorMessage("用户数量过多，请使用附件上传");
                }
            }
        }

        // 6. 投放指定地区时
        if (AppPushMsgConstants.TargetRegion == pushType) {
            if (CollectionUtils.isEmpty(appPushMsg.getTargetRegion())) {
                return MapMessage.errorMessage("请选择有效的地区");
            }
            int regionTags = appPushMsg.getTargetRegion().size();
            if (regionTags > 20) {
                return MapMessage.errorMessage("地区的数量不要超过20条");
            }

            // 如果使用了 Jpush，因为其orTag只能支持一种维度，因此扩展选项只允许一项多选
            if (appPushMsg.canSendPush() && !appPushMsg.checkOnlyOneMultiChoice()) {
                return MapMessage.errorMessage("扩展选项只允许一项多选");
            }
        }

        // 7. 投放指定学校时
        if (AppPushMsgConstants.TargetSchool == pushType) {
            if (CollectionUtils.isEmpty(appPushMsg.getTargetSchool())) {
                return MapMessage.errorMessage("请填写有效的学校ID");
            }
            int schoolTags = appPushMsg.getTargetSchool().size();
            // FIXME 这里的20条限制是因为Jpush的orTag最大只支持20个，其实如果超过20个的话可以考虑拆成多条Push，先这么着了，地区同理
            if (schoolTags > 20) {
                return MapMessage.errorMessage("学校的数量不要超过20条");
            }

            // 如果使用了 Jpush，因为其orTag只能支持一种维度，因此扩展选项只允许一项多选
            if (appPushMsg.canSendPush() && !appPushMsg.checkOnlyOneMultiChoice()) {
                return MapMessage.errorMessage("扩展选项只允许一项多选");
            }
        }

        // 根据指定标签发送
        if (AppPushMsgConstants.TargetTagGroup == pushType) {
            List<List<Map<String, Object>>> tagGroups = appPushMsg.getTargetTagGroups();
            if(CollectionUtils.isEmpty(tagGroups)){
                return MapMessage.errorMessage("请选设置标签组");
            }
        }

        // 8. 混合投放模式
        if (AppPushMsgConstants.Fixed == pushType) {
            // 如果使用了 Jpush，因为其orTag只能支持一种维度，因此扩展选项只允许一项多选
            if (appPushMsg.canSendPush() && !appPushMsg.checkOnlyOneMultiChoice()) {
                return MapMessage.errorMessage("扩展选项只允许一项多选");
            }
        }

        return MapMessage.successMessage();
    }

    public static MapMessage validateFast(AppPushWfMessage appPushMsg) {
        if (appPushMsg == null) {
            return MapMessage.errorMessage("开玩喜呢，啥都没有");
        }

        // 0.1 跳转URL不能有两个[?], 就算参数需要带 url 自己先去encode
//        if (appPushMsg.getLink() != null && StringUtils.countMatches(appPushMsg.getLink(), "?") > 1) {
//            return MapMessage.errorMessage("无效的跳转链接");
//        }

        // 1. 学段必填
        if (validateKtwelve(appPushMsg.getKtwelve())) {
            return MapMessage.errorMessage("请选择学段");
        }

        // 2. 校验是否勾选了发送类型
        if (!appPushMsg.canSendPush() && !appPushMsg.canSendMsg()) {
            return MapMessage.errorMessage("什么都不发， 你要干嘛 ？");
        }

        // 3. 校验是否选择了有效的发送端
        UserType userType = appPushMsg.fetchTargetUserType();
        if (userType == null) {
            return MapMessage.errorMessage("无效的发送端：" + appPushMsg.getUserType() + ", idType=" + appPushMsg.getIdType());
        }

        // 4. 只能使用指定用户投放
        int pushType = appPushMsg.getPushType();
        if (AppPushMsgConstants.TargetUser != pushType) {
            return MapMessage.errorMessage("快速推送只支持按用户ID推送");
        }

        // 5. 投放指定用户时
        if (CollectionUtils.isEmpty(appPushMsg.getTargetUser())) {
            return MapMessage.errorMessage("没有填有效的用户ID，发给谁呢？");
        }

        if (appPushMsg.getTargetUser().size() > 5) {
            return MapMessage.errorMessage("快速推送最多只能推送5个用户");
        }
        return MapMessage.successMessage();
    }

    public static String generateWorkFlowContent(AppPushWfMessage workflowMessage) {
        StringBuilder content = new StringBuilder();
        content.append("发送APP：").append(workflowMessage.getUserType()).append("<br/>");
        if (workflowMessage.canSendPush()) {
            content.append("Push内容：").append(workflowMessage.getNotifyContent()).append("<br/>");
        }
        if (workflowMessage.canSendMsg()) {
            content.append("标题：").append(workflowMessage.getTitle()).append("<br/>");
            content.append("概要：").append(workflowMessage.getContent()).append("<br/>");
        }
        return content.toString();
    }

    private static boolean validateKtwelve(String ktwelve) {
        return ktwelve == null || !Arrays.asList("j", "m", "i", "s").contains(ktwelve);
    }

    public static List<AppMessage> generateUserMessage(AppPushWfMessage workflowMessage, Collection<Long> userIds, Integer messageType, Map<String, Object> messageExtInfo) {
        userIds = CollectionUtils.toLinkedHashSet(userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return userIds.stream().map(userId -> {
            AppMessage userMessage = new AppMessage();
            userMessage.setUserId(userId);
            userMessage.setMessageType(messageType);
            userMessage.setTitle(workflowMessage.getTitle());
            userMessage.setContent(workflowMessage.getContent());
            userMessage.setImageUrl(workflowMessage.getFileName());
            userMessage.setLinkUrl(workflowMessage.getLink());
            userMessage.setLinkType(0);   //crm运营消息应该都是绝对url
            userMessage.setIsTop(Boolean.TRUE.equals(workflowMessage.getIsTop()));
            userMessage.setTopEndTime(workflowMessage.fetchTopEndTime());
            userMessage.setExtInfo(messageExtInfo);
            return userMessage;
        }).collect(Collectors.toList());
    }


}

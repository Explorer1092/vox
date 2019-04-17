package com.voxlearning.utopia.service.crm.impl.support.apppush.publisher;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.constants.crm.AppPushMsgConstants;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.impl.support.apppush.AppPushWorkflowContext;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ParentAppPushPublisher extends AppPushPublisher {

    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public MapMessage sendPushMessage(AppPushWorkflowContext context) {
        AppPushWfMessage wfMsg = context.getWorkflowMessage();
        // 处理参数
        String link = StringUtils.trim(wfMsg.getLink());
        AppMessageSource source = AppMessageSource.of(wfMsg.getSendApp());
        if(source == AppMessageSource.UNKNOWN)
            return MapMessage.errorMessage("未知的sendApp");

        Boolean share = Boolean.TRUE.equals(wfMsg.getShare());
        String shareContent = wfMsg.getShareContent();
        String shareUrl = StringUtils.trim(wfMsg.getShareUrl());
        ParentMessageShareType shareType = share ? ParentMessageShareType.SHARE_VIEW : ParentMessageShareType.NO_SHARE_VIEW;
        shareUrl = i7TinyUrl(shareUrl);

        // jpush发送扩展参数
        Map<String, Object> jpushExtInfo = new HashMap<>();
        //这个字段给客户端点击消息进入列表后返回首页清除首页-系统消息 tab的红点使用
        jpushExtInfo.put("ext_tab_message_type", ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getType());
        // 记录taskId
        recordTaskId(jpushExtInfo);
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("url", link);
        jpushExtInfo.put("tag", ParentMessageTag.通知.name());
        jpushExtInfo.put("shareType", shareType.name());
        jpushExtInfo.put("shareContent", shareContent);
        jpushExtInfo.put("shareUrl", shareUrl);
        jpushExtInfo.put("s",ParentAppPushType.NOTICE.name());

        String content;
        if (RuntimeMode.le(Mode.STAGING)) {
            content = StringUtils.formatMessage("{}（来自于{}环境）", wfMsg.getNotifyContent(), RuntimeMode.getCurrentStage());
        } else {
            content = wfMsg.getNotifyContent();
        }
        // 按用户发送
        int pushType = wfMsg.getPushType();
        if (pushType == AppPushMsgConstants.TargetUser) {
            int idType = wfMsg.getIdType();
            List<Long> totalUserIdsList = new ArrayList<>();
            if (idType == 1) {//导入的是学生ID，就通过学生ID查到家长ID
                List<Long> sendUserList = fetchSendUserList(wfMsg);
                Map<Long, List<StudentParentRef>> studentParentRefMap = studentLoaderClient.loadStudentParentRefs(sendUserList);
                if (studentParentRefMap.isEmpty()) {
                    return MapMessage.errorMessage("未通过学生ID找到相应的家长");
                }
                totalUserIdsList = studentParentRefMap.values().stream()
                        .flatMap(Collection::stream)
                        .map(StudentParentRef::getParentId)
                        .distinct()
                        .collect(Collectors.toList());
            } else if (idType == 2) {//导入的是家长ID，可以直接使用
                totalUserIdsList = fetchSendUserList(wfMsg);
            }
            int time = Integer.max(1, (int) Math.ceil(totalUserIdsList.size() / 200d));
            CollectionUtils.splitList(totalUserIdsList, time)
                    .forEach(list -> appMessageServiceClient.sendAppJpushMessageByIds(content, source, list, jpushExtInfo, wfMsg.fetchSendTime()));

            // 按指定用户处理完毕
            return MapMessage.successMessage();
        }else if(pushType == AppPushMsgConstants.TargetTagGroup){
            List<Long> targetUserIds = context.getTargetUserIds();
            if(CollectionUtils.isNotEmpty(targetUserIds)){
                int time = Integer.max(1, (int) Math.ceil(targetUserIds.size() / 200d));
                CollectionUtils.splitList(targetUserIds, time)
                        .forEach(list -> appMessageServiceClient.sendAppJpushMessageByIds(content, source, list, jpushExtInfo, wfMsg.fetchSendTime()));

                //logger.info("send push to {}, {}, {}, {}, {}", content, source, targetUserIds, jpushExtInfo, wfMsg.fetchSendTime());
            }
            return MapMessage.successMessage();
        }

        JPushTag jPushTag = jpushTag(wfMsg);
        if (jPushTag.isEmpty()) {
            return MapMessage.errorMessage("没有合适的投放策略");
        }
        appMessageServiceClient.sendAppJpushMessageByTags(
                content, source, jPushTag.getOrTag(), jPushTag.getAndTag(), jpushExtInfo, wfMsg.getDurationTime(), wfMsg.fetchSendTime()
        );
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage sendAppMessage(AppPushWorkflowContext context) {
        AppPushWfMessage wfMsg = context.getWorkflowMessage();
        // 处理参数
        String link = StringUtils.trim(wfMsg.getLink());
        AppMessageSource source = AppMessageSource.of(wfMsg.getSendApp());
        Integer messageType = ParentMessageType.REMINDER.getType();
        Boolean share = Boolean.TRUE.equals(wfMsg.getShare());
        String shareContent = wfMsg.getShareContent();
        String shareUrl = StringUtils.trim(wfMsg.getShareUrl());
        ParentMessageShareType shareType = share ? ParentMessageShareType.SHARE_VIEW : ParentMessageShareType.NO_SHARE_VIEW;
        shareUrl = i7TinyUrl(shareUrl);

        ParentMessageTag messageTag = ParentMessageTag.nameOf(wfMsg.getMessageTag());
        // 小铃铛消息参数
        Map<String, Object> messageExtInfo = new HashMap<>();
        messageExtInfo.put("studentId", "");
        messageExtInfo.put("senderName", "");
        messageExtInfo.put("tag", messageTag == null ? ParentMessageTag.通知.name() : messageTag.name());
        messageExtInfo.put("type", ParentMessageType.REMINDER.name());
        messageExtInfo.put("shareType", shareType.name());
        messageExtInfo.put("shareContent", shareContent);
        messageExtInfo.put("shareUrl", shareUrl);

        // 按指定用户发送
        int pushType = wfMsg.getPushType();
        if (pushType == AppPushMsgConstants.TargetUser) {
            int idType = wfMsg.getIdType();
            List<Long> totalUserIdsList = new ArrayList<>();
            if (idType == 1) {// 导入的是学生ID，就通过学生ID查到家长ID
                List<Long> sendUserList = fetchSendUserList(wfMsg);
                Map<Long, List<StudentParentRef>> studentParentRefMap = studentLoaderClient.loadStudentParentRefs(sendUserList);
                if (studentParentRefMap.isEmpty()) {
                    return MapMessage.errorMessage("未通过学生ID找到相应的家长");
                }
                totalUserIdsList = studentParentRefMap.values().stream()
                        .flatMap(Collection::stream)
                        .map(StudentParentRef::getParentId)
                        .distinct()
                        .collect(Collectors.toList());
            } else if (idType == 2) {// 导入的是家长ID，可以直接使用
                totalUserIdsList = fetchSendUserList(wfMsg);
            }
            if (CollectionUtils.isEmpty(totalUserIdsList)) {
                return MapMessage.errorMessage("未输入合法的用户ID");
            }
            totalUserIdsList.stream().map(userId -> {
                AppMessage message = new AppMessage();
                message.setUserId(userId);
                message.setMessageType(messageType);
                message.setTitle(wfMsg.getTitle());
                message.setContent(wfMsg.getContent());
                message.setImageUrl(wfMsg.getFileName());
                message.setLinkUrl(link);
                message.setLinkType(0); //crm运营消息应该都是绝对url
                message.setIsTop(Boolean.TRUE.equals(wfMsg.getIsTop()));
                message.setTopEndTime(wfMsg.fetchTopEndTime());
                message.setExtInfo(messageExtInfo);
                return message;
            }).forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
            return MapMessage.successMessage();
        }else if(pushType == AppPushMsgConstants.TargetTagGroup){
            List<Long> targetUserIds = context.getTargetUserIds();
            if(CollectionUtils.isEmpty(targetUserIds)){
                return MapMessage.errorMessage("未输入合法的用户ID");
            }
            targetUserIds.stream().map(userId -> {
                AppMessage message = new AppMessage();
                message.setUserId(userId);
                message.setMessageType(messageType);
                message.setTitle(wfMsg.getTitle());
                message.setContent(wfMsg.getContent());
                message.setImageUrl(wfMsg.getFileName());
                message.setLinkUrl(link);
                message.setLinkType(0); //crm运营消息应该都是绝对url
                message.setIsTop(Boolean.TRUE.equals(wfMsg.getIsTop()));
                message.setTopEndTime(wfMsg.fetchTopEndTime());
                message.setExtInfo(messageExtInfo);
                return message;
            }).forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
            return MapMessage.successMessage();
        }

        // 其他策略发送
        AppGlobalMessage globalMessage = new AppGlobalMessage();
        globalMessage.setMessageSource(source.name());
        globalMessage.setMessageType(messageType);
        globalMessage.setTitle(wfMsg.getTitle());
        globalMessage.setContent(wfMsg.getContent());
        globalMessage.setExtInfo(messageExtInfo);
        globalMessage.setImageUrl(wfMsg.getFileName());
        globalMessage.setLinkUrl(link);
        globalMessage.setLinkType(0); //crm运营消息应该都是绝对url
        globalMessage.setIsTop(Boolean.TRUE.equals(wfMsg.getIsTop()));
        globalMessage.setTopEndTime(wfMsg.fetchTopEndTime());

        // 带上约束条件
        // 地区
        if (CollectionUtils.isNotEmpty(wfMsg.getTargetRegion())) {
            globalMessage.withRegionConstraint(genRegionTags(wfMsg.getTargetRegion()));
        }

        // 学校
        if (CollectionUtils.isNotEmpty(wfMsg.getTargetSchool())) {
            List<String> schoolTags = wfMsg.getTargetSchool().stream()
                    .map(school -> JpushUserTag.SCHOOL.generateTag(school.toString())).collect(Collectors.toList());
            globalMessage.withSchoolConstraint(schoolTags);
        }

        // 学段
//        String ktwelveTag = ("j".equals(wfMsg.getKtwelve())) ? JpushUserTag.PRIMARY_SCHOOL.tag : JpushUserTag.JUNIOR_SCHOOL.tag;
        String ktwelveTag = getUserKtwelvePushTag(wfMsg.getKtwelve());
        globalMessage.withSchoolLevelConstraint(Collections.singletonList(ktwelveTag));

        // 年级
        List<String> clazzLevelTag = wfMsg.parseClazzLevels().stream()
                .map(clazzLevel -> JpushUserTag.CLAZZ_LEVEL.generateTag(String.valueOf(clazzLevel.getLevel()))).collect(Collectors.toList());
        globalMessage.withClazzLevelConstraint(clazzLevelTag);

        // 黑白名单
        List<String> paymentList = new ArrayList<>();
        if (wfMsg.inPaymentBlackList()) {
            paymentList.add(JpushUserTag.PAYMENT_BLACK_LIST.tag);
        }
        if (wfMsg.inNoneBlackList()) {
            paymentList.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
        }
        globalMessage.withPaymentListConstraint(paymentList);

        globalMessage.setCreateTime(wfMsg.getSendTime() == null ? System.currentTimeMillis() : wfMsg.getSendTime().getTime());

        // 发送
        messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(globalMessage);

        return MapMessage.successMessage();
    }

    /**
     * 此处认为已经完成了校验，无需再做校验了
     * 如果有 size == 1 的选项，收入 andTags 里面
     * 如果有 size > 1 的选项， 收入 orTags 里面
     */
    public JPushTag jpushTag(AppPushWfMessage wfMsg) {
        List<String> andTags = new ArrayList<>();
        List<String> orTags = new ArrayList<>();

        // 地区
        List<Integer> regions = wfMsg.getTargetRegion();
        if (CollectionUtils.isNotEmpty(regions)) {
            int regionCnt = regions.size();
            if (regionCnt == 1) {
                String regionTag = genRegionTag(regions.get(0));
                if (StringUtils.isNotBlank(regionTag)) andTags.add(regionTag);
            } else {
                orTags.addAll(genRegionTags(regions));
            }
        }

        // 学校
        List<Long> schools = wfMsg.getTargetSchool();
        if (CollectionUtils.isNotEmpty(schools)) {
            int schoolCnt = schools.size();
            if (schoolCnt == 1) {
                andTags.add(JpushUserTag.SCHOOL.generateTag(schools.get(0).toString()));
            } else {
                schools.forEach(school -> orTags.add(JpushUserTag.SCHOOL.generateTag(school.toString())));
            }
        }

        // 学段
        String ktwelve = wfMsg.getKtwelve();
        if (StringUtils.isNotBlank(ktwelve)) {
            andTags.add(getUserKtwelvePushTag(ktwelve));
//            andTags.add(ktwelve.equals("j") ? JpushUserTag.PRIMARY_SCHOOL.tag : JpushUserTag.JUNIOR_SCHOOL.tag);
        }

        // 年级
        List<ClazzLevel> clazzLevels = wfMsg.parseClazzLevels();
        if (clazzLevels.size() == 1) {
            andTags.add(JpushUserTag.CLAZZ_LEVEL.generateTag(String.valueOf(clazzLevels.get(0).getLevel())));
        } else if (clazzLevels.size() > 1) {
            orTags.addAll(
                    clazzLevels.stream().map(clazzLevel -> JpushUserTag.CLAZZ_LEVEL.generateTag(String.valueOf(clazzLevel.getLevel()))).collect(Collectors.toList())
            );
        }

        // 付费黑名单
        if (wfMsg.inPaymentBlackList()) {
            andTags.add(JpushUserTag.PAYMENT_BLACK_LIST.tag);
        }

        // 不包含黑名单
        if (wfMsg.inNoneBlackList()) {
            andTags.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
        }

        return new JPushTag(orTags, andTags);
    }

}

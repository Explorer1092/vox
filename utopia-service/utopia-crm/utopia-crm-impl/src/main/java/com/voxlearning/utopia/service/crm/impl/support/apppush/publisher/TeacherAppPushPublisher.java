package com.voxlearning.utopia.service.crm.impl.support.apppush.publisher;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.constants.crm.AppPushMsgConstants;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.impl.support.apppush.AppPushWorkflowContext;
import com.voxlearning.utopia.service.crm.tools.AppPushWorkFlowUtils;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppJxtExtTabTypeToNative;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class TeacherAppPushPublisher extends AppPushPublisher {

    @Override
    public MapMessage sendAppMessage(AppPushWorkflowContext context) {
        AppPushWfMessage wfMsg = context.getWorkflowMessage();
        // 处理参数
        Integer messageType = TeacherMessageType.ACTIVIY.getType();
        String ktwelve = wfMsg.getKtwelve();
        AppMessageSource source = getAppMessageSource(wfMsg.getSendApp());
        if(source == AppMessageSource.UNKNOWN)
            return MapMessage.errorMessage("未知的sendApp参数!");

        String link = StringUtils.trim(wfMsg.getLink());
        // 小铃铛消息参数
        Map<String, Object> messageExtInfo = new HashMap<>();

        int pushType = wfMsg.getPushType();
        // 按指定用户发送
        if (pushType == AppPushMsgConstants.TargetUser) {
            List<Long> totalUserIdsList = fetchSendUserList(wfMsg);
            if (CollectionUtils.isEmpty(totalUserIdsList)) {
                return MapMessage.errorMessage("没有可以发的用户");
            }
            AppPushWorkFlowUtils.generateUserMessage(wfMsg, totalUserIdsList, messageType, messageExtInfo)
                    .forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);

            // 按指定用户处理完毕
            return MapMessage.successMessage();
        }else if(pushType == AppPushMsgConstants.TargetTagGroup){
            List<Long> targetUserIds = context.getTargetUserIds();
            if(CollectionUtils.isEmpty(targetUserIds)){
                return MapMessage.errorMessage("没有可以发的用户");
            }
            AppPushWorkFlowUtils.generateUserMessage(wfMsg, targetUserIds, messageType, messageExtInfo)
                    .forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
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
        String ktwelveTag = getUserKtwelvePushTag(ktwelve);
        globalMessage.withSchoolLevelConstraint(Collections.singletonList(ktwelveTag));

        // 年级
        List<String> clazzLevelTag = wfMsg.parseClazzLevels().stream()
                .map(clazzLevel -> JpushUserTag.CLAZZ_LEVEL.generateTag(String.valueOf(clazzLevel.getLevel()))).collect(Collectors.toList());
        globalMessage.withClazzLevelConstraint(clazzLevelTag);

        // 学科
        List<String> subjectTag = wfMsg.parseSubjects().stream()
                .map(subject -> JpushUserTag.SUBJECT.generateTag(subject.name())).collect(Collectors.toList());
        globalMessage.withSubjectConstraint(subjectTag);

        // 认证状态
        List<String> authTag = wfMsg.parseAuthenticationStates().stream()
                .map(state -> JpushUserTag.AUTH.generateTag(state.name())).collect(Collectors.toList());
        globalMessage.withAuthStateConstraint(authTag);

        globalMessage.setCreateTime(wfMsg.getSendTime() == null ? System.currentTimeMillis() : wfMsg.getSendTime().getTime());

        // 发送
        messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(globalMessage);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage sendPushMessage(AppPushWorkflowContext context) {
        AppPushWfMessage wfMsg = context.getWorkflowMessage();
        // 处理参数
        Integer messageType = TeacherMessageType.ACTIVIY.getType();
        String ktwelve = wfMsg.getKtwelve();
        AppMessageSource source = getAppMessageSource(wfMsg.getSendApp());

        // jpush发送扩展参数
        Map<String, Object> jpushExtInfo = new HashMap<>();
        // 记录taskId,方便日后记录收集
        recordTaskId(jpushExtInfo);
        jpushExtInfo.put("s", messageType);
        jpushExtInfo.put("link", StringUtils.trim(wfMsg.getLink()));
        jpushExtInfo.put("t", "h5");
        jpushExtInfo.put("key", ktwelve);
        //这个字段给客户端点击消息进入列表后返回首页清除首页-系统消息 tab的红点使用
        jpushExtInfo.put("ext_tab_message_type", ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getType());

        String content;
        if (RuntimeMode.le(Mode.STAGING)) {
            content = StringUtils.formatMessage("{}（来自于{}环境）", wfMsg.getNotifyContent(), RuntimeMode.getCurrentStage());
        } else {
            content = wfMsg.getNotifyContent();
        }
        int pushType = wfMsg.getPushType();
        // 按指定用户发送
        if (pushType == AppPushMsgConstants.TargetUser) {
            List<Long> totalUserIdsList = fetchSendUserList(wfMsg);
            // 没有可以发的用户
            if (CollectionUtils.isEmpty(totalUserIdsList)) {
                return MapMessage.errorMessage("没有可以发的用户");
            }
            int time = Integer.max(1, (int) Math.ceil(totalUserIdsList.size() / 200d));
            CollectionUtils.splitList(totalUserIdsList, time).forEach(list -> {
                if(RuntimeMode.isUsingTestData()){
                    logger.info("AppMessageWorkFlow:Send teacher app push,c:{},s:{},l:{},ext:{},t:{}",
                            content,source,list,jpushExtInfo,wfMsg.fetchSendTime());
                }

                appMessageServiceClient.sendAppJpushMessageByIds(content, source, list, jpushExtInfo, wfMsg.fetchSendTime());
            });

            return MapMessage.successMessage();
        }else if(pushType == AppPushMsgConstants.TargetTagGroup){
            List<Long> targetUserIds = context.getTargetUserIds();
            if(CollectionUtils.isEmpty(targetUserIds)){
                return MapMessage.errorMessage("没有可以发的用户");
            }
            int time = Integer.max(1, (int) Math.ceil(targetUserIds.size() / 200d));
            CollectionUtils.splitList(targetUserIds, time).forEach(list -> {
                if(RuntimeMode.isUsingTestData()){
                    logger.info("AppMessageWorkFlow:Send teacher app push,c:{},s:{},l:{},ext:{},t:{}",
                            content,source,list,jpushExtInfo,wfMsg.fetchSendTime());
                }

                appMessageServiceClient.sendAppJpushMessageByIds(content, source, list, jpushExtInfo, wfMsg.fetchSendTime());
            });
            return MapMessage.successMessage();
        }

        // 其他策略投放
        JPushTag jPushTag = jpushTag(wfMsg);
        if (jPushTag.isEmpty()) {
            return MapMessage.errorMessage("没有合适的投放策略");
        }

        appMessageServiceClient.sendAppJpushMessageByTags(
                content, source, jPushTag.getOrTag(), jPushTag.getAndTag(), jpushExtInfo, wfMsg.getDurationTime(), wfMsg.fetchSendTime()
        );
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
        }

        // 学科
        List<Subject> subjects = wfMsg.parseSubjects();
        if (subjects.size() == 1) {
            andTags.add(JpushUserTag.SUBJECT.generateTag(subjects.get(0).name()));
        } else if (subjects.size() > 1) {
            orTags.addAll(
                    subjects.stream().map(subject -> JpushUserTag.SUBJECT.generateTag(subject.name())).collect(Collectors.toList())
            );
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

        // 认证状态
        List<AuthenticationState> states = wfMsg.parseAuthenticationStates();
        if (states.size() == 1) {
            andTags.add(JpushUserTag.AUTH.generateTag(states.get(0).name()));
        } else if (states.size() > 1) {
            orTags.addAll(
                    states.stream().map(state -> JpushUserTag.AUTH.generateTag(state.name())).collect(Collectors.toList())
            );
        }

        return new JPushTag(orTags, andTags);
    }

    private AppMessageSource getAppMessageSource(String app) {
/*        if ("i".equals(ktwelve)) {
            return AppMessageSource.INFANT_TEACHER;
        } else if ("j".equals(ktwelve)) {
            return AppMessageSource.PRIMARY_TEACHER;
        } else if ("m".equals(ktwelve)) {
            return AppMessageSource.JUNIOR_TEACHER;
        } else if ("s".equals(ktwelve)) {
            return AppMessageSource.JUNIOR_TEACHER; // FIXME 高中暂时先跟初中一样
        }

        throw new IllegalArgumentException("unknown ktwelve value " + ktwelve);*/
        return AppMessageSource.of(app);
    }

}

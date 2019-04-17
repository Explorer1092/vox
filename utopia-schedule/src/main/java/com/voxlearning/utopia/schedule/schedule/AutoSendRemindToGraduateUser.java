/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.schedule;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.athena.APP;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.base.gray.StudentGrayFunctionManager;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by XiaoPeng.Yang on 15-4-29.
 * 常态功能 每年一次 目前只处理小学
 */
@Named
@ScheduledJobDefinition(
        jobName = "奖品中心毕业班用户提醒",
        jobDescription = "5月18日3点运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 8,18 5 ?"
)
@ProgressTotalWork(100)
public class AutoSendRemindToGraduateUser extends ScheduledJobWithJournalSupport {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;

    private UtopiaSql utopiaSql;
    private UtopiaSql utopiaSqlPlatform;

    //家长端接收push信息时间
    private String parentPushDay = "05-08";
    //家长端接收其他信息时间
    private String parentSendDay = "05-18";

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        utopiaSqlPlatform = utopiaSqlFactory.getUtopiaSql("hs_platform");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String testGroupIdsStr = SafeConverter.toString(parameters.get("testGroupIds"), "");
        List<Long> testGroupIds = Arrays.stream(testGroupIdsStr.split(","))
                .filter(StringUtils::isNotEmpty)
                .map(SafeConverter::toLong)
                .collect(Collectors.toList());

        //查询所有毕业班
        int jieP6 = ClassJieHelper.fromClazzLevel(ClazzLevel.SIXTH_GRADE);
        int jieP5 = ClassJieHelper.fromClazzLevel(ClazzLevel.FIFTH_GRADE);
        // 中学毕业的
        int jieP9 = ClassJieHelper.fromClazzLevel(ClazzLevel.NINTH_GRADE);

        String day = DateUtils.dateToString(new Date(startTimestamp), "MM-dd");

        String sql = "SELECT g.ID,s.JIE FROM VOX_CLASS s, VOX_CLAZZ_GROUP g WHERE s.ID = g.CLAZZ_ID AND s.JIE = " + jieP6 + " AND s.DISABLED = 0 AND s.EDU_SYSTEM = 'P6' AND g.DISABLED = FALSE " +
                " UNION ALL SELECT g.ID,s.JIE FROM VOX_CLASS s, VOX_CLAZZ_GROUP g WHERE s.ID = g.CLAZZ_ID AND s.JIE = " + jieP5 + " AND s.DISABLED = 0 AND s.EDU_SYSTEM = 'P5' AND g.DISABLED = FALSE" +
                " UNION ALL SELECT g.ID,s.JIE FROM VOX_CLASS s, VOX_CLAZZ_GROUP g WHERE s.ID = g.CLAZZ_ID AND s.JIE = " + jieP9 + " AND s.DISABLED = 0 AND g.DISABLED = FALSE ";
        // List<Long> groupIds = utopiaSql.withSql(sql).queryColumnValues(Long.class);
        List<Map<String, Object>> groupInfos = utopiaSql.withSql(sql).queryAll();

        progressMonitor.worked(5);

        List<Long> sendGroupIds = new ArrayList<>();

        String platformSql = "SELECT DISTINCT USER_ID FROM VOX_USER_POPUP WHERE UPDATE_DATETIME > '2019-05-18 11:22:00' AND CONTENT LIKE '%系统检测到您所带的班级中有毕业班学生%'";
        List<Long> userIds = utopiaSqlPlatform.withSql(platformSql).queryColumnValues(Long.class);
        if (CollectionUtils.isNotEmpty(userIds)) {
            String userIdJoin = StringUtils.join(userIds, ",");
            // 查询已经发送的groupID
            String sendSql = "SELECT DISTINCT CLAZZ_GROUP_ID FROM VOX_GROUP_TEACHER_REF WHERE TEACHER_ID IN (" + userIdJoin + ")";
            sendGroupIds = utopiaSql.withSql(sendSql).queryColumnValues(Long.class);
        }

        progressMonitor.worked(5);

        if (groupInfos.isEmpty()) {
            return;
        }

        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, groupInfos.size());

        // 记录灰度判断的缓存
        LoadingCache<Long, Boolean> grayCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<Long, Boolean>() {
                    @Override
                    public Boolean load(Long groupId) throws Exception {
                        return null;
                    }
                });

        StudentGrayFunctionManager stuGrayFuncMng = grayFunctionManagerClient.getStudentGrayFunctionManager();
        // 用户单发过滤
        String cacheKeyFix = "A_S_R_T_G_U_";
        for (Map<String, Object> groupInfo : groupInfos) {
            try {
                Long groupId = SafeConverter.toLong(groupInfo.get("ID"));
                int jie = SafeConverter.toInt(groupInfo.get("JIE"));

                // 如果有测试参数，则只看测试的id
                if (testGroupIds.size() > 0) {
                    parentPushDay = day;
                    parentSendDay = day;
                    if (!testGroupIds.contains(groupId)) {
                        continue;
                    }
                }

                if (sendGroupIds.contains(groupId)) {
                    continue;
                }

                //查班级学生
                List<Long> studentList = studentLoaderClient.loadGroupStudentIds(groupId);
                // 实物灰度下线区域不再发通知
                boolean ignore = studentList.stream()
                        .findFirst()
                        .map(id -> studentLoaderClient.loadStudentDetail(id))
                        .map(sd -> stuGrayFuncMng.isWebGrayFunctionAvailable(sd,"Reward","OfflineShiWu"))
                        .orElse(false);
                if(ignore){
                    continue;
                }

                // 只限制中学区域
                if (jie == jieP9) {
                    Boolean grayResult = grayCache.getIfPresent(groupId);
                    if (grayResult == null && studentList.size() > 0) {
                        // 任取个学生判断灰度
                        Long anyStuId = studentList.get(0);
                        StudentDetail anyStuDetail = studentLoaderClient.loadStudentDetail(anyStuId);

                        boolean judgeResult = stuGrayFuncMng.isWebGrayFunctionAvailable(anyStuDetail, "MSIntegral", "Mall");
                        grayResult = judgeResult;
                        grayCache.put(groupId, judgeResult);
                    }

                    // 如果不在灰度区域中，则不发送提示
                    // FIX：只限制中学区域
                    if (grayResult != null && !grayResult) {
                        continue;
                    }
                }

                if (CollectionUtils.isNotEmpty(studentList)) {
                    for (Long studentId : studentList) {
                        this.parentNotify(jie, jieP9, studentId, day);
                        this.studentNotify(jie, jieP9, studentId, cacheKeyFix);
                    }
                }

                this.teacherNotify(jie, jieP9, groupId, cacheKeyFix);
            } catch (Exception ignore) {
                logger.error("error happened when sending remind info", ignore);
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private void teacherNotify(int jie, int jieP9, long groupId, String cacheKeyFix) {
        //查班级老师
        List<Teacher> teacherList = teacherLoaderClient.loadGroupTeacher(groupId);
        if (CollectionUtils.isNotEmpty(teacherList)) {
            for (Teacher teacher : teacherList) {
                String key = cacheKeyFix + teacher.getId();
                CacheObject<String> object = CacheSystem.CBS.getCache("unflushable").get(key);
                if (object != null && StringUtils.isNotBlank(object.getValue())) {
                    continue;
                }

                // 初中的不发站内信
                if (jie != jieP9) {
                    String teacherLRMsg = "亲爱的老师: <br/>系统检测到您所带的班级中有毕业班学生，为了学生们毕业前能兑换到心仪奖品，辛苦您通知毕业班同学在5月31日前进行本学期最后一次实物兑换，毕业生虚拟兑换不受影响，教师奖品也不受此影响哦~~感谢您的辛勤付出~~ ";
                    userPopupServiceClient.createPopup(teacher.getId())
                            .content(teacherLRMsg)
                            .type(PopupType.DEFAULT_AD)
                            .category(PopupCategory.LOWER_RIGHT)
                            .create();

                    // 老师模板消息
                    String teacherWechatMsg = "亲爱的老师：系统检测到您所带的班级中有毕业班学生，为了学生们毕业前能兑换到心仪奖品，辛苦您通知毕业班同学在5月31日前进行本学期最后一次实物兑换，毕业生虚拟兑换不受影响，教师奖品也不受此影响哦~~感谢您的辛勤付出~~ ";
                    Map<String, Object> msgInfo = new HashMap<>();
                    msgInfo.put("content", teacherWechatMsg);
                    wechatServiceClient
                            .processWechatNotice(WechatNoticeProcessorType.TeacherRemindForGraduateStudentFromReward, teacher.getId(), msgInfo, WechatType.TEACHER);
                }

                // 认证老师发短信
                if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
                    UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(teacher.getId());
                    if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                        String teacherSmsMsg = "亲爱的老师：为避免毕业生无法兑换奖品，需要您通知毕业班同学5.31日前最后一次实物兑换，虚拟兑换不受影响，教师奖品也不受此影响哦~~感谢您的辛勤付出~~ ";
                        userSmsServiceClient.buildSms().content(teacherSmsMsg)
                                .to(userAuthentication)
                                .type(SmsType.TEACHER_REMIND_FOR_GRADUATE_STUDENT_FROM_REWARD)
                                .send();
                    }

                }
                // 记录发送缓存
                if (!RuntimeMode.isDevelopment() && !RuntimeMode.isTest()) {
                    CacheSystem.CBS.getCache("unflushable").set(key, 129600, "Y");
                }
            }
        }
    }

    private void studentNotify(int jie, int jieP9, long studentId, String cacheKeyFix) {
        String key = cacheKeyFix + studentId;
        CacheObject<String> object = CacheSystem.CBS.getCache("unflushable").get(key);
        if (object != null && StringUtils.isNotBlank(object.getValue())) {
            return;
        }

        String studentLRMsg;
        if (jie == jieP9)
            studentLRMsg = "亲爱的同学：<br/>祝贺你即将毕业，为避免毕业离校无法收到奖品，建议你在5月31日前进行本学期最后一次实物兑换，6月起毕业生仍能兑换虚拟奖品哦~~ ";
        else {
            studentLRMsg = "亲爱的同学：<br/>祝贺你即将毕业，为避免毕业离校无法收到奖品，建议你在5月31日前进行本学期最后一次实物兑换，6月起毕业生仍能兑换虚拟奖品哦~~ ";
        }

        // 右下角弹窗
        userPopupServiceClient.createPopup(studentId)
                .content(studentLRMsg)
                .type(PopupType.DEFAULT_AD)
                .category(PopupCategory.LOWER_RIGHT)
                .create();
        String studentAppMsg;
        if (jie == jieP9) {
            studentAppMsg = "亲爱的同学：祝贺你即将毕业，为避免毕业离校无法收到奖品，建议你在5月31日前进行本学期最后一次实物兑换，6月起毕业生仍能兑换虚拟奖品哦~~  ";
        } else {
            studentAppMsg = "亲爱的同学：祝贺你即将毕业，为避免毕业离校无法收到奖品，建议你在5月31日前进行本学期最后一次实物兑换，6月起毕业生仍能兑换虚拟奖品哦~~  ";
        }

        AppMessage message = new AppMessage();
        message.setUserId(studentId);
        message.setMessageType(StudentAppPushType.COMMON_REMIND.getType());
        message.setTitle("奖品中心提醒");
        message.setContent(studentAppMsg);
        message.setLinkUrl("");
        message.setLinkType(1);//站内的相对地址
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        // 记录发送缓存
        CacheSystem.CBS.getCache("unflushable").set(key, 129600, "Y");
    }

    private void parentNotify(int jie, int jieP9, long studentId, String day) {
        String parentAppMsg = null;
        if (parentSendDay.equals(day)) {
            if (jie != jieP9) {
                // 家长模板消息
                Map<String, Object> msgInfo = new HashMap<>();
                parentAppMsg = "亲爱的家长：您的孩子即将毕业，感谢您对一起作业的喜爱和支持，系统温馨提示您，记得提醒孩子在5月31日前进行本学期最后一次实物兑换哦，毕业后仍可兑换虚拟奖品，同时学豆在小朋友升入初中后也可以继续保留呢~~ ";
                msgInfo.put("content", parentAppMsg);
                wechatServiceClient
                        .processWithStudents(WechatNoticeProcessorType.ParentRemindForGraduateStudentFromReward, Collections.singleton(studentId), msgInfo, WechatType.PARENT);
            }

            // 家长app站内信
            List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);
            // 初中毕业班不发家长
            if (CollectionUtils.isNotEmpty(parents) && jie != jieP9) {
                Set<Long> parentIds = parents.stream().map(p -> p.getParentUser().getId()).collect(Collectors.toSet());
                parentAppMsg = "亲爱的家长：您的孩子即将毕业，感谢您对一起作业的喜爱和支持，系统温馨提示您，记得提醒孩子在5月31日前进行本学期最后一次实物兑换哦，毕业后仍可兑换虚拟奖品，同时学豆在小朋友升入初中后也可以继续保留呢~~ ";
                List<AppMessage> messageList = new ArrayList<>();

                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("studentId", "");
                extInfo.put("tag", ParentMessageTag.奖品中心.name());
                extInfo.put("type", ParentMessageType.REMINDER.name());
                extInfo.put("senderName", "");
                Date date = new Date();
                for (Long parentId : parentIds) {
                    //新消息中心
                    AppMessage message = new AppMessage();
                    message.setUserId(parentId);
                    message.setContent(parentAppMsg);
                    message.setLinkUrl("");
                    message.setImageUrl("");
                    message.setExtInfo(extInfo);
                    message.setMessageType(ParentMessageType.REMINDER.getType());
                    message.setCreateTime(date.getTime());
                    messageList.add(message);
                }
                messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
            }
        } else if (parentPushDay.equals(day)) {
            List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);
            if (CollectionUtils.isNotEmpty(parents) && jie != jieP9) {
                Set<Long> parentIds = parents.stream().map(p -> p.getParentUser().getId()).collect(Collectors.toSet());
                parentAppMsg = "亲爱的家长：\n" +
                        "\n" +
                        "您的孩子即将毕业，感谢您对一起作业的喜爱和支持，系统温馨提示您，记得提醒孩子在5月31日前进行本学期最后一次实物兑换哦，" +
                        "毕业后仍可兑换虚拟奖品，同时学豆在小朋友升入初中后也可以继续保留呢~~";
                appMessageServiceClient.sendAppJpushMessageByIds(parentAppMsg, AppMessageSource.PARENT, new ArrayList<>(parentIds), null);
            }
        }
    }
}

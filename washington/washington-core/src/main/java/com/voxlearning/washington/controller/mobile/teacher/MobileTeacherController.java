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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.StringDateSerializer;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.TeacherAlterationService;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzTeacherAlteration;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.TeacherApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 16/4/1
 */
@Controller
@RequestMapping(value = "/teacherMobile/")
@Slf4j
public class MobileTeacherController extends AbstractMobileTeacherController {

    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Inject private RaikouSDK raikouSDK;

    /**
     * 申请详情
     */
    @RequestMapping(value = "/application/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage applicationDetail() {
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;
        MapMessage resultMap = new MapMessage();
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());

        Long applicationId = getRequestLong("applicationId", 0);

        TeacherApplicationMapper teacherApplicationMapper = loadApplications(Collections.singleton(applicationId)).get(applicationId);
        if (teacherApplicationMapper == null || !relTeacherIds.contains(teacherApplicationMapper.getRespondentId())) {
//        if(teacherApplicationMapper == null ){
            resultMap.setSuccess(false);
            resultMap.setInfo("错误申请!");
            return resultMap;
        }


        resultMap.setSuccess(true);
        resultMap.add("applicationId", teacherApplicationMapper.getId());
        resultMap.add("date", StringDateSerializer.format(teacherApplicationMapper.getDate(), "yyyy/MM/dd HH:mm:ss"));
        resultMap.add("state", teacherApplicationMapper.getState());
        resultMap.add("content", generateApplicationContent(teacherApplicationMapper));
        return resultMap;

    }


    /**
     * 处理申请
     */
    @RequestMapping(value = "/application/deal.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage dealApplication() {
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;
        MapMessage resultMap = new MapMessage();

        Long applicationId = getRequestLong("applicationId", 0);
        String action = getRequestString("action");

        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());

        TeacherApplicationMapper teacherApplicationMapper = loadApplications(Collections.singleton(applicationId)).get(applicationId);
        if (teacherApplicationMapper == null || !relTeacherIds.contains(teacherApplicationMapper.getRespondentId())) {
//        if(teacherApplicationMapper == null ){
            resultMap.setSuccess(false);
            resultMap.setInfo("错误申请!");
            return resultMap;
        }

        resultMap = dealAllApplication(teacherApplicationMapper.getRespondentId(), teacherApplicationMapper.getType(), applicationId, action);

        return resultMap;

    }


    /**
     * 根据申请类型生成申请文案
     *
     * @param app
     * @return
     */
    private String generateApplicationContent(TeacherApplicationMapper app) {
        String templateLink = "{0}老师{1}申请和你一起教{2}的学生";
        String templateReplace = "{0}老师{1}申请代替你在{2}教英语";
        String templateTransfer = "{0}老师{1}请你在{2}担任{3}老师";

        String content = "";
        switch (app.getType()) {
            case "LINK":
                content = MessageFormat.format(templateLink, app.getApplicantSubject().getValue(), app.getApplicantName(), app.getClazzName());
                break;
            case "TRANSFER":
                content = MessageFormat.format(templateTransfer, app.getApplicantSubject().getValue(), app.getApplicantName(), app.getClazzName(), app.getApplicantSubject().getValue());
                break;
            case "REPLACE":
                content = MessageFormat.format(templateReplace, app.getApplicantSubject().getValue(), app.getApplicantName(), app.getClazzName());
                break;
        }
        return content;
    }


    public Map<Long, TeacherApplicationMapper> loadApplications(Collection<Long> applicationIds) {
        Map<Long, ClazzTeacherAlteration> applicationMap = teacherLoaderClient.loadClazzTeacherAlterations(applicationIds);

        Collection<ClazzTeacherAlteration> applications = applicationMap.values();
        // 申请人集合
        Set<Long> applicantIds = applications.stream()
                .map(ClazzTeacherAlteration::getApplicantId)
                .collect(Collectors.toSet());
        Map<Long, Teacher> applicants = teacherLoaderClient.loadTeachers(applicantIds);

        // 被申请人集合
        Set<Long> respondentIds = applications.stream()
                .map(ClazzTeacherAlteration::getRespondentId)
                .collect(Collectors.toSet());
        Map<Long, Teacher> respondents = teacherLoaderClient.loadTeachers(respondentIds);

        // 班级集合
        Set<Long> clazzIds = applications.stream()
                .map(ClazzTeacherAlteration::getClazzId)
                .collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        List<TeacherApplicationMapper> allApplicationMappers = applications.stream()
                .filter(e -> e.getType() == ClazzTeacherAlterationType.LINK || e.getType() == ClazzTeacherAlterationType.REPLACE || e.getType() == ClazzTeacherAlterationType.TRANSFER)
                .sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchUpdateTimestamp()))
                .map(e -> generateTeacherApplicationMapper(applicants, respondents, clazzs, e))
                .collect(Collectors.toList());

        return allApplicationMappers.stream().collect(Collectors.toMap(TeacherApplicationMapper::getId, (e) -> e));

    }

    private TeacherApplicationMapper generateTeacherApplicationMapper(Map<Long, Teacher> applicants, Map<Long, Teacher> respondents, Map<Long, Clazz> clazzs, ClazzTeacherAlteration e) {
        TeacherApplicationMapper mapper = new TeacherApplicationMapper();
        mapper.setId(e.getId());
        mapper.setType(e.getType().name());
        mapper.setClazzId(e.getClazzId());
        mapper.setApplicantId(e.getApplicantId());
        mapper.setApplicantName(applicants.get(e.getApplicantId()).fetchRealname());
        mapper.setApplicantSubject(applicants.get(e.getApplicantId()).getSubject());
        if (clazzs.containsKey(e.getClazzId())) {
            mapper.setClazzName(clazzs.get(e.getClazzId()).formalizeClazzName());
        } else {
            logger.warn("Teahcer Application ClazzTeacherAlteration has invalid clazzId. clazzId={}, alteration={}", e.getClazzId(), e.getId());
        }
        mapper.setRespondentId(e.getRespondentId());
        mapper.setRespondentName(respondents.get(e.getRespondentId()).fetchRealname());
        mapper.setRespondentSubject(respondents.get(e.getRespondentId()).getSubject());
        mapper.setState(e.getState().name());
        mapper.setDate(e.getUpdateDatetime());
        return mapper;
    }


    private MapMessage dealAllApplication(Long respondentId, String applicationTypeStr, long recordId, String applicationAction) {

        ClazzTeacherAlterationType applicationType = ClazzTeacherAlterationType.valueOf(applicationTypeStr);


        ApplicationMessageBuilder messageBuilder = buildMessageBuilder(applicationType, applicationAction);

        switch (applicationAction) {
            case "reject":
                MapMessage message = rejectApplication(respondentId, recordId, applicationType);
                if (message.isSuccess()) {
                    sendApplicationMessageToApplicant(message, messageBuilder, false, false);
                    sendAppMessageForDealApplication(message, messageBuilder);
                }
                return message;
            case "approve":
                MapMessage message1 = teacherAlterationServiceClient.approveApplication(respondentId, recordId, applicationType, OperationSourceType.app);

                if (message1.isSuccess()) {
                    sendApplicationMessageToApplicant(message1, messageBuilder, false, true);
                    sendAppMessageForDealApplication(message1, messageBuilder);
                }
                return message1;
            default:
                return new MapMessage().setSuccess(true);
        }
    }

    private ApplicationMessageBuilder buildMessageBuilder(ClazzTeacherAlterationType applicationType, String applicationAction) {
        if (ClazzTeacherAlterationType.REPLACE.equals(applicationType)) {
            if (applicationAction.equals("approve"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师同意了您接管{}的申请",
                                respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                    }
                };
            if (applicationAction.equals("reject"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师拒绝了您接管{}班级的申请",
                                respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                    }
                };
        }
        if (ClazzTeacherAlterationType.TRANSFER.equals(applicationType)) {
            if (applicationAction.equals("approve"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师同意了您转让{}的申请",
                                respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                    }
                };
            if (applicationAction.equals("reject"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师拒绝了您转让{}的申请",
                                respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                    }
                };
        }
        if (ClazzTeacherAlterationType.LINK.equals(applicationType)) {
            if (applicationAction.equals("approve"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师同意了您共享{}学生资源的申请",
                                respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                    }
                };
            if (applicationAction.equals("reject"))
                return new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师拒绝了您一起教{}班级的申请",
                                respondent.getProfile().getRealname(), clazz.formalizeClazzName());
                    }
                };
        }
        return null;
    }


    /**
     * 申请消息生成器
     *
     * @author changyuan.liu
     */
    private abstract class ApplicationMessageBuilder {
        /**
         * 生成申请消息
         *
         * @param applicant  申请人
         * @param respondent 被申请人
         * @param clazz      班级
         * @return 申请消息
         */
        abstract String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz);
    }


    /**
     * 给申请接受者发送消息提醒
     */
    private void sendApplicationMessageToApplicant(MapMessage message,
                                                   ApplicationMessageBuilder messageBuilder,
                                                   boolean appendCheckDetailBtn,
                                                   boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.get("applicant");
            Teacher respondent = (Teacher) message.get("respondent");
            Clazz clazz = (Clazz) message.get("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);
            //发送站内信
            doSendApplicationMessage(applicant, sendMsg, appendCheckDetailBtn, needPopup);
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage(), ex);
        }
    }


    private void doSendApplicationMessage(Teacher user,
                                          String message,
                                          boolean appendCheckDetailBtn,
                                          boolean needPopup) {
        // 需要加链接的
        if (appendCheckDetailBtn) {
            message = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【查看详情】</a>",
                    "/teacher/systemclazz/clazzindex.vpage"
            );
            // 发送站内信  带链接
            sendMessage(user, message);
        } else {
            // 发送站内信  不带链接
            sendMessage(user, message);
        }
        // 发右下角弹窗
        if (needPopup) {
            userPopupServiceClient.createPopup(user.getId())
                    .content(message)
                    .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
        }
    }

    private void sendMessage(User receiver, String payload) {
        if (StringUtils.isBlank(payload)) {
            return;
        }
        payload = StringUtils.replace(payload, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiver.getId(), payload);
    }

    /**
     * 拒绝申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage rejectApplication(long respondentId,
                                         long recordId,
                                         ClazzTeacherAlterationType type) {
        MapMessage msg = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, type, OperationSourceType.app);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("拒绝申请失败");
        }
        return msg;
    }

    /**
     * 处理班级请求时,给申请者发app消息,不发jpush
     *
     * @param message
     * @param applicationMessageBuilder
     * @return
     */
    private void sendAppMessageForDealApplication(MapMessage message, ApplicationMessageBuilder applicationMessageBuilder) {

        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");

        String messageContent = applicationMessageBuilder.buildMessage(applicant, respondent, clazz);

        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(applicant.getId());
        appMessage.setMessageType(TeacherMessageType.CLAZZNEWS.getType());
        appMessage.setContent(messageContent);
        appMessage.setTitle(TeacherMessageType.CLAZZNEWS.getDescription());

        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(applicant.getId());
        if (mainTeacherId != null && mainTeacherId > 0L) {
            appMessage.setUserId(mainTeacherId);
        }

        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
    }
}


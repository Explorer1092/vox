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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.TeacherAlterationService;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用于老师管理系统自建班级
 *
 * @author changyuan.liu
 * @since 2015/6/15
 */
@Slf4j
@Controller
@RequestMapping("teacher/systemclazz")
public class TeacherSystemClazzController extends AbstractTeacherController {

    @Inject private UserPopupServiceClient userPopupServiceClient;

    /**
     * 班级管理主页
     */
    @RequestMapping(value = "clazzindex.vpage", method = RequestMethod.GET)
    public String listManagedClazzs(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage";
    }

    /**
     * 老师拒绝其他老师关联学生申请
     */
    @RequestMapping(value = "rejectlinkapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rejectLinkApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) respondentId = getSubjectSpecifiedTeacherId();

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = rejectApplication(respondentId, recordId, ClazzTeacherAlterationType.LINK);

        if (message.isSuccess()) {
            sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师拒绝",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            }, false, false);
        }

        return message;
    }

    /**
     * 老师同意其他老师的关联学生申请
     */
    @RequestMapping(value = "approvelinkapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage approveLinkApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) respondentId = getSubjectSpecifiedTeacherId();

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = approveApplication(respondentId, recordId, ClazzTeacherAlterationType.LINK);

        if (message.isSuccess()) {
            sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师接受",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            }, false, true);
        }

        return message;
    }

    /**
     * 老师拒绝其他老师的接管学生申请
     */
    @RequestMapping(value = "rejectreplaceapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rejectReplaceApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) respondentId = getSubjectSpecifiedTeacherId();

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = rejectApplication(respondentId, recordId, ClazzTeacherAlterationType.REPLACE);

        if (message.isSuccess()) {
            sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师拒绝",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            }, false, false);
        }

        return message;
    }

    /**
     * 老师同意其他老师的接管学生申请
     */
    @RequestMapping(value = "approvereplaceapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage approveReplaceApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) respondentId = getSubjectSpecifiedTeacherId();

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = approveApplication(respondentId, recordId, ClazzTeacherAlterationType.REPLACE);

        if (message.isSuccess()) {
            sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师接受",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            }, false, true);
        }

        return message;
    }
    /**
     * 拒绝其他老师的转让班级申请
     */
    @RequestMapping(value = "rejecttransferapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rejectTransferApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) respondentId = getSubjectSpecifiedTeacherId();

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = rejectApplication(respondentId, recordId, ClazzTeacherAlterationType.TRANSFER);

        if (message.isSuccess()) {
            sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}拒绝",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            }, false, false);
        }

        return message;
    }

    /**
     * 同意其他老师的转让班级申请
     */
    @RequestMapping(value = "approvetransferapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage approveTransferApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) respondentId = getSubjectSpecifiedTeacherId();

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = approveApplication(respondentId, recordId, ClazzTeacherAlterationType.TRANSFER);

        if (message.isSuccess()) {
            sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}接受",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            }, false, true);
        }

        return message;
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
        MapMessage msg = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, type, OperationSourceType.pc);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("拒绝申请失败");
        }
        return msg;
    }

    /**
     * 同意申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     */
    private MapMessage approveApplication(long respondentId,
                                          long recordId,
                                          ClazzTeacherAlterationType type) {
        MapMessage msg = teacherAlterationServiceClient.approveApplication(respondentId, recordId, type, OperationSourceType.pc);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("批准申请失败");
        }
        return msg;
    }

    /**
     * 给申请接受者发送消息提醒
     */
    private void sendApplicationMessageToApplicant(MapMessage message,
                                                   ApplicationMessageBuilder messageBuilder,
                                                   boolean appendCheckDetailBtn,
                                                   boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(applicant, sendMsg, appendCheckDetailBtn, needPopup);
            //发送微信消息通知
            Map<String, Object> extensionInfo = MiscUtils.m("first", "班级申请通知",
                    "keyword1", "一起作业",
                    "keyword2", "您向" + respondent.fetchRealname() + "老师发出的班级请求已有结果",
                    "url", ProductConfig.get("wechat.url") + "/teacher/message/list.vpage?_from=wechatnotice");
            Map<Long, List<UserWechatRef>> userWechatRefs = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(applicant.getId()), WechatType.TEACHER);
            List<UserWechatRef> refs = userWechatRefs.get(applicant.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                wechatServiceClient.processWechatNotice(
                        WechatNoticeProcessorType.TeacherOperationNotice, applicant.getId(), extensionInfo, WechatType.TEACHER);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage(), ex);
        }
    }

    private void doSendApplicationMessage(Teacher user,
                                          String message,
                                          boolean appendCheckDetailBtn,
                                          boolean needPopup) {
        // 发送站内通知
        if (appendCheckDetailBtn) {
            // FIXME 查看申请地址的链接应该不用改
            String messagePayload = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【查看详情】</a>",
                    "/teacher/clazz/alteration/unprocessedapplication.vpage?type=someBodyToMe"
            );
            sendMessage(user, messagePayload);
        } else {
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

}

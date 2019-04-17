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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.AlterationCcProcessState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzTeacherAlteration;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserTagQueueClient;
import com.voxlearning.washington.controller.open.AbstractApiController;

import org.omg.PortableInterceptor.SUCCESSFUL;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 换班自动外呼处理相关API
 * Created by Yuechen Wang on 2016-05-10.
 */
@Controller
@RequestMapping(value = "/v1/clazz/alteration")
public class ClazzAlterationApiController extends AbstractApiController {

    private static final String CLAZZ_ALTERATION_APPROVE = "1"; // 同意换班请求
    private static final String CLAZZ_ALTERATION_REJECT = "2";  // 拒绝换班请求
    private static final String CLAZZ_ALTERATION_FAKE = "9";    // 疑似假老师
    private static final String CLAZZ_ALTERATION_EMPTY = "";    // 未处理，空按键
    private static final String CLAZZ_ALTERATION_MANUAL = "9";  // 询求人工帮助

    private static final int TASK_STATE_THROUGH = 0;
    private static final int TASK_STATE_NOT_CONNECTED = 1;

    private static final String CLAZZ_ALTERATION_KEY_INDEX = "CLAZZ_TEACHER_ALTERATION_KEY";

    @Inject
    private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject
    private UserTagQueueClient userTagQueueClient;
    @Inject
    private UserPopupServiceClient userPopupServiceClient;

    @RequestMapping(value = "/process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processClazzAlteration(@RequestBody Map params) {

        if(params == null)
            return failMessage("传入参数为空!");

        logger.info("自动外呼调用参数:" + params.toString());
 /*       try {
            validateRequired(REQ_ALTERATION_ID, "换班记录ID");
            validateRequired(REQ_ALTERATION_RESULT, "换班处理结果");
            validateRequired(REQ_ALTERATION_OPTION, "收取的按键");
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }*/

        MapMessage returnMap = new MapMessage()
                .add("code",200)
                .add("msg","ok");

        boolean multiRequest = false;
        // 可能对应着多个请求
        String multiAlterId = SafeConverter.toString(params.get(REQ_ALTERATION_ID));
        List<Long> alterIdList = Arrays.stream(multiAlterId.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (alterIdList.size() > 1)
            multiRequest = true;
        else if (alterIdList.size() <= 0)
            return failMessage("请求ID不存在!");

        Long alterationId = alterIdList.get(0);
        ClazzTeacherAlteration alteration = teacherLoaderClient.loadClazzTeacherAlteration(alterationId);
        if (alteration == null) {
            logger.error("自动外呼 Unknown clazz alteration id {} received!", alterationId);
            // 防止回调太频繁，用中通的格式
            return returnMap.setInfo("Unknown clazz alteration id:" + alterationId);
            //return failMessage("Unknown clazz alteration id:" + alterationId);
        }

        if (alteration.isDisabledTrue() || alteration.getState() != ClazzTeacherAlterationState.PENDING) {
            return returnMap;
        }

        int taskState = SafeConverter.toInt(params.get(REQ_ALTERATION_RESULT));
        String keyResult = SafeConverter.toString(params.get(REQ_ALTERATION_OPTION));
        MapMessage dealResult = null;

        if (taskState == TASK_STATE_THROUGH) {
            if (multiRequest) {
                if (keyResult.equals(CLAZZ_ALTERATION_MANUAL))
                    dealResult = dealNeedManualAlteration(alterIdList);
                else
                    // 发现异常时，将所有按键都置成
                    for(Long alterId :alterIdList)
                        dealResult = dealUnexpectedHungUpAlteration(alterId);
            } else {
                switch (keyResult) {
                    case CLAZZ_ALTERATION_APPROVE:
                        // 外呼结果成功 且 老师选择同意，通过换班请求
                        dealResult = approveClazzAlteration(alteration);
                        break;
                    case CLAZZ_ALTERATION_FAKE:
                        // 外呼结果成功且老师选择了我不是一名老师，给该老师打上疑似标签
                        fakeRespondentTeacher(alteration.getRespondentId());
                        dealResult = dealUnexpectedHungUpAlteration(alterationId);
                        break;
                    case CLAZZ_ALTERATION_REJECT:
                        // 外呼结果成功 且 老师选择拒绝，拒绝换班请求
                        dealResult = rejectClazzAlteration(alteration);
                        break;
                    case CLAZZ_ALTERATION_EMPTY:
                        // 接通但是按键为空
                        dealResult = dealUnexpectedHungUpAlteration(alterationId);
                        break;
                    default:
                        // 按了意料之外的值
                        dealResult = dealUnexpectedHungUpAlteration(alterationId);
                        break;
                }
            }
        } else if (taskState == TASK_STATE_NOT_CONNECTED) {
            // 外呼结果未接通 或者 空号，说明异常挂断次数大于3次了，此时标记状态为异常
            dealResult = dealNonCallAlteration(alterIdList);
        } else
            // 异常接通的返回值
            dealResult = dealUnexpectedHungUpAlteration(alterationId);

        if(dealResult != null && RES_RESULT_SUCCESS.equals(dealResult.get(RES_RESULT)))
            return returnMap;
        else
            return dealResult;
    }

    @RequestMapping(value = "/cipher_key.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCipherKey() {
        try {
            validateRequired(REQ_CIPHER_TOKEN, "密钥TOKEN");
            validateRequestNoSessionKey(REQ_CIPHER_TOKEN);
        } catch (IllegalArgumentException ex) {
            return failMessage(ex);
        }

        // 这里用的 unflushable 缓存跟AutoSendClazzAlterationTaskJob里用的缓存保证一致
        String cipherToken = getRequestString(REQ_CIPHER_TOKEN);
        // 取之前校验一下TOKEN是否一致，如果直接用TOKEN获取密钥就没意义了
        String oldToken = CacheSystem.CBS.getCache("unflushable").load(CLAZZ_ALTERATION_KEY_INDEX);
        if (!StringUtils.equals(oldToken, cipherToken)) {
            return failMessage(RES_CIPHER_TOKEN_EXPIRED_MSG);
        }
        // token 校验无误方可去取密钥
        String cipherKey = CacheSystem.CBS.getCache("unflushable").load(cipherToken);
        if (StringUtils.isBlank(cipherKey)) {
            return failMessage(RES_FAILED_GET_CIPHER_TOKEN_MSG);
        }
        return successMessage().add(RES_CIPHER_KEY, cipherKey);
    }

    /**
     * 同意换班申请，逻辑跟 washington/.../TeacherSystemClazzController 里的同意处理保持一致
     *
     * @param alteration 同意的换班请求
     */
    public MapMessage approveClazzAlteration(ClazzTeacherAlteration alteration) {
        Long recordId = alteration.getId();
        Long applicantId = alteration.getApplicantId();
        Long respondentId = alteration.getRespondentId();
        ClazzTeacherAlterationType alterationType = alteration.getType();
        try {
            MapMessage message = teacherAlterationServiceClient.approveApplication(respondentId, recordId, alterationType, OperationSourceType.cc);
            if (!message.isSuccess()) {
                logger.error("Failed approve clazz alteration : (id={}, msg={})", recordId, message.get("info"));
                // 换班处理失败了，并不是客服系统的问题，仍然返回success
                teacherAlterationServiceClient.updateAlterationProcessState(recordId, AlterationCcProcessState.PROCESS_FAILED, OperationSourceType.cc);
                return successMessage();
            }
            // 发送教师首页通知
            sendTeacherNotify("同意", alterationType, message);
            return successMessage();
        } catch (Exception ex) {
            logger.error("Error occurs when approving teacher class alteration: (id={}, applicantId={}, respondentId={}, type={}, Ex={})",
                    recordId, applicantId, respondentId, alterationType, ex.getMessage(), ex);
            return failMessage(ex);
        }
    }

    /**
     * 拒绝换班申请，逻辑跟 washington/.../TeacherSystemClazzController 里的拒绝处理保持一致
     *
     * @param alteration 拒绝的换班请求
     */
    public MapMessage rejectClazzAlteration(ClazzTeacherAlteration alteration) {
        Long applicantId = alteration.getApplicantId();
        Long respondentId = alteration.getRespondentId();
        Long recordId = alteration.getId();
        ClazzTeacherAlterationType alterationType = alteration.getType();
        try {
            MapMessage message = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, alterationType, OperationSourceType.cc);
            if (!message.isSuccess()) {
                logger.error("Failed approve clazz alteration : (id={}, msg={})", recordId, message.get("info"));
                // 换班处理失败了，并不是客服系统的问题，仍然返回success
                teacherAlterationServiceClient.updateAlterationProcessState(recordId, AlterationCcProcessState.PROCESS_FAILED, OperationSourceType.cc);
                return successMessage();
            }
            // 发送教师首页通知
            sendTeacherNotify("拒绝", alterationType, message);
            return successMessage();
        } catch (Exception ex) {
            logger.error("Error occurs when rejecting clazz alteration. alterationId: {}, applicantId: {}, respondentId:{},  type:{}, Ex: {}",
                    recordId, applicantId, respondentId, alterationType, ex.getMessage(), ex);
            return failMessage(ex);
        }
    }

    /**
     * 给选择非老师的用户打上疑似假老师的标签
     */
    public MapMessage fakeRespondentTeacher(Long teacherId) {
        try {
            crmSummaryServiceClient.updateTeacherFakeType(teacherId, CrmTeacherFakeValidationType.AUTO_VALIDATION_CAC, "换班自动外呼判疑");

            // 记录USER_RECORD
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId("system");
            userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
            userServiceRecord.setOperationContent("自动外呼判假");
            userServiceRecord.setComments("自动外呼老师（" + teacherId + "），选择‘我不是一名老师’");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            return successMessage();
        } catch (Exception ex) {
            logger.error("Error occurs when faking respondent. respondentId: {}, Ex: {}", teacherId, ex.getMessage(), ex);
            return failMessage(ex);
        }
    }

    /**
     * 处理需要人工处理的请求
     */
    public MapMessage dealNeedManualAlteration(List<Long> alterationIds) {
        try {
            for (Long alterationId : alterationIds)
                teacherAlterationServiceClient.updateAlterationProcessState(alterationId, AlterationCcProcessState.PROCESS_MANUAL, OperationSourceType.cc);
            return successMessage();
        } catch (Exception ex) {
            logger.error("Error occurs when dealing unexpected hung up. alterationIds: {}, Ex: {}",
                    alterationIds, ex.getMessage(), ex);
            return failMessage(ex);
        }
    }

    /**
     * 处理挂断未接通的情况
     * @param alterationIds
     * @return
     */
    public MapMessage dealNonCallAlteration(List<Long> alterationIds){
        for(Long alterationId : alterationIds) {
            try {
                teacherAlterationServiceClient.updateAlterationProcessState(alterationId, AlterationCcProcessState.PROCESS_NONCALL, OperationSourceType.cc);
            } catch (Exception ex) {
                logger.error("Error occurs when dealing unexpected hung up. alterationId: {}, Ex: {}",
                        alterationId, ex.getMessage(), ex);
                return failMessage(ex);
            }
        }

        return successMessage();
    }

    /**
     * 处理异常挂断的请求
     *
     * @param alterationId 换班请求ID
     */
    public MapMessage dealUnexpectedHungUpAlteration(Long alterationId) {
        try {
            teacherAlterationServiceClient.updateAlterationProcessState(alterationId, AlterationCcProcessState.PROCESS_FAILED, OperationSourceType.cc);
            return successMessage();
        } catch (Exception ex) {
            logger.error("Error occurs when dealing unexpected hung up. alterationId: {}, Ex: {}",
                    alterationId, ex.getMessage(), ex);
            return failMessage(ex);
        }
    }

    private void sendTeacherNotify(String op, ClazzTeacherAlterationType type, MapMessage message) {
        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");
        String content;
        if (type != ClazzTeacherAlterationType.TRANSFER && type != ClazzTeacherAlterationType.REPLACE) {
            return;
        }
        if (type == ClazzTeacherAlterationType.TRANSFER) {
            content = StringUtils.formatMessage("{}老师{}了您转让{}的申请。",
                    respondent.getProfile().getRealname(), op, clazz.formalizeClazzName());
        } else {
            content = StringUtils.formatMessage("{}老师{}了您接管{}的申请。",
                    respondent.getProfile().getRealname(), op, clazz.formalizeClazzName());
        }
        userPopupServiceClient.createPopup(applicant.getId())
                .content(content)
                .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                .category(PopupCategory.LOWER_RIGHT)
                .create();
    }

}

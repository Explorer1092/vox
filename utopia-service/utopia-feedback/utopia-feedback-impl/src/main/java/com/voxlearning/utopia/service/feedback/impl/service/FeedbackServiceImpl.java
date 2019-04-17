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

package com.voxlearning.utopia.service.feedback.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.RegisterFeedbackCategory;
import com.voxlearning.utopia.api.constant.RegisterFeedbackState;
import com.voxlearning.utopia.service.feedback.api.FeedbackService;
import com.voxlearning.utopia.service.feedback.api.entities.*;
import com.voxlearning.utopia.service.feedback.impl.dao.*;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link FeedbackService}.
 *
 * @author Xiaohai Zhang
 * @since Jan 16, 2015
 */
@Named("com.voxlearning.utopia.service.feedback.impl.service.FeedbackServiceImpl")
@ExposeService(interfaceClass = FeedbackService.class)
public class FeedbackServiceImpl extends SpringContainerSupport implements FeedbackService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private ExamFeedbackPersistence examFeedbackPersistence;
    @Inject private RegisterFeedbackPersistence registerFeedbackPersistence;
    @Inject private UserFeedbackPersistence userFeedbackPersistence;
    @Inject private UserFeedbackTagPersistence userFeedbackTagPersistence;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private VoiceFeedbackPersistence voiceFeedbackPersistence;

    @Override
    public MapMessage saveFeedback(UserFeedback userFeedback) {
        // FIXME check the contact phone length
        if (userFeedback.getContactSensitivePhone() != null && userFeedback.getContactSensitivePhone().length() > 100) {
            return MapMessage.errorMessage();
        }

        if (StringUtils.contains(userFeedback.getIp(), ",")) {
            String ip = userFeedback.getIp().substring(0, userFeedback.getIp().indexOf(","));
            userFeedback.setIp(ip);
        }
        String ipAddress = raikouSystem.parseIpLocation(userFeedback.getIp()).getFullAddress();
        userFeedback.setAddress(ipAddress);
        try {
            userFeedbackPersistence.insert(userFeedback);
            return MapMessage.successMessage().add("feedbackId", userFeedback.getId());
        } catch (Exception ex) {
            logger.error("FAILED TO PERSIST USER FEEDBACK, userFeedback:{}", JsonUtils.toJson(userFeedback), ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage updateFeedbackContact(Long userId, Long feedbackId, String contactQq, String contactPhone) {
        UserFeedback feedback = userFeedbackPersistence.load(feedbackId);
        if (feedback == null || !Objects.equals(feedback.getUserId(), userId)) {
            return MapMessage.errorMessage();
        }
        feedback = new UserFeedback();
        feedback.setId(feedbackId);
        feedback.setContactSensitiveQq(contactQq);
        feedback.setContactSensitivePhone(contactPhone);
        userFeedbackPersistence.replace(feedback);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage sendExamFeedback(Long userId, String content, String examFeedbackType, String questionId) {
        if (userId == null) {
            return MapMessage.errorMessage("发送反馈失败，请稍后再试");
        }
        try {
            ExamFeedback examFeedback = new ExamFeedback();
            examFeedback.setUserId(userId);
            examFeedback.setQuestionId(questionId);
            examFeedback.setType(examFeedbackType);
            examFeedback.setContent(content);
            examFeedbackPersistence.insert(examFeedback);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("FAILED TO PERSIST EXAM FEEDBACK", ex);
            return MapMessage.errorMessage("发送反馈失败，请稍后再试");
        }
    }

    @Override
    public MapMessage persistExamFeedback(ExamFeedback examFeedback) {
        try {
            examFeedbackPersistence.insert(examFeedback);
            return MapMessage.successMessage("增加反馈成功");
        } catch (Exception ex) {
            logger.error("Failed to persist ExamFeedback");
            return MapMessage.errorMessage();
        }
    }

    @Override
    public AlpsFuture<MapMessage> callAmbassador2(Long ambassadorId, String mobile, String content) {
        String sensitiveMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
        List<RegisterFeedback> feedbacks = registerFeedbackPersistence.findByMobileAndCategoryAndStateWithinDayRange(sensitiveMobile, RegisterFeedbackCategory.CALL_AMBASSADOR, RegisterFeedbackState.WAITTING);
        if (feedbacks.isEmpty()) {
            RegisterFeedback feedback = new RegisterFeedback();
            feedback.setSensitiveMobile(sensitiveMobile);
            feedback.setVerificationCode("");
            feedback.setContent(content);
            feedback.setState(RegisterFeedbackState.WAITTING.getType());
            feedback.setCategory(RegisterFeedbackCategory.CALL_AMBASSADOR);
            feedback.setUserId(ambassadorId);
            registerFeedbackPersistence.insert(feedback);
        }
        return new ValueWrapperFuture<>(MapMessage.successMessage());
    }

    @Override
    public MapMessage clazzExchangeHelper(Long ambassadorId, String mobile, String content) {
        String sensitiveMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
        List<RegisterFeedback> feedbacks = registerFeedbackPersistence.findByMobileAndCategoryAndStateWithinDayRange(sensitiveMobile, RegisterFeedbackCategory.CALL_AMBASSADOR, RegisterFeedbackState.WAITTING);
        if (feedbacks.isEmpty()) {
            RegisterFeedback feedback = new RegisterFeedback();
            feedback.setSensitiveMobile(sensitiveMobile);
            feedback.setVerificationCode("");
            feedback.setContent(content);
            feedback.setState(RegisterFeedbackState.WAITTING.getType());
            feedback.setCategory(RegisterFeedbackCategory.CLAZZ_EXCHANGE);
            feedback.setUserId(ambassadorId);
            registerFeedbackPersistence.insert(feedback);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage processRegisterFeedback(Long id, String desc, int state, String operator) {
        MapMessage message = new MapMessage();
        try {
            if (StringUtils.isEmpty(desc)) {
                message.setSuccess(false);
                message.setInfo("备注不能为空");
                return message;
            }
            if (state != RegisterFeedbackState.WAITTING.getType() && state != RegisterFeedbackState.PROCESSED.getType()) {
                message.setSuccess(false);
                message.setInfo("非法的处理状态");
                return message;
            }

            RegisterFeedback feedback = registerFeedbackPersistence.load(id);
            if (null == feedback) {
                message.setSuccess(false);
                message.setInfo("反馈id无效");
                return message;
            }
            feedback.setOperation(desc);
            feedback.setOperator(operator);
            feedback.setState(state);
            registerFeedbackPersistence.replace(feedback);

            message.setSuccess(true);
            message.add("mobile", feedback.getSensitiveMobile());
            message.setInfo("处理完成");
        } catch (Exception ex) {
            logger.error("处理注册验证反馈失败，[id:{},desc:{},state:{}],msg:{}", id, desc, state, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("处理失败");
        }
        return message;
    }

    @Override
    public MapMessage persistUserFeedbackTag(UserFeedbackTag tag) {
        userFeedbackTagPersistence.insert(tag);
        return MapMessage.successMessage().add("id", tag.getId());
    }

    @Override
    public MapMessage updateUserFeedbackTag(Long id, UserFeedbackTag tag) {
        tag.setId(id);
        userFeedbackTagPersistence.replace(tag);
        return MapMessage.successMessage();
    }

    @Override
    public AlpsFuture<List<UserFeedback>> findUserFeedbackList(Long userId) {
        if (userId == null) {
            return new ValueWrapperFuture<>(Collections.emptyList());
        }
        List<UserFeedback> list = userFeedbackPersistence.findByUserId(userId);
        return new ValueWrapperFuture<>(list);
    }

    @Override
    public MapMessage persistVoiceFeedback(VoiceFeedback voiceFeedback) {
        try {
            voiceFeedbackPersistence.insert(voiceFeedback);
            return MapMessage.successMessage("增加反馈成功");
        } catch (Exception ex) {
            logger.error("Failed to persist VoiceFeedback", ex);
            return MapMessage.errorMessage("写入失败");
        }
    }
}

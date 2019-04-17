/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.feedback.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;
import com.voxlearning.utopia.service.feedback.api.entities.VoiceFeedback;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Feedback service interface definition.
 *
 * @author Xiaohai Zhang
 * @since Jan 16, 2015
 */
@ServiceVersion(version = "1.1.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface FeedbackService extends IPingable {

//    MapMessage feedback(String mobile, RegisterFeedbackCategory category);

    MapMessage saveFeedback(UserFeedback userFeedback);

    MapMessage updateFeedbackContact(Long userId, Long feedbackId, String contactQq, String contactPhone);

    MapMessage sendExamFeedback(Long userId, String content, String examFeedbackType, String questionId);

    MapMessage persistExamFeedback(ExamFeedback examFeedback);

    @Async
    AlpsFuture<MapMessage> callAmbassador2(Long ambassadorId, String mobile, String content);

    MapMessage clazzExchangeHelper(Long ambassadorId, String mobile, String content);

    MapMessage processRegisterFeedback(Long id, String desc, int state, String operator);

    MapMessage persistUserFeedbackTag(UserFeedbackTag tag);

    MapMessage updateUserFeedbackTag(Long id, UserFeedbackTag tag);

    @Async
    AlpsFuture<List<UserFeedback>> findUserFeedbackList(Long userId);

    MapMessage persistVoiceFeedback(VoiceFeedback voiceFeedback);
}

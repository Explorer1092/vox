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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.feedback.api.entities.RegisterFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Feedback loader interface definition.
 *
 * @author Xiaohai Zhang
 * @since Jan 16, 2015
 */
@ServiceVersion(version = "1.0.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface FeedbackLoader extends IPingable {

    List<RegisterFeedback> loadRegisterFeedbacks(int state, Date start, Date end);

    List<UserFeedbackTag> findByWatcherName(String watcherName);

    List<Map<String, Object>> findWatchersWithTag();

    UserFeedbackTag loadUserFeedbackTag(Long id);

    UserFeedbackTag findByTagName(String tagName);

    List<UserFeedbackTag> findAllTags();
}

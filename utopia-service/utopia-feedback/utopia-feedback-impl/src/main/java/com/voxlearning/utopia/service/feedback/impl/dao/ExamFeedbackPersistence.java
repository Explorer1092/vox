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

package com.voxlearning.utopia.service.feedback.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author Longlong Yu
 * @since 下午2:59,13-10-29.
 */
@Named("com.voxlearning.utopia.service.feedback.impl.dao.ExamFeedbackPersistence")
@CacheBean(type = ExamFeedback.class)
public class ExamFeedbackPersistence extends AlpsStaticJdbcDao<ExamFeedback, Long> {
    @Override
    protected void calculateCacheDimensions(ExamFeedback document, Collection<String> dimensions) {
        dimensions.add(ExamFeedback.ck_id(document.getId()));
    }
}

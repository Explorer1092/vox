/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;
import com.voxlearning.utopia.service.business.impl.persistence.BizStudentVoicePersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 原有的教研员功能service
 *
 * @author Maofeng Lu
 * @since 13-8-14 下午2:32
 */
@Named
public class StudentVoiceService {

    @Inject private BizStudentVoicePersistence bizStudentVoicePersistence;

    public List<BizStudentVoice> loadClazzStudentVoices(Collection<Long> clazzIds) {
        if (CollectionUtils.isEmpty(clazzIds)) {
            return Collections.emptyList();
        }
        return bizStudentVoicePersistence.findByClazzIds(clazzIds, 400);
    }

}

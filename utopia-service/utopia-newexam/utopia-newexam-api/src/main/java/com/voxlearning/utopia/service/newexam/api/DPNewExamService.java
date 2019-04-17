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

package com.voxlearning.utopia.service.newexam.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamRegistrationLoaderMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * New exam dubbo proxy service.
 */
@ServiceVersion(version = "2016.08.19")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPNewExamService {

    // ========================================================================
    // com.voxlearning.utopia.service.newexam.api.service.NewExamService
    // ========================================================================

    MapMessage loadAllExamsByStudentId(Long studentId);

    List<Map<String, Object>> loadExamsCanBeEnteredByStudentId(Long studentId);

    MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId);


    MapMessage handlerStudentExaminationAuthorityV2(Long sid, String newExamId, boolean makeUp);

    MapMessage loadByNewExamIdAndPage(NewExamRegistrationLoaderMapper newExamRegistrationLoaderMapper);

    /**
     * 查询某次考试学生人数
     * @param newExamId 考试ID
     * @return 学生数
     */
    Integer loadNewExamStudentCount(String newExamId);
}

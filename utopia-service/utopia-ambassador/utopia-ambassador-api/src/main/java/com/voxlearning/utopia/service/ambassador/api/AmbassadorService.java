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

package com.voxlearning.utopia.service.ambassador.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.api.constant.AmbassadorRecordType;
import com.voxlearning.utopia.service.ambassador.api.document.*;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Ambassador related service's abstraction.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@ServiceVersion(version = "2016.08.03")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AmbassadorService extends IPingable {

    void recordAmbassadorMentor(Long teacherId, Map<UserTagType, UserTagEventType> params);

    void addCompetitionScore(Long teacherId, Long targetUserId, AmbassadorCompetitionScoreType scoreType);

    void addAmbassadorScore(Long ambassadorId, Long targetUserId, AmbassadorCompetitionScoreType scoreType);

    void addScoreOncePerDay(Long teacherId, Long targetUserId, AmbassadorCompetitionScoreType scoreType);

    // 这个方法靠调用端的原子操作锁来保证数据的一致性
    MapMessage saveUpdateAmbassadorAcademyRecord(Long ambassadorId, AmbassadorRecordType recordType);

    // 改变大使学校
    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES)
    MapMessage changeAmbassadorSchool(Subject subject, AmbassadorSchoolRef sourceRef, Long targetSchoolId);

    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES)
    MapMessage disableAmbassador(Subject subject, AmbassadorSchoolRef sourceRef, Long targetSchoolId);

    // ========================================================================
    // dollar methods
    // ========================================================================

    AmbassadorSchoolRef $insertAmbassadorSchoolRef(AmbassadorSchoolRef document);

    AmbassadorSchoolRef $replaceAmbassadorSchoolRef(AmbassadorSchoolRef document);

    void $disableAmbassadorSchoolRef(Long ambassadorId);

    void $disableAmbassadorScoreHistories(Long ambassadorId);

    AmbassadorCompetition $insertAmbassadorCompetition(AmbassadorCompetition document);

    boolean $disableAmbassadorCompetition(Long id);

    AmbassadorLevelDetail $insertAmbassadorLevelDetail(AmbassadorLevelDetail document);

    AmbassadorLevelDetail $replaceAmbassadorLevelDetail(AmbassadorLevelDetail document);

    boolean $disableAmbassadorLevelDetail(Long id);

    AmbassadorReportInfo $insertAmbassadorReportInfo(AmbassadorReportInfo document);

    AmbassadorReportInfo $replaceAmbassadorReportInfo(AmbassadorReportInfo document);

    boolean $disableAmbassadorReportInfo(Long id);

    AmbassadorReportStudentFeedback $insertAmbassadorReportStudentFeedback(AmbassadorReportStudentFeedback document);

    AmbassadorLevelHistory $insertAmbassadorLevelHistory(AmbassadorLevelHistory document);
}

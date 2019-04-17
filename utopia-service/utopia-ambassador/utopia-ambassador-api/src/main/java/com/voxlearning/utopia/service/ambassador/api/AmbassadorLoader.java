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

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ambassador.api.document.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Ambassador related loader's abstraction.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@ServiceVersion(version = "2016.08.03")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AmbassadorLoader extends IPingable {

    /**
     * Find all ambassador school references belongs to specified ambassador.
     *
     * @param ambassadorId the ambassador id.
     * @return ambassador school reference list.
     */
    @Idempotent
    @CacheMethod(type = AmbassadorSchoolRef.class, writeCache = false)
    List<AmbassadorSchoolRef> findAmbassadorSchoolRefs(@CacheParameter("ambassadorId") Long ambassadorId);

    /**
     * Find all school ambassador references belongs to specified school.
     *
     * @param schoolId the school id.
     * @return school ambassador reference list.
     */
    @Idempotent
    @CacheMethod(type = AmbassadorSchoolRef.class, writeCache = false)
    List<AmbassadorSchoolRef> findSchoolAmbassadorRefs(@CacheParameter("schoolId") Long schoolId);

    @Idempotent
    boolean haveBeAmbassador(Long teacherId);

    /**
     * Load all ambassador teachers of specified school.
     *
     * @param schoolId the school id.
     * @return ambassador teachers.
     */
    @Idempotent
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    Map<Long, Teacher> loadSchoolAmbassadors(Long schoolId);

    /**
     * Find same subject ambassador in specified school.
     *
     * @param subject  the subject.
     * @param schoolId the school.
     * @return return null means not found.
     */
    @Idempotent
    AmbassadorSchoolRef findSameSubjectAmbassadorInSchool(Subject subject, Long schoolId);

    /**
     * Load academy record of specified ambassador.
     *
     * @param ambassadorId ambassador id.
     * @return return null means not found.
     */
    @Idempotent
    @CacheMethod(type = AmbassadorAcademyRecord.class, writeCache = false)
    AmbassadorAcademyRecord loadAmbassadorAcademyRecord(@CacheParameter("ambassadorId") Long ambassadorId);

    @Idempotent
    int loadAmbassadorTotalScore(Long ambassadorId, Date beginDate);

    @Idempotent
    List<AmbassadorScoreHistory> loadScoreHistory(Long ambassadorId, Date beginDate);

    @Idempotent
    @CacheMethod(type = AmbassadorCompetitionDetail.class, writeCache = false)
    List<AmbassadorCompetitionDetail> findAmbassadorCompetitionDetails(@CacheParameter("teacherId") Long teacherId);

    @Idempotent
    @CacheMethod(type = AmbassadorCompetition.class, writeCache = false)
    AmbassadorCompetition loadTeacherAmbassadorCompetition(@CacheParameter("teacherId") Long teacherId);

    @Idempotent
    @CacheMethod(type = AmbassadorCompetition.class, writeCache = false)
    List<AmbassadorCompetition> findSchoolAmbassadorCompetitions(@CacheParameter("schoolId") Long schoolId);

    @Idempotent
    @CacheMethod(type = AmbassadorLevelDetail.class, writeCache = false)
    AmbassadorLevelDetail loadAmbassadorLevelDetail(@CacheParameter("ambassadorId") Long ambassadorId);

    @Idempotent
    @CacheMethod(type = AmbassadorReportInfo.class, writeCache = false)
    List<AmbassadorReportInfo> findAmbassadorReportInfos(@CacheParameter("teacherId") Long teacherId);

    @Idempotent
    default List<AmbassadorReportInfo> loadAmbassadorReportInfoIgnoreDisabled(Long ambassadorId, Long teacherId, Date beginDate) {
        List<AmbassadorReportInfo> reportInfos = findAmbassadorReportInfos(teacherId);
        if (reportInfos.isEmpty()) {
            return Collections.emptyList();
        }
        return reportInfos.stream().filter(i -> Objects.equals(i.getReportId(), ambassadorId))
                .filter(i -> i.getCreateDatetime().after(beginDate)).collect(Collectors.toList());
    }

    @Idempotent
    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES)
    List<AmbassadorReportInfo> findAmbassadorReportInfosByType(Integer type);

    @Idempotent
    @CacheMethod(type = AmbassadorReportStudentFeedback.class, writeCache = false)
    List<AmbassadorReportStudentFeedback>
    findAmbassadorReportStudentFeedbacks(@CacheParameter("teacherId") Long teacherId);

    @Idempotent
    @CacheMethod(type = AmbassadorReportStudentFeedback.class, writeCache = false)
    List<AmbassadorReportStudentFeedback>
    findAmbassadorReportStudentFeedbacks(@CacheParameter("teacherId") Long teacherId,
                                         @CacheParameter("studentId") Long studentId);

    @Idempotent
    Page<AmbassadorRewardHistory> findAmbassadorRewardHistories(Pageable pageable, String month, Integer status);
}

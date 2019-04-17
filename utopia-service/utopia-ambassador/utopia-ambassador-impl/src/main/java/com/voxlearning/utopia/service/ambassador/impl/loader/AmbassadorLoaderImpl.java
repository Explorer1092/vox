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

package com.voxlearning.utopia.service.ambassador.impl.loader;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ambassador.api.AmbassadorLoader;
import com.voxlearning.utopia.service.ambassador.api.document.*;
import com.voxlearning.utopia.service.ambassador.impl.persistence.*;
import com.voxlearning.utopia.service.user.api.entities.TeacherSubjectRef;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The exposable service implementation of {@link AmbassadorLoader}.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@Named
@ExposeService(interfaceClass = AmbassadorLoader.class)
public class AmbassadorLoaderImpl implements AmbassadorLoader {

    @Inject private AmbassadorAcademyRecordDao ambassadorAcademyRecordDao;
    @Inject private AmbassadorCompetitionDao ambassadorCompetitionDao;
    @Inject private AmbassadorCompetitionDetailDao ambassadorCompetitionDetailDao;
    @Inject private AmbassadorLevelDetailDao ambassadorLevelDetailDao;
    @Inject private AmbassadorReportInfoDao ambassadorReportInfoDao;
    @Inject private AmbassadorReportStudentFeedbackDao ambassadorReportStudentFeedbackDao;
    @Inject private AmbassadorRewardHistoryDao ambassadorRewardHistoryDao;
    @Inject private AmbassadorSchoolRefDao ambassadorSchoolRefDao;
    @Inject private AmbassadorScoreHistoryDao ambassadorScoreHistoryDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public List<AmbassadorSchoolRef> findAmbassadorSchoolRefs(Long ambassadorId) {
        if (ambassadorId == null) return Collections.emptyList();
        return ambassadorSchoolRefDao.loadByAmbassadorId(ambassadorId);
    }

    @Override
    public List<AmbassadorSchoolRef> findSchoolAmbassadorRefs(Long schoolId) {
        if (schoolId == null) return Collections.emptyList();
        return ambassadorSchoolRefDao.findBySchoolId(schoolId);
    }

    // No cache control found on original implementation
    @Override
    public boolean haveBeAmbassador(Long teacherId) {
        if (teacherId == null) return false;
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(teacherId);
        Sort sort = new Sort(Sort.Direction.DESC, "UPDATE_DATETIME");
        Query query = Query.query(criteria).with(sort).limit(1);
        AmbassadorSchoolRef ref = ambassadorSchoolRefDao.query(query).stream().findFirst().orElse(null);
        return ref != null && DateUtils.calculateDateDay(new Date(), -30).before(ref.getUpdateDatetime());
    }

    @Override
    public Map<Long, Teacher> loadSchoolAmbassadors(Long schoolId) {
        List<AmbassadorSchoolRef> refList = ambassadorSchoolRefDao.findBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(refList)) {
            return Collections.emptyMap();
        }
        return teacherLoaderClient.loadTeachers(refList.stream().map(AmbassadorSchoolRef::getAmbassadorId).collect(Collectors.toSet()));
    }

    @Override
    public AmbassadorSchoolRef findSameSubjectAmbassadorInSchool(Subject subject, Long schoolId) {
        if (subject == null || schoolId == null) {
            return null;
        }
        List<AmbassadorSchoolRef> refList = ambassadorSchoolRefDao.findBySchoolId(schoolId);
        Set<Long> ambassadorIds = refList.stream()
                .map(AmbassadorSchoolRef::getAmbassadorId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ambassadorIds.isEmpty()) {
            return null;
        }
        Map<Long, TeacherSubjectRef> map = teacherLoaderClient.loadTeacherSubjectRefs(ambassadorIds);
        for (AmbassadorSchoolRef ref : refList) {
            TeacherSubjectRef tsr = map.get(ref.getAmbassadorId());
            if (tsr != null && tsr.getSubject() == subject) {
                return ref;
            }
        }
        return null;
    }

    @Override
    public AmbassadorAcademyRecord loadAmbassadorAcademyRecord(Long ambassadorId) {
        if (ambassadorId == null) return null;
        return ambassadorAcademyRecordDao.findByAmbassadorId(ambassadorId);
    }

    @Override
    public int loadAmbassadorTotalScore(Long ambassadorId, Date beginDate) {
        if (ambassadorId == null || beginDate == null) return 0;
        return ambassadorScoreHistoryDao.loadAmbassadorTotalScore(ambassadorId, beginDate);
    }

    @Override
    public List<AmbassadorScoreHistory> loadScoreHistory(Long ambassadorId, Date beginDate) {
        if (ambassadorId == null || beginDate == null) return Collections.emptyList();
        return ambassadorScoreHistoryDao.loadScoreHistory(ambassadorId, beginDate);
    }

    @Override
    public List<AmbassadorCompetitionDetail> findAmbassadorCompetitionDetails(Long teacherId) {
        if (teacherId == null) return Collections.emptyList();
        return ambassadorCompetitionDetailDao.loadByTeacherId(teacherId);
    }

    @Override
    public AmbassadorCompetition loadTeacherAmbassadorCompetition(Long teacherId) {
        if (teacherId == null) return null;
        return ambassadorCompetitionDao.loadByTeacherId(teacherId);
    }

    @Override
    public List<AmbassadorCompetition> findSchoolAmbassadorCompetitions(Long schoolId) {
        if (schoolId == null) return Collections.emptyList();
        return ambassadorCompetitionDao.loadBySchoolId(schoolId);
    }

    @Override
    public AmbassadorLevelDetail loadAmbassadorLevelDetail(Long ambassadorId) {
        if (ambassadorId == null) return null;
        return ambassadorLevelDetailDao.findByAmbassadorId(ambassadorId);
    }

    @Override
    public List<AmbassadorReportInfo> findAmbassadorReportInfos(Long teacherId) {
        if (teacherId == null) return Collections.emptyList();
        return ambassadorReportInfoDao.loadByTeacherId(teacherId);
    }

    @Override
    public List<AmbassadorReportInfo> findAmbassadorReportInfosByType(Integer type) {
        if (type == null) return Collections.emptyList();
        return ambassadorReportInfoDao.loadByType(type);
    }

    @Override
    public List<AmbassadorReportStudentFeedback> findAmbassadorReportStudentFeedbacks(Long teacherId) {
        if (teacherId == null) return Collections.emptyList();
        return ambassadorReportStudentFeedbackDao.loadByTeacherId(teacherId);
    }

    @Override
    public List<AmbassadorReportStudentFeedback> findAmbassadorReportStudentFeedbacks(Long teacherId, Long studentId) {
        if (teacherId == null || studentId == null) return Collections.emptyList();
        return ambassadorReportStudentFeedbackDao.loadByTeacherIdAndStudentId(teacherId, studentId);
    }

    @Override
    public Page<AmbassadorRewardHistory> findAmbassadorRewardHistories(Pageable pageable, String month, Integer status) {
        if (pageable == null || month == null || status == null) return new PageImpl<>(Collections.emptyList());
        return ambassadorRewardHistoryDao.find(pageable, month, status);
    }
}

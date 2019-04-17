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

package com.voxlearning.utopia.service.user.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.entity.crm.*;
import com.voxlearning.utopia.service.user.api.CrmSummaryLoader;
import lombok.Getter;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/5/10.
 */
public class CrmSummaryLoaderClient implements CrmSummaryLoader {

    @Getter
    @ImportService(interfaceClass = CrmSummaryLoader.class)
    private CrmSummaryLoader remoteReference;

    @Override
    public Map<Long, List<CrmTeacherSummary>> loadSchoolTeachers(Collection<Long> schoolIds) {
        return remoteReference.loadSchoolTeachers(schoolIds);
    }

    @Override
    public CrmTeacherSummary loadTeacherSummary(Long teacherId) {
        return remoteReference.loadTeacherSummary(teacherId);
    }

    @Override
    public CrmTeacherSummary loadTeacherSummaryByMobile(String teacherMobile) {
        return remoteReference.loadTeacherSummaryByMobile(teacherMobile);
    }

    @Override
    public Map<Long, CrmTeacherSummary> loadTeacherSummary(Collection<Long> teacherIds) {
        return remoteReference.loadTeacherSummary(teacherIds);
    }

    @Override
    public List<CrmTeacherSummary> loadSchoolTeachers(Long schoolId) {
        return remoteReference.loadSchoolTeachers(schoolId);
    }

    @Override
    public List<CrmTeacherSummary> loadTeacherSummaryByFakeFlag(Boolean fakeTeacher,String validationType) {
        return remoteReference.loadTeacherSummaryByFakeFlag(fakeTeacher,validationType);
    }

    @Override
    public List<CrmTeacherSummary> findByCountyCodes(Collection<Integer> countyCodes) {
        return remoteReference.findByCountyCodes(countyCodes);
    }

    @Override
    public CrmGroupSummary loadGroupSummary(Long groupId) {
        return remoteReference.loadGroupSummary(groupId);
    }

    @Override
    public Map<Long, CrmGroupSummary> loadGroupSummary(Collection<Long> groupIds) {
        return remoteReference.loadGroupSummary(groupIds);
    }

    @Override
    public List<CrmGroupSummary> loadTeacherGroupSummary(Long teacherId) {
        return remoteReference.loadTeacherGroupSummary(teacherId);
    }

    @Override
    public Map<Long, List<CrmGroupSummary>> loadTeachersGroupSummary(Collection<Long> teacherIds) {
        return remoteReference.loadTeachersGroupSummary(teacherIds);
    }

    @Override
    public List<CrmGroupSummary> loadSchoolGroupSummary(Long schoolId) {
        return remoteReference.loadSchoolGroupSummary(schoolId);
    }

    @Override
    public CrmSchoolSummary loadSchoolSummary(Long schoolId) {
        return remoteReference.loadSchoolSummary(schoolId);
    }

    @Override
    public Map<Long, CrmSchoolSummary> loadSchoolSummary(Collection<Long> schoolIds) {
        return remoteReference.loadSchoolSummary(schoolIds);
    }

    @Override
    public List<CrmSchoolSummary> findSchool(String schoolName, Integer limit) {
        return remoteReference.findSchool(schoolName, limit);
    }

    @Override
    public List<CrmSchoolSummary> findByCityCodesAndName(Collection<Integer> cityCodes, String schoolName) {
        return remoteReference.findByCityCodesAndName(cityCodes, schoolName);
    }

    @Override
    public List<CrmClazzlevelSummary> getCrmClazzlevelSummaryList(Long schoolId) {
        return remoteReference.getCrmClazzlevelSummaryList(schoolId);
    }

    @Override
    public List<CrmClazzSummary> getCrmClazzSummaryList(Long schoolId) {
        return remoteReference.getCrmClazzSummaryList(schoolId);
    }

    @Override
    public List<CrmGroupSummary> loadClassGroupSummary(Long classId) {
        return remoteReference.loadClassGroupSummary(classId);
    }
    @Override
    public Map<Long, List<CrmClazzSummary>> getCrmClazzSummaryBySchoolIds(Collection<Long> schoolIds) {
        return remoteReference.getCrmClazzSummaryBySchoolIds(schoolIds);
    }
    @Override
    public Map<Long, List<CrmGroupSummary>> getCrmGroupSummaryByClassIds(Collection<Long> classIds) {
        return remoteReference.getCrmGroupSummaryByClassIds(classIds);
    }
}

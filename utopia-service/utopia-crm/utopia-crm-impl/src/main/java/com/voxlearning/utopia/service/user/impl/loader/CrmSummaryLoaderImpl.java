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

package com.voxlearning.utopia.service.user.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.crm.*;
import com.voxlearning.utopia.service.user.api.CrmSummaryLoader;
import com.voxlearning.utopia.service.user.impl.dao.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


/**
 * Created by Summer Yang on 2016/5/10.
 */
@Named
@Service(interfaceClass = CrmSummaryLoader.class)
@ExposeService(interfaceClass = CrmSummaryLoader.class)
public class CrmSummaryLoaderImpl extends SpringContainerSupport implements CrmSummaryLoader {

    @Inject private CrmGroupSummaryDao crmGroupSummaryDao;
    @Inject private CrmSchoolSummaryDao crmSchoolSummaryDao;
    @Inject private CrmTeacherSummaryDao crmTeacherSummaryDao;
    @Inject private CrmClazzlevelSummaryDao crmClazzlevelSummaryDao;
    @Inject private CrmClazzSummaryDao crmClazzSummaryDao;

    @Override
    public Map<Long, List<CrmTeacherSummary>> loadSchoolTeachers(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        return crmTeacherSummaryDao.findBySchools(schoolIds);
    }

    // CrmTeacherSummary
    @Override
    public CrmTeacherSummary loadTeacherSummary(Long teacherId) {
        if (teacherId == null || teacherId.equals(0L)) {
            return null;
        }
        return crmTeacherSummaryDao.findByTeacherId(teacherId);
    }

    @Override
    public CrmTeacherSummary loadTeacherSummaryByMobile(String teacherMobile) {
        if (StringUtils.isBlank(teacherMobile)) {
            return null;
        }
        return crmTeacherSummaryDao.findByMobile(teacherMobile);
    }

    @Override
    public Map<Long, CrmTeacherSummary> loadTeacherSummary(Collection<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }
        return crmTeacherSummaryDao.findByTeacherIds(teacherIds);
    }

    @Override
    public List<CrmTeacherSummary> loadSchoolTeachers(Long schoolId) {
        if (schoolId == null || schoolId.equals(0L)) {
            return Collections.emptyList();
        }
        return crmTeacherSummaryDao.findBySchool(schoolId);
    }

    @Override
    public List<CrmTeacherSummary> loadTeacherSummaryByFakeFlag(Boolean fakeTeacher,String validationType) {
        return crmTeacherSummaryDao.findByFakeFlag(fakeTeacher,validationType);
    }

    @Override
    public List<CrmTeacherSummary> findByCountyCodes(Collection<Integer> countyCodes) {
        return crmTeacherSummaryDao.findByCountyCodes(countyCodes);
    }

    // CrmGroupSummary
    @Override
    public CrmGroupSummary loadGroupSummary(Long groupId) {
        if (groupId == null || groupId.equals(0L)) {
            return null;
        }

        return crmGroupSummaryDao.findByGroupId(groupId);
    }

    @Override
    public Map<Long, CrmGroupSummary> loadGroupSummary(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        return crmGroupSummaryDao.findByGroupIds(groupIds);
    }

    @Override
    public List<CrmGroupSummary> loadTeacherGroupSummary(Long teacherId) {
        if (teacherId == null || teacherId.equals(0L)) {
            return Collections.emptyList();
        }

        return crmGroupSummaryDao.findByTeacherId(teacherId);
    }

    @Override
    public Map<Long, List<CrmGroupSummary>> loadTeachersGroupSummary(Collection<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }
        return crmGroupSummaryDao.findByTeacherIds(teacherIds);
    }

    @Override
    public List<CrmGroupSummary> loadSchoolGroupSummary(Long schoolId) {
        if (schoolId == null || schoolId.equals(0L)) {
            return Collections.emptyList();
        }

        return crmGroupSummaryDao.findBySchoolId(schoolId);
    }

    // CrmSchoolSummary
    @Override
    public CrmSchoolSummary loadSchoolSummary(Long schoolId) {
        if (schoolId == null || schoolId.equals(0L)) {
            return null;
        }
        return crmSchoolSummaryDao.findBySchoolId(schoolId);
    }

    @Override
    public Map<Long, CrmSchoolSummary> loadSchoolSummary(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        return crmSchoolSummaryDao.findBySchoolIds(schoolIds);
    }

    @Override
    public List<CrmSchoolSummary> findSchool(String schoolName, Integer limit) {
        if (StringUtils.isBlank(schoolName)) {
            return Collections.emptyList();
        }

        return crmSchoolSummaryDao.findBySchoolName(schoolName, limit);
    }

    @Override
    public List<CrmSchoolSummary> findByCityCodesAndName(Collection<Integer> cityCodes, String schoolName){
        if (CollectionUtils.isEmpty(cityCodes)) {
            return Collections.emptyList();
        }

        return crmSchoolSummaryDao.findByCityCodesAndName(cityCodes, schoolName);
    }

    @Override
    public List<CrmClazzlevelSummary> getCrmClazzlevelSummaryList(Long schoolId){
        if (null != schoolId && !schoolId.equals(0L)) {
            return crmClazzlevelSummaryDao.loadBySchoolId(schoolId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<CrmClazzSummary> getCrmClazzSummaryList(Long schoolId){
        if (null != schoolId && !schoolId.equals(0L)) {
            return crmClazzSummaryDao.loadBySchoolId(schoolId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<CrmGroupSummary> loadClassGroupSummary(Long classId) {
        if (classId == null || classId.equals(0L)) {
            return Collections.emptyList();
        }
        return crmGroupSummaryDao.findByClazzId(classId);
    }

    @Override
    public Map<Long, List<CrmClazzSummary>> getCrmClazzSummaryBySchoolIds(Collection<Long> schoolIds){
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            return crmClazzSummaryDao.loadBySchoolIds(schoolIds);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<Long, List<CrmGroupSummary>> getCrmGroupSummaryByClassIds(Collection<Long> classIds){
        if (CollectionUtils.isNotEmpty(classIds)) {
            return crmGroupSummaryDao.findByClazzIds(classIds);
        }
        return Collections.emptyMap();
    }

}

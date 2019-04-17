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

package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedSchoolServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/6/24
 */
@Named
public class TeacherSummaryService extends AbstractAgentService {

    @Inject TeacherLoaderClient teacherLoaderClient;
    @Inject
    BaseOrgService baseOrgService;
    @Inject CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;
    public CrmTeacherSummary load(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return crmSummaryLoaderClient.loadTeacherSummary(teacherId);
    }

    public CrmTeacherSummary load(Long teacherId, Long userId) {
        if (teacherId == null || userId == null) {
            return null;
        }
        CrmTeacherSummary teacher = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
        return filterSchool(teacher, userId);
    }

    public List<CrmTeacherSummary> load(Collection<Long> teacherIds, Long userId) {
        if (teacherIds == null || teacherIds.isEmpty() || userId == null) {
            return null;
        }
        Map<Long, CrmTeacherSummary> teachers = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);
        List<CrmTeacherSummary> teacherSummaries = new ArrayList<>();
        teacherSummaries.addAll(teachers.values());
        return filterSchool(teacherSummaries, userId);
    }

    private CrmTeacherSummary filterSchool(CrmTeacherSummary teacher, Long userId) {
        if (teacher == null) {
            return null;
        }
        if (!isJunior(teacher.getSchoolLevel())) {
            return null;
        }
        List<Long> iSchools = baseOrgService.getManagedSchoolList(userId);
        if(CollectionUtils.isNotEmpty(iSchools) && iSchools.contains(teacher.getSchoolId())){
            return teacher;
        }
        return null;
    }

    private List<CrmTeacherSummary> filterSchool(List<CrmTeacherSummary> teachers, Long userId) {
        if (teachers == null || teachers.isEmpty()) {
            return teachers;
        }
        List<CrmTeacherSummary> buffer = teachers.stream()
                .filter(teacher -> teacher != null)
                .filter(teacher -> isJunior(teacher.getSchoolLevel()))
                .collect(Collectors.toList());
        List<Long> iSchools = baseOrgService.getManagedSchoolList(userId);
        if(CollectionUtils.isNotEmpty(iSchools)){
            return buffer.stream().filter(p -> iSchools.contains(p.getSchoolId())).collect(Collectors.toList());
        }
        return null;
    }


    private boolean isJunior(SchoolLevel schoolLevel) {
        return schoolLevel == null || schoolLevel == SchoolLevel.JUNIOR;
    }

    private boolean isJunior(String level) {
        if (StringUtils.isBlank(level)) {
            return false;
        }
        try {
            SchoolLevel schoolLevel = SchoolLevel.valueOf(level);
            return isJunior(schoolLevel);
        } catch (Exception ignored) {
            return false;
        }
    }

}

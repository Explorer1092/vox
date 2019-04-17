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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.dao.CrmUGCStudentCountDao;
import com.voxlearning.utopia.admin.dao.CrmUGCStudentCountDetailDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCStudentOfClass;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCStudentOfClassDetail;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhuan liu
 * @since 2016/1/25.
 */

@Named
public class CrmUGCStudentCountService extends AbstractAdminService {

    private static final int UGC_STUDENTCOUNT_DUPLICATE = 0; // 触发条件包括两种类型
    private static final int UGC_STUDENTCOUNT_CHANGED = 3;//答案变更
    private static final int UGC_STUDENTCOUNT_UNMATCH_SYSTEM = 4;//与系统年级不符

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private CrmUGCStudentCountDao crmUGCStudentCountDao;
    @Inject
    private CrmUGCStudentCountDetailDao crmUGCStudentCountDetailDao;

    public List<CrmUGCStudentOfClassDetail> crmUGCClassDetails(Long clazzId) {
        return crmUGCStudentCountDetailDao.findUgcStudentCountDetailTop5(clazzId);
    }

    public Page<CrmUGCStudentOfClass> crmUgcStudentCount(Integer trigger, Pageable pageable) {

        Page<CrmUGCStudentOfClass> result = null;
        switch (trigger) {
            case UGC_STUDENTCOUNT_DUPLICATE:
                result = crmUGCStudentCountDao.findCrmUgcStudentCountIn(Arrays.asList(1, 2), pageable);
                filSchoolNameAndClassName(result);
                break;
            case UGC_STUDENTCOUNT_CHANGED:
                result = crmUGCStudentCountDao.findCrmUgcStudentCountAnswerChange(trigger, pageable);
                filSchoolNameAndClassName(result);
                break;
            case UGC_STUDENTCOUNT_UNMATCH_SYSTEM:
                result = crmUGCStudentCountDao.findCrmUgcStudentCountIs(trigger, pageable);
                filSchoolNameAndClassName(result);
                break;

        }
        return result;

    }

    public void updateUgcStudentCount(Long schoolId, Long clazzId, String updatedUgcStudentCount) {

        CrmUGCStudentOfClass studentCount = crmUGCStudentCountDao.findUgcStudentCountByClazzId(schoolId, clazzId);
        if (studentCount != null && !StringUtils.isEmpty(updatedUgcStudentCount)) {
            studentCount.setUgcStudentCount(updatedUgcStudentCount);
            crmUGCStudentCountDao.update(studentCount.getId(), studentCount);
        }
    }

    public void filSchoolNameAndClassName(Page<CrmUGCStudentOfClass> result) {

        List<Long> schoolIds = result.getContent().stream().map(CrmUGCStudentOfClass::getSchoolId).collect(Collectors.toList());

        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        for (CrmUGCStudentOfClass crmUGCStudentOfClass : result) {
            School school = schoolMap.get(crmUGCStudentOfClass.getSchoolId());
            if (school != null) {
                crmUGCStudentOfClass.setSchoolName(school.getCname());
            }
            crmUGCStudentOfClass.setClassName(assembleClassName(crmUGCStudentOfClass.getClazzId()));
            crmUGCStudentOfClass.setSysStudentCount(studentCount(crmUGCStudentOfClass.getClazzId()));
        }

    }

    public String assembleClassName(Long clazzId) {

        String clazzName = "";
        if (clazzId != null) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz != null) {
                clazzName = formalizeClazzName(clazz.getClassLevel(), clazz.getClassName(), clazz.getEduSystem());
            }
        }
        return clazzName;

    }

    public Integer studentCount(Long clazzId) {

        List<GroupMapper> clazzGroups = deprecatedGroupLoaderClient.loadClazzGroups(clazzId);

        Set<Long> stuSet = new HashSet<>();
        if (clazzGroups != null && (!clazzGroups.isEmpty())) {
            for (GroupMapper mapper : clazzGroups) {
                GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(mapper.getId(), true);
                List<GroupMapper.GroupUser> studentList = groupMapper.getStudents();
                for (GroupMapper.GroupUser student : studentList) {
                    stuSet.add(student.getId());
                }

            }
        }
        return stuSet.size() == 0 ? null : stuSet.size();
    }

    private String formalizeClazzName(String clazzLevel, String clazzName, EduSystemType eduSystemType) {
        int jie = ClassJieHelper.fromClazzLevel(ClazzLevel.parse(Integer.valueOf(clazzLevel)));
        int level = ClassJieHelper.toClazzLevel(jie, eduSystemType).getLevel();
        return level + "年级" + clazzName;
    }

    public long getCrmUgcStudentsCount() {
        return crmUGCStudentCountDao.getCrmUgcStudentsCount();
    }

    public long getCrmUgcStudentCountDetailCount() {
        return crmUGCStudentCountDetailDao.getUgcStudentCountDetailCount();
    }

    public List<CrmUGCStudentOfClass> allCrmUgcStudentCountData(int limit, int skip) {
        return crmUGCStudentCountDao.allCrmUgcStudentCountData(limit, skip);
    }

    public List<CrmUGCStudentOfClassDetail> allCrmUgcClassDetailData(int limit, int skip) {
        return crmUGCStudentCountDetailDao.allUgcStudentCountDetails(limit, skip);
    }
}

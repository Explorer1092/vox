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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.dao.CrmUGCGradeClassDao;
import com.voxlearning.utopia.admin.dao.CrmUGCGradeClassDetailDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeClass;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeClassDetail;
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
 * @since 2016/1/27.
 */

@Named
public class CrmUGCGradeClassService extends AbstractAdminService {

    private static final int UGC_GRADECLASSNAME_DUPLICATE = 0; // 触发条件包括两种类型
    private static final int UGC_GRADECLASSNAME_CHANGED = 3;//答案变更
    private static final int UGC_GRADECLASSNAME_UNMATCH_SYSTEM = 4;//与系统年级不符

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private CrmUGCGradeClassDao crmUGCGradeClassDao;
    @Inject
    private CrmUGCGradeClassDetailDao crmUGCGradeClassDetailDao;

    public Page<CrmUGCGradeClass> crmUGCGradeClass(Integer triggerType, Pageable pageable) {

        Page<CrmUGCGradeClass> result = null;
        switch (triggerType) {
            case UGC_GRADECLASSNAME_DUPLICATE:
                result = crmUGCGradeClassDao.findCrmUGCGradeClassIn(Arrays.asList(1, 2), pageable);
                fillUgcSchoolAndGradeName(result);
                break;
            case UGC_GRADECLASSNAME_CHANGED:
                result = crmUGCGradeClassDao.ugcGradeClassNameAnswerChange(UGC_GRADECLASSNAME_CHANGED, pageable);
                fillUgcSchoolAndGradeName(result);
                break;
            case UGC_GRADECLASSNAME_UNMATCH_SYSTEM:

                result = crmUGCGradeClassDao.findCrmUGCGradeClassIs(UGC_GRADECLASSNAME_UNMATCH_SYSTEM, pageable);
                fillUgcSchoolAndGradeName(result);
                break;
        }
        return result;
    }

    public void updateUgcGradeClassName(Long schoolId, String grade, String updatedUgcClassName) {

        CrmUGCGradeClass gradeClass = crmUGCGradeClassDao.findCrmUGCGradeClassBySchoolIdAndGrade(schoolId, grade);

        if (gradeClass != null && !StringUtils.isEmpty(updatedUgcClassName)) {
            gradeClass.setUgcClassName(updatedUgcClassName);
        }
        crmUGCGradeClassDao.update(gradeClass.getId(), gradeClass);

    }

    private void fillUgcSchoolAndGradeName(Page<CrmUGCGradeClass> result) {
        List<Long> schoolIds = result.getContent().stream().map(CrmUGCGradeClass::getSchoolId).collect(Collectors.toList());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();


        List<CrmUGCGradeClass> temp = result.getContent();
        for (CrmUGCGradeClass crmUGCGradeClass : temp) {
            School school = schoolMap.get(crmUGCGradeClass.getSchoolId());

            String classLevel = getClassName(crmUGCGradeClass.getSchoolId(), crmUGCGradeClass.getGrade());
            if (school != null) {
                crmUGCGradeClass.setSchoolName(school.getCname());
            }
            crmUGCGradeClass.setClassName(classLevel);
        }
    }

    public String getClassName(Long schoolId, String grade) {

        List<Clazz> clazzInfoList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .toList();
        List<Long> clazzIds = clazzInfoList.stream().map(Clazz::getId)
                .collect(Collectors.toList());
        List<GroupMapper> clazzGroups = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds).values()
                .stream().flatMap(Collection::stream)
                .collect(Collectors.toList());

        Set<Long> clazzExistGroupIds = new HashSet<>();
        Map<String, List<String>> gradeClassMap = new HashMap<>();

        for (GroupMapper mapper : clazzGroups) {
            if (mapper != null && !clazzExistGroupIds.contains(mapper.getClazzId())) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(mapper.getClazzId());
                if (clazz != null) {
                    String classLevelGrade = clazz.getClassLevel();

                    if (!gradeClassMap.containsKey(classLevelGrade)) {
                        List<String> classNameList = new ArrayList<>();
                        classNameList.add(clazz.getClassName());
                        gradeClassMap.put(classLevelGrade, classNameList);
                    } else {
                        gradeClassMap.get(classLevelGrade).add(clazz.getClassName());
                    }

                }
                clazzExistGroupIds.add(clazz.getId());
            }

        }
        List<String> clazzList = gradeClassMap.get(grade);
        return StringUtils.join(clazzList, ",");
    }


    public long getUgcGradeClassCount() {

        return crmUGCGradeClassDao.getUgcGradeClassCount();
    }

    public long getUgcGradeClassDetailCount() {
        return crmUGCGradeClassDetailDao.getUgcGradeClassDetailCount();
    }

    public List<CrmUGCGradeClassDetail> getCrmUgcGradeClassDetail(Long schoolId) {
        return crmUGCGradeClassDetailDao.findUGCGradeClassDetailTop5(schoolId);
    }

    public List<CrmUGCGradeClass> allUgcGradeClassData(int limit, int skip) {

        return crmUGCGradeClassDao.allUgcGradeClassData(limit, skip);
    }

    public List<CrmUGCGradeClassDetail> allUgcGradeClassDetailData(int limit, int skip) {
        return crmUGCGradeClassDetailDao.allUgcGradeClassDetailData(limit, skip);
    }


}

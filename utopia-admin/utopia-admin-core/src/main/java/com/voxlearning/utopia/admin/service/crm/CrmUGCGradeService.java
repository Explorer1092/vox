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
import com.voxlearning.utopia.admin.dao.CrmUGCGradeDao;
import com.voxlearning.utopia.admin.dao.CrmUGCGradeDetailDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGrade;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCGradeDetail;
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
 * @since 2016/1/18.
 */

@Named
public class CrmUGCGradeService extends AbstractAdminService {

    private static final int UGC_GRADENAME_DUPLICATE = 0; // 触发条件包括两种类型
    private static final int UGC_GRADENAME_CHANGED = 3;//答案变更
    private static final int UGC_GRADENAME_UNMATCH_SYSTEM = 4;//与系统年级不符

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private CrmUGCGradeDao crmUGCGradeDao;
    @Inject
    private CrmUGCGradeDetailDao crmUGCGradeDetailDao;

    public Page<CrmUGCGrade> crmUGCGrades(Integer triggerType, Pageable pageable) {

        Page<CrmUGCGrade> result = null;
        switch (triggerType) {
            case UGC_GRADENAME_DUPLICATE:
                result = crmUGCGradeDao.findCrmUGCGradeIn(Arrays.asList(1, 2), pageable);
                fillUgcSchoolAndGradeName(result);
                break;
            case UGC_GRADENAME_CHANGED:
                result = crmUGCGradeDao.ugcGradeNameAnswerChange(UGC_GRADENAME_CHANGED, pageable);
                fillUgcSchoolAndGradeName(result);
                break;
            case UGC_GRADENAME_UNMATCH_SYSTEM:

                result = crmUGCGradeDao.findCrmUGCGradeIs(UGC_GRADENAME_UNMATCH_SYSTEM, pageable);
                fillUgcSchoolAndGradeName(result);
                break;
        }
        return result;
    }

    public void updateUgcGradeName(Long schoolId, String updatedUgcGradeName) {
        if (schoolId == null) {
            return;
        }
        CrmUGCGrade crmUGCGrade = crmUGCGradeDao.findUGCGradeBySchoolId(schoolId);
        if (crmUGCGrade != null && !StringUtils.isEmpty(updatedUgcGradeName)) {
            crmUGCGrade.setUgcGradeNames(updatedUgcGradeName);
            crmUGCGradeDao.update(crmUGCGrade.getId(), crmUGCGrade);
        }
    }

    private void fillUgcSchoolAndGradeName(Page<CrmUGCGrade> result) {
        List<Long> schoolIds = result.getContent().stream().map(CrmUGCGrade::getSchoolId).collect(Collectors.toList());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();


        List<CrmUGCGrade> temp = result.getContent();
        for (CrmUGCGrade crmUGCGrade : temp) {
            School school = schoolMap.get(crmUGCGrade.getSchoolId());

            String classLevel = getGradeName(crmUGCGrade.getSchoolId());
            if (school != null) {
                crmUGCGrade.setSchoolName(school.getCname());
            }
            crmUGCGrade.setGradeName(classLevel);
        }
    }

    public String getGradeName(Long schoolId) {

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
        Set<String> clazzList = new HashSet<>();
        for (GroupMapper mapper : clazzGroups) {
            if (mapper != null && !clazzExistGroupIds.contains(mapper.getClazzId())) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(mapper.getClazzId());
                if (clazz != null) {
                    clazzList.add(clazz.getClassLevel());

                }
            }
        }
        //不需要显示99级
        clazzList.remove(String.valueOf(ClazzLevel.PRIMARY_GRADUATED.getLevel()));
        return StringUtils.join(clazzList, ",");
    }


    public long getUgcGradeCount() {

        return crmUGCGradeDao.getUgcGradeCount();
    }

    public long getUgcGradeDetailCount() {
        return crmUGCGradeDetailDao.getUgcGradeDetailCount();
    }

    public List<CrmUGCGradeDetail> getCrmUgcGradeDetail(Long schoolId) {
        return crmUGCGradeDetailDao.findUGCGradeDetailTop5(schoolId);
    }

    public List<CrmUGCGrade> allUgcGradeData(int limit, int skip) {

        return crmUGCGradeDao.allUgcGradeData(limit, skip);
    }

    public List<CrmUGCGradeDetail> allUgcGradeDetailData(int limit, int skip) {
        return crmUGCGradeDetailDao.allUgcGradeDetailData(limit, skip);
    }


}

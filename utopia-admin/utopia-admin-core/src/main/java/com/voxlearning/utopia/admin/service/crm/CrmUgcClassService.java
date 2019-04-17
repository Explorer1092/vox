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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.dao.CrmUGCClassDao;
import com.voxlearning.utopia.admin.dao.CrmUGCClassDetailDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCClass;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCClassDetail;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Zhuan liu
 * @since 2016/1/21.
 */

@Named
public class CrmUgcClassService extends AbstractAdminService {

    private static final int UGC_CLASSNAME_DUPLICATE = 0; // 触发条件包括两种类型
    private static final int UGC_CLASSNAME_CHANGED = 3;//答案变更
    private static final int UGC_CLASSNAME_UNMATCH_SYSTEM = 4;//与系统年级不符

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private CrmUGCClassDao crmUGCClassDao;
    @Inject
    private CrmUGCClassDetailDao crmUGCClassDetailDao;

    public List<CrmUGCClassDetail> crmUGCClassDetails(Long groupId) {
        return crmUGCClassDetailDao.findUgcClassDetailTop5(groupId);
    }

    public Page<CrmUGCClass> crmUgcClasses(Integer trigger, Pageable pageable) {

        Page<CrmUGCClass> result = null;
        switch (trigger) {
            case UGC_CLASSNAME_DUPLICATE:
                result = crmUGCClassDao.findCrmUgcClassIn(Arrays.asList(1, 2), pageable);
                filSchoolNameAndClassName(result);
                break;
            case UGC_CLASSNAME_CHANGED:
                result = crmUGCClassDao.findCrmUgcClassAnswerChange(trigger, pageable);
                filSchoolNameAndClassName(result);
                break;
            case UGC_CLASSNAME_UNMATCH_SYSTEM:
                result = crmUGCClassDao.findCrmUgcClassIs(trigger, pageable);
                filSchoolNameAndClassName(result);
                break;

        }
        return result;

    }

    public void updateUgcClassName(Long schoolId, Long groupId, String updatedUgcClassName) {
        CrmUGCClass crmUGCClass = crmUGCClassDao.findUgcClassBySchoolIdAndGroupId(schoolId, groupId);
        if (crmUGCClass != null) {
            crmUGCClass.setUgcClassName(updatedUgcClassName);
            crmUGCClassDao.update(crmUGCClass.getId(), crmUGCClass);
        }
    }

    public void filSchoolNameAndClassName(Page<CrmUGCClass> result) {

        List<Long> schoolIds = result.getContent().stream().map(CrmUGCClass::getSchoolId).collect(Collectors.toList());

        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        for (CrmUGCClass crmUGCClass : result) {
            School school = schoolMap.get(crmUGCClass.getSchoolId());
            if (school != null) {
                crmUGCClass.setSchoolName(school.getCname());
            }
            crmUGCClass.setClassName(assembleClassName(crmUGCClass.getGroupId()));
        }

    }

    public String assembleClassName(Long groupId) {

        GroupMapper groups = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        String clazzName = "";
        if (groups != null) {
            Long clazzId = groups.getClazzId();
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz != null) {
                clazzName = formalizeClazzName(clazz.getClassLevel(), clazz.getClassName(), clazz.getEduSystem());
            }
        }
        return clazzName;

    }

    private String formalizeClazzName(String clazzLevel, String clazzName, EduSystemType eduSystemType) {
        int jie = ClassJieHelper.fromClazzLevel(ClazzLevel.parse(Integer.valueOf(clazzLevel)));
        int level = ClassJieHelper.toClazzLevel(jie, eduSystemType).getLevel();
        return level + "年级" + clazzName;
    }

    public long getCrmUgcClassCount() {
        return crmUGCClassDao.getCrmUgcClassCount();
    }

    public long getCrmUgcClassDetailCount() {
        return crmUGCClassDetailDao.getUgcClassDetailCount();
    }

    public List<CrmUGCClass> allCrmUgcClassData(int limit, int skip) {
        return crmUGCClassDao.allCrmUgcClassData(limit, skip);
    }

    public List<CrmUGCClassDetail> allCrmUgcClassDetailData(int limit, int skip) {
        return crmUGCClassDetailDao.allUgcClassDetails(limit, skip);
    }
}

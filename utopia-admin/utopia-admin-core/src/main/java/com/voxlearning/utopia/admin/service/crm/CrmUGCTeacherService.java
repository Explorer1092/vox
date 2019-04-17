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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.admin.dao.CrmUGCTeacherDao;
import com.voxlearning.utopia.admin.dao.CrmUGCTeacherDetailDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCTeacher;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCTeacherDetail;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhuan liu
 * @since 2016/1/28.
 */

@Named
public class CrmUGCTeacherService extends AbstractAdminService {
    private static final int UGC_TEACHERNAME_DUPLICATE = 0; // 触发条件包括两种类型
    private static final int UGC_TEACHERNAME_CHANGED = 3;//答案变更
    private static final int UGC_TEACHERNAME_UNMATCH_SYSTEM = 4;//与系统年级不符
    private static final Map<Integer, String> SUBJECT_MAP = new HashMap<>();

    static {
        SUBJECT_MAP.put(1, "English");
        SUBJECT_MAP.put(2, "Math");
        SUBJECT_MAP.put(3, "Chinese");
    }

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private CrmUGCTeacherDao crmUGCTeacherDao;
    @Inject
    private CrmUGCTeacherDetailDao crmUGCTeacherDetailDao;

    public List<CrmUGCTeacherDetail> crmUGCTeacherDetails(Long clazzId) {
        return crmUGCTeacherDetailDao.findUgcTeacherDetailTop5(clazzId);
    }

    public Page<CrmUGCTeacher> crmUgcTeacheres(Integer trigger, Integer subjectTrigger, Pageable pageable) {

        Page<CrmUGCTeacher> result = null;
        switch (trigger) {
            case UGC_TEACHERNAME_DUPLICATE:
                result = crmUGCTeacherDao.findCrmUgcTeacherIn(Arrays.asList(1, 2), SUBJECT_MAP.get(subjectTrigger), pageable);
                filSchoolNameAndTeacherName(result);
                break;
            case UGC_TEACHERNAME_CHANGED:
                result = crmUGCTeacherDao.findCrmUgcTeacherAnswerChange(trigger, SUBJECT_MAP.get(subjectTrigger), pageable);
                filSchoolNameAndTeacherName(result);
                break;
            case UGC_TEACHERNAME_UNMATCH_SYSTEM:
                result = crmUGCTeacherDao.findCrmUgcTeacherIs(trigger, SUBJECT_MAP.get(subjectTrigger), pageable);
                filSchoolNameAndTeacherName(result);
                break;

        }
        return result;

    }

    public void updateUgcTeacherName(Long schoolId, Long clazzId, String subject, String updatedUgcTeacherName) {

        CrmUGCTeacher crmUGCTeacher = crmUGCTeacherDao.findUgcTeacherBySubject(schoolId, clazzId, subject);
        if (crmUGCTeacher != null && !StringUtils.isEmpty(updatedUgcTeacherName)) {
            crmUGCTeacher.setUgcTeacherName(updatedUgcTeacherName);
            crmUGCTeacherDao.update(crmUGCTeacher.getId(), crmUGCTeacher);
        }

    }

    public void filSchoolNameAndTeacherName(Page<CrmUGCTeacher> result) {

        List<Long> schoolIds = result.getContent().stream().map(CrmUGCTeacher::getSchoolId).collect(Collectors.toList());

        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        for (CrmUGCTeacher crmUGCTeacher : result) {
            School school = schoolMap.get(crmUGCTeacher.getSchoolId());
            if (school != null) {
                crmUGCTeacher.setSchoolName(school.getCname());
            }
            crmUGCTeacher.setClassName(assembleClassName(crmUGCTeacher.getClazzId()));
            crmUGCTeacher.setSysTeacherName(getTeacherName(crmUGCTeacher.getClazzId(), crmUGCTeacher.getSubject()));
        }
    }

    public String assembleClassName(Long clazzId) {

        String clazzName = "";
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz != null) {
            clazzName = formalizeClazzName(clazz.getClassLevel(), clazz.getClassName(), clazz.getEduSystem());
        }
        return clazzName;
    }

    public String getTeacherName(Long clazzId, String subjects) {

        Map<String, List<String>> teacherMap = new HashMap<>();

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz != null) {
            Set<Long> clazzGroupIds = deprecatedGroupLoaderClient.loadClazzGroups(clazzId).stream().map(GroupMapper::getId).collect(Collectors.toSet());
            List<GroupTeacherTuple> groupTeacherRefList = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByGroupIds(clazzGroupIds);

            for (GroupTeacherTuple clazzTeacherRef : groupTeacherRefList) {

                Teacher teacher = teacherLoaderClient.loadTeacher(clazzTeacherRef.getTeacherId());

                if (teacher != null) {
                    String subject = teacher.getSubject() == null ? Subject.UNKNOWN.getValue() : teacher.getSubject().getValue();
                    if (!teacherMap.containsKey(subject)) {
                        List<String> classNameList = new ArrayList<>();
                        classNameList.add(teacher.getProfile() == null ? "" : teacher.getProfile().getRealname());
                        teacherMap.put(subject, classNameList);
                    } else {
                        teacherMap.get(subject).add(teacher.getProfile() == null ? "" : teacher.getProfile().getRealname());
                    }
                }
            }
        }

        List<String> teacherNameList = teacherMap.get(subjects);
        return StringUtils.join(teacherNameList, ",");
    }

    private String formalizeClazzName(String clazzLevel, String clazzName, EduSystemType eduSystemType) {
        int jie = ClassJieHelper.fromClazzLevel(ClazzLevel.parse(Integer.valueOf(clazzLevel)));
        int level = ClassJieHelper.toClazzLevel(jie, eduSystemType).getLevel();
        return level + "年级" + clazzName;
    }

    public long getCrmUgcTeacherCount() {
        return crmUGCTeacherDao.getCrmUgcTeacherCount();
    }

    public long getCrmUgcTeacherDetailCount() {
        return crmUGCTeacherDetailDao.getUgcTeacherDetailCount();
    }

    public List<CrmUGCTeacher> allCrmUgcTeacherData(int limit, int skip) {
        return crmUGCTeacherDao.allCrmUgcTeacherData(limit, skip);
    }

    public List<CrmUGCTeacherDetail> allCrmUgcTeacherDetailData(int limit, int skip) {
        return crmUGCTeacherDetailDao.allUgcTeacherDetails(limit, skip);
    }
}

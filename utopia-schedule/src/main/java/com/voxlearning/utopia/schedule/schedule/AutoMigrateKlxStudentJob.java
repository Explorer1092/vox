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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.NewClazzServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.ClazzServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 快乐学学生数据迁移任务
 * Created by alex on 2017/6/21.
 */
@Named
@ScheduledJobDefinition(
        jobName = "学前学校迁移任务",
        jobDescription = "学前学校迁移任务",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 */5 * * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoMigrateKlxStudentJob extends ScheduledJobWithJournalSupport {

    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolServiceClient schoolServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherServiceClient teacherServiceClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private NewClazzServiceClient newClazzServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String i2pIds = SafeConverter.toString(parameters.get("i2p"));
        if (StringUtils.isNotBlank(i2pIds)) {
            String[] i2pSchoolIds = i2pIds.split(",");
            for (String schoolId : i2pSchoolIds) {
                if (StringUtils.isBlank(schoolId)) {
                    continue;
                }

                logger.info("migrating school {} from infant to primary", schoolId);
                School school = schoolLoaderClient.getSchoolLoader().loadSchool(SafeConverter.toLong(schoolId)).getUninterruptibly();
                if (school != null) {
                    migrateI2p(school);
                }
                progressMonitor.worked(1);
            }
        }

        String p2iIds = SafeConverter.toString(parameters.get("p2i"));
        if (StringUtils.isNotBlank(p2iIds)) {
            String[] p2iSchoolIds = p2iIds.split(",");
            for (String schoolId : p2iSchoolIds) {
                if (StringUtils.isBlank(schoolId)) {
                    continue;
                }

                logger.info("migrating school {} from primary to infant", schoolId);
                School school = schoolLoaderClient.getSchoolLoader().loadSchool(SafeConverter.toLong(schoolId)).getUninterruptibly();
                if (school != null) {
                    migrateP2i(school);
                }
                progressMonitor.worked(1);
            }
        }

        progressMonitor.done();
    }

    // 学前到小学
    private void migrateI2p(School school) {
        // step 1. process all class
        Collection<Long> clazzIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .originalLocationsAsList()
                .stream()
                .map(Clazz.Location::getId)
                .collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        for (Clazz clazz : clazzs.values()) {
            // 2016 == 2017   2015 == 2016  2014 == 2015  2013 == 2014  2011 or 2012 毕业班，检查看是否有数据
            ClazzLevel clazzLevel = ClazzLevel.FIRST_GRADE;
            if ("2017".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.FIRST_GRADE;
            } else if ("2016".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.SECOND_GRADE;
            } else if ("2015".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.THIRD_GRADE;
            } else if ("2014".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.FOURTH_GRADE;
            } else if ("2012".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.FIFTH_GRADE;
            } else if ("2011".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.SIXTH_GRADE;
            }

            newClazzServiceClient.getNewClazzService().updateClazzLevel(clazz.getId(), String.valueOf(clazzLevel.getLevel()), EduSystemType.P6.name());
        }

        // step 2. update all teacher subject ref
        List<UserSchoolRef> userSchoolRefs = schoolLoaderClient.getSchoolLoader().findUserSchoolRefsBySchoolId(school.getId()).getUninterruptibly();
        if (CollectionUtils.isNotEmpty(userSchoolRefs)) {
            Collection<Long> teacherIds = userSchoolRefs.stream().map(UserSchoolRef::getUserId).collect(Collectors.toSet());
            Map<Long, TeacherSubjectRef> teacherSubjectRefMap = teacherLoaderClient.loadTeacherSubjectRefs(teacherIds);
            for (TeacherSubjectRef tsr : teacherSubjectRefMap.values()) {
                teacherServiceClient.setTeacherSubject(tsr.getUserId(), tsr.getSubject(), Ktwelve.PRIMARY_SCHOOL);
            }
        }

        // step 3. update school level to infant
        school.setLevel(SchoolLevel.JUNIOR.getLevel());
        schoolServiceClient.getSchoolService().updateSchool(school).awaitUninterruptibly();

    }

    // 小学到学前
    private void migrateP2i(School school) {
        // step 1. process all class
        Collection<Long> clazzIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .originalLocationsAsList()
                .stream()
                .map(Clazz.Location::getId)
                .collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        // 先把1-4年级都处理了
        for (Clazz clazz : clazzs.values()) {
            // 2016 == 2017   2015 == 2016  2014 == 2015  2013 == 2014  2011 or 2012 毕业班，检查看是否有数据
            ClazzLevel clazzLevel = ClazzLevel.INFANT_FIRST;
            if ("2017".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.INFANT_FIRST;
            } else if ("2016".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.INFANT_SECOND;
            } else if ("2015".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.INFANT_THIRD;
            } else if ("2014".equals(clazz.getJie())) {
                clazzLevel = ClazzLevel.INFANT_FOURTH;
            } else {
                continue;
            }

            newClazzServiceClient.getNewClazzService().updateClazzLevel(clazz.getId(), String.valueOf(clazzLevel.getLevel()), EduSystemType.I4.name());
        }

        // 这次只处理5-6年级的
        for (Clazz clazz : clazzs.values()) {
            if (!"2013".equals(clazz.getJie()) && !"2012".equals(clazz.getJie())) {
                continue;
            }

            // 对于其他的都往4年级上挂
            List<GroupMapper> clazzGroups = deprecatedGroupLoaderClient.loadClazzGroups(clazz.getId());
            if (CollectionUtils.isEmpty(clazzGroups)) {
                continue;
            }

            Clazz infantFourClazz = clazzs.values().stream()
                    .filter(p -> !Objects.equals(p.getId(), clazz.getId()))
                    .filter(p -> "2014".equals(p.getJie()))
                    .filter(p -> Objects.equals(p.getClassName(), clazz.getClassName()))
                    .findFirst().orElse(null);
            if (infantFourClazz == null) {
                newClazzServiceClient.getNewClazzService().updateClazzLevel(clazz.getId(), String.valueOf(ClazzLevel.INFANT_FOURTH.getLevel()), EduSystemType.I4.name());
                continue;
            }

            for (GroupMapper group : clazzGroups) {
                clazzServiceClient.updateClazzDataForSystemClazzMove(0L, group.getClazzId(), infantFourClazz.getId(), group.getId());
            }
        }

        // step 2. update all teacher subject ref
        List<UserSchoolRef> userSchoolRefs = schoolLoaderClient.getSchoolLoader().findUserSchoolRefsBySchoolId(school.getId()).getUninterruptibly();
        if (CollectionUtils.isNotEmpty(userSchoolRefs)) {
            Collection<Long> teacherIds = userSchoolRefs.stream().map(UserSchoolRef::getUserId).collect(Collectors.toSet());
            Map<Long, TeacherSubjectRef> teacherSubjectRefMap = teacherLoaderClient.loadTeacherSubjectRefs(teacherIds);
            for (TeacherSubjectRef tsr : teacherSubjectRefMap.values()) {
                teacherServiceClient.setTeacherSubject(tsr.getUserId(), tsr.getSubject(), Ktwelve.INFANT);
            }
        }

        // step 3. update school level to infant
        school.setLevel(SchoolLevel.INFANT.getLevel());
        schoolServiceClient.getSchoolService().updateSchool(school).awaitUninterruptibly();

        // step 4. update school edusystem info
        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(school.getId()).getUninterruptibly();
        if (extInfo != null) {
            extInfo.setEduSystem("I4");
            schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(extInfo);
        }

    }

}

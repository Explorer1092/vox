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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;
import com.voxlearning.utopia.service.mizar.consumer.service.CJLSyncDataServiceClient;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.KlxStudent;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理请求过来的数据
 * Created by Yuechen.Wang on 2017/07/21.
 */
@Named
@ScheduledJobDefinition(
        jobName = "处理陈经纶学校同步数据",
        jobDescription = "处理同步陈经纶学校数据",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 */20 * * * ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class ProcessSyncCJLDataJob extends ScheduledJobWithJournalSupport {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private GroupServiceClient groupServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private ThirdPartyLoaderClient thirdPartyLoaderClient;
    @Inject private CJLSyncDataServiceClient cjlSyncDataServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        List<CJLTeacherCourse> allCourse = cjlSyncDataServiceClient.getSyncDataService().findAllTeacherCourseForJob()
                .stream()
                .filter(CJLTeacherCourse::notSync)
                .collect(Collectors.toList());

        Map<String, Long> schoolIdMapping = getSchoolIdMapping();
        Map<String, CJLTeacher> allTeacher = cjlSyncDataServiceClient.getSyncDataService().findAllTeacher().stream().collect(Collectors.toMap(CJLTeacher::getId, Function.identity()));
        Map<String, CJLClass> allClass = cjlSyncDataServiceClient.getSyncDataService().findAllClass().stream().collect(Collectors.toMap(CJLClass::getId, Function.identity()));
        Map<String, CJLStudent> allStudent = cjlSyncDataServiceClient.getSyncDataService().findAllStudent().stream().collect(Collectors.toMap(CJLStudent::getId, Function.identity()));

        progressMonitor.worked(5);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, allCourse.size());

        logger.info("Total {} courses found.", allCourse.size());

        for (CJLTeacherCourse course : allCourse) {
            try {
                CJLTeacher teacherData = allTeacher.get(course.getTeacherId());
                CJLClass classData = allClass.get(course.getClassId());
                if (teacherData == null || classData == null) {
                    continue;
                }

                List<CJLStudent> students = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(course.getStudentList())) {
                    Set<String> IDSet = CollectionUtils.toLinkedHashSet(course.getStudentList());
                    for (String id : IDSet) {
                        CollectionUtils.addNonNullElement(students, allStudent.get(id));
                    }
                }

                Long schoolId = schoolIdMapping.get(teacherData.getSchoolId());
                if (schoolId == null) {
                    continue;
                }

                MapMessage process = process(course, teacherData, classData, schoolId, students);

                if (process.isSuccess()) {
                    course.setSyncStatus(1);
                } else {
                    course.setSyncStatus(SafeConverter.toInt(process.get("code")));
                    course.setSyncMessage(process.getInfo());
                }
                cjlSyncDataServiceClient.getSyncDataService().modifyTeacherCourse(course);
                logger.info("Process success, course={}, message={}", course.getId(), JsonUtils.toJson(process));
                Thread.sleep(10 * 1000); // 不急这一会
            } catch (Exception ex) {
                logger.error("Course is {}. ", JsonUtils.toJson(course), ex);
            } finally {
                monitor.worked(1);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private MapMessage process(CJLTeacherCourse course,
                               CJLTeacher teacherData,
                               CJLClass classData,
                               Long schoolId,
                               List<CJLStudent> studentData) {
        // 不是数学老师，不管
        if (!teacherData.isMathTeacher()) {
            return MapMessage.errorMessage("老师学科未知").add("code", 0);
        }

        // 找到关联的老师和班级, 如果没有同步该老师的数据，重新创建一个咯
        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(SsoConnections.CjlSchool.getSource(), teacherData.getId());
        if (landingSource == null) {
            cjlSyncDataServiceClient.getSyncDataService().syncSchoolTeacher(teacherData.getSchoolId(), Collections.singletonList(teacherData));
        }
        landingSource = thirdPartyLoaderClient.loadLandingSource(SsoConnections.CjlSchool.getSource(), teacherData.getId());
        if (landingSource == null || landingSource.getUserId() == null) {
            return MapMessage.errorMessage("创建老师失败").add("code", 9);
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(landingSource.getUserId());
        if (teacher == null || teacher.isDisabledTrue()) {
            logger.debug("Can not find teacher of Specific Teacher , CJLTeacherId={}, userId={}", course.getTeacherId(), landingSource.getUserId());
            return MapMessage.errorMessage("无效的老师ID: " + landingSource.getId()).add("code", 9);
        }

        List<Long> clazzIds = teacherLoaderClient.loadTeacherClazzIds(teacher.getId());
        Clazz matchedClazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(clazz -> matchClazz(classData, clazz))
                .findFirst().orElse(null);
        Long clazzId;
        // 看看有没有匹配到的班级
        if (matchedClazz == null) {
            // 没有的话就要新创建了
            MapMessage retMsg = internalSyncClass(classData, schoolId);
            List<NeonatalClazz> neonatals = (List<NeonatalClazz>) retMsg.get("neonatals");
            if (CollectionUtils.isEmpty(neonatals)) {
                logger.error("no clazz found in new level and error happened when creating system clazz!");
                return MapMessage.errorMessage("创建班级失败").add("code", 0);
            }
            clazzId = neonatals.get(0).getClazzId();
        } else {
            clazzId = matchedClazz.getId();
        }

        GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
        Long groupId = null;
        if (group == null) {
            MapMessage message = groupServiceClient.createTeacherGroup(teacher.getId(), clazzId, null);
            if (message.isSuccess() && SafeConverter.toLong(message.get("groupId")) > 0L) {
                groupId = SafeConverter.toLong(message.get("groupId"));
            }
        } else {
            groupId = group.getId();
        }

        if (groupId == null) {
            return MapMessage.errorMessage("创建班组失败，稍后重试").add("code", 0);
        }

        Map<String, CJLStudent> studentNameMap = studentData.stream()
                .collect(Collectors.toMap(CJLStudent::getName, Function.identity(), (u, v) -> u));

        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxGroupStudents(groupId)
                .stream().collect(Collectors.toMap(KlxStudent::getName, Function.identity(), (u, v) -> u));

        List<CJLStudent> students = new LinkedList<>();

        for (Map.Entry<String, CJLStudent> entry : studentNameMap.entrySet()) {
            String studentName = entry.getKey();
            CJLStudent student = entry.getValue();
            KlxStudent klxStudent = klxStudentMap.get(studentName);
            if (klxStudent == null) {
                MapMessage message = MapMessage.errorMessage();
                try {
                    message = newKuailexueServiceClient.createKlxStudent(schoolId, groupId, studentName, student.getStudentNumber());
                    Thread.sleep(500);
                } catch (Exception ex) {
                    logger.error("Failed create Klx Student.", ex);
                }
                klxStudent = message.isSuccess() ? (KlxStudent) message.get("klxStudent") : null;
            }

            if (klxStudent != null) {
                student.setKlxStudentId(klxStudent.getId());
                students.add(student);
            }
        }
        cjlSyncDataServiceClient.getSyncDataService().syncStudents(students);
        return MapMessage.successMessage();
    }

    private Map<String, Long> getSchoolIdMapping() {
        String schoolMapConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CJL_SCHOOL_MAP"
        );

        Map<String, Long> schoolIdMap = new HashMap<>();
        Stream.of(schoolMapConfig.split(",")).forEach(pair -> {
            String[] split = pair.split(":");
            schoolIdMap.put(split[0], SafeConverter.toLong(split[1]));
        });
        return schoolIdMap;
    }

    private MapMessage internalSyncClass(CJLClass sourceClass, Long schoolId) {
        ClassMapper mapper = new ClassMapper();
        mapper.setClassLevel(ConversionUtils.toString(sourceClass.getClazzLevel().getLevel()));
        mapper.setClazzName(sourceClass.getName());
        mapper.setSchoolId(schoolId);
        mapper.setEduSystem(EduSystemType.S3.name());
        mapper.setFreeJoin(true);
        return clazzServiceClient.createSystemClazz(Collections.singletonList(mapper));
    }

    private boolean matchClazz(CJLClass classData, Clazz clazz) {
        if (classData == null || clazz == null) {
            return false;
        }
        String clazzName1 = classData.formalizeClazzName();
        String clazzName2 = clazz.formalizeClazzName();

        return StringUtils.equals(clazzName1, clazzName2);
    }

}

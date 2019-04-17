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
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.entity.seiue.SeiueClass;
import com.voxlearning.utopia.entity.seiue.SeiueTerm;
import com.voxlearning.utopia.entity.seiue.SeiueUser;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.client.TemplateEmailCreator;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.log.UserOperatorType;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.api.service.SeiueSyncDataService;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 同步希悦平台数据
 * Created by Yuechen.Wang on 2017/12/25.
 */
@Named
@ScheduledJobDefinition(
        jobName = "同步希悦平台数据",
        jobDescription = "同步希悦平台数据，每天4点运行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING, /*Mode.PRODUCTION*/},
        cronExpression = "0 0 4 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
@SuppressWarnings("unchecked")
public class SyncSeiueDataJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = SeiueSyncDataService.class)
    private SeiueSyncDataService seiueSyncDataService;

    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private GroupServiceClient groupServiceClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherServiceClient teacherServiceClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private ThirdPartyLoaderClient thirdPartyLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    private static final List<Subject> scienceSubjects = Arrays.asList(Subject.PHYSICS, Subject.CHEMISTRY, Subject.BIOLOGY);

    private StringBuilder errMsg;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String schalter = commonConfigServiceClient.getCommonConfigBuffer()
                .loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.name(), "SEIUE_SWITCH");
        if (!"anmachen".equals(schalter)) {
            logger.info("SyncSeiueDataJob is closed.");
            progressMonitor.done();
            return;
        }

        errMsg = new StringBuilder();
        // 检查配置
        Long schoolId = seiueSyncDataService.loadSchoolId();
        if (schoolId == null || schoolId <= 0L) {
            logger.error("School info is  incorrect, please check data.");
            errMsg.append("学校信息配置异常，请检查数据").append("<br/>");
            return;
        }

        // 先拉取数据
        logger.info("............Pull Seiue data start.");
        pullSeiueData(progressMonitor);
        logger.info("............Pull Seiue data done.");

        // 拿出当前学期
        SeiueTerm term = seiueSyncDataService.loadCurrentTerm();
        if (term == null) {
            logger.error("Seiue current term not found, please check data.");

            return;
        }
        progressMonitor.worked(3);

        // 根据学期找出班级
        List<SeiueClass> classList = seiueSyncDataService.loadTermCompulsoryClass(term.getId());
        logger.info("Total Seiue data found : {}", classList.size());

        ISimpleProgressMonitor syncTask = progressMonitor.subTask(60, classList.size());

        // 学校中所有的教学班备用
        List<Clazz> schoolWalkingClazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .toList()
                .stream()
                .filter(Clazz::isWalkingClazz)
                .collect(Collectors.toList());

        Map<ClazzLevel, List<Clazz>> gradeWalkingClazzList = schoolWalkingClazzList
                .stream()
                .collect(Collectors.groupingBy(Clazz::getClazzLevel));

        // 取出所有的老师备用
        List<Teacher> teacherList = teacherLoaderClient.loadSchoolTeachers(schoolId);

        // 取出所有学生备用
        Map<ClazzLevel, List<KlxStudent>> gradeStudentsMap = new HashMap<>(); // 学生也按照年级分好
        Map<String, KlxStudent> seiueKlxStudentMap = new HashMap<>(); // 已经绑定过的学生列表
        for (Map.Entry<ClazzLevel, List<Clazz>> entry : gradeWalkingClazzList.entrySet()) {
            List<Long> clazzIds = gradeWalkingClazzList.getOrDefault(entry.getKey(), Collections.emptyList())
                    .stream()
                    .map(Clazz::getId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<String, KlxStudent> klxStudents = specialTeacherLoaderClient.loadKlxStudentsByClazzIds(clazzIds)
                    .values().stream().flatMap(Collection::stream)
                    .collect(Collectors.toMap(KlxStudent::getId, Function.identity(), (u, v) -> u));

            gradeStudentsMap.put(entry.getKey(), new LinkedList<>(klxStudents.values()));

            for (KlxStudent klxStudent : klxStudents.values()) {
                String seiueStudentId = klxStudent.getSeiueId();
                if (StringUtils.isBlank(seiueStudentId)) {
                    continue;
                }
                if (!seiueKlxStudentMap.containsKey(seiueStudentId)) {
                    seiueKlxStudentMap.put(seiueStudentId, klxStudent);
                    continue;
                }
                if (!StringUtils.equals(seiueKlxStudentMap.get(seiueStudentId).getId(), klxStudent.getId())) {
                    logger.warn("Duplicate Seiue-KlxStudent found, klxId = ({}, {})", seiueKlxStudentMap.get(seiueStudentId), klxStudent.getId());
                }
            }
        }

        // 准备好科学的特殊配置
        Map<String, Map<Subject, Long>> scienceConfig = seiueSyncDataService.loadScienceTeacherConfig();

        // 对每个班级找出班级的老师和学生
        int index = 0;
        Set<String> classNameSet = new HashSet<>();
        for (SeiueClass seiueClass : classList) {
            index++;

            // 加载出班级所有相关人员
            List<SeiueUser> users = seiueSyncDataService.loadClassUsers(seiueClass.getId());

            // 找出一个可以用的老师之后才开始处理班级信息
            SeiueUser seiueTeacher = users.stream().filter(SeiueUser::teacher).findFirst().orElse(null);
            if (seiueTeacher == null) {
                logger.warn("Failed load seiueClass's teacher, class={}", seiueClass.getId());
                syncTask.worked(1);
                continue;
            }

            // 分检出学生信息
            List<SeiueUser> students = users.stream().filter(SeiueUser::student).collect(Collectors.toList());

            // 加载出年级里面中所有的快乐学学生
            List<KlxStudent> klxStudents = gradeStudentsMap.getOrDefault(seiueClass.clazzLevel(), Collections.emptyList());

            List<Subject> processSubject = Subject.SCIENCE == seiueClass.subject() ? scienceSubjects : Collections.singletonList(seiueClass.subject());

            for (Subject subject : processSubject) {
                String className = seiueClass.formalizeClazzName(subject);
                classNameSet.add(className);

                // 先同步老师的信息
                Long syncTeacherId;
                if (Subject.SCIENCE == seiueClass.subject()) {
                    syncTeacherId = scienceConfig.getOrDefault(seiueTeacher.getId(), Collections.emptyMap()).get(subject);
                } else {
                    syncTeacherId = syncSeiueTeacher(seiueTeacher, teacherList, subject, schoolId);
                }
                if (syncTeacherId == null) {
                    logger.warn("Failed sync seiueTeacher, teacher={}, subject={}", seiueTeacher.getId(), subject);
                    errMsg.append("同步希悦老师数据失败: 未找到该老师").append("<br/>");
                    syncTask.worked(1);
                    continue;
                }

                // 有了老师之后就可以同步班级信息
                List<Clazz> clazzList = gradeWalkingClazzList.getOrDefault(seiueClass.clazzLevel(), Collections.emptyList());
                WalkingClass syncClass = syncSeiueClass(seiueClass, clazzList, subject, syncTeacherId, schoolId);
                if (!syncClass.valid()) {
                    logger.warn("Failed sync seiueClass, class={}, teacher={}, errMsg={}", seiueClass.getId(), syncTeacherId, syncClass.errorMessage);
                    errMsg.append("同步希悦班级数据失败: ").append(syncClass.errorMessage).append("，班级：").append(seiueClass.realClassName()).append("<br/>");
                    syncTask.worked(1);
                    continue;
                }

                Set<String> syncKlxIds = new HashSet<>();
                for (SeiueUser student : students) {
                    KlxStudent klxStudent = syncSeiueStudent(student, klxStudents, seiueKlxStudentMap, syncClass.groupId, schoolId);
                    if (klxStudent == null) {
                        logger.warn("Failed sync seiueStudent, classInfo={}, student={}", syncClass, student.getId());
                        errMsg.append("同步希悦学生数据失败， 学生姓名 ：").append(student.getUserName()).append("<br/>");
                        continue;
                    }
                    syncKlxIds.add(klxStudent.getId());
                }

                // 还要让不在这个班级的学生退出班级
                asyncGroupServiceClient.findKlxGroupStudentRefsWithCache(syncClass.groupId)
                        .stream()
                        .filter(ref -> StringUtils.isNotBlank(ref.getKlxStudentId()))
                        .filter(ref -> !syncKlxIds.contains(ref.getKlxStudentId()))
                        .forEach(ref -> newKuailexueServiceClient.deleteKlxStudent(syncClass.groupId, ref.getKlxStudentId()));

                takeABreak(20);
            }

            syncTask.worked(1);
            logger.info("Sync Seiue data done, {}/{}", index, classList.size());
            takeABreak(30);
        }

        // 之后对非本学期的班级做禁用处理
        schoolWalkingClazzList.stream()
                .filter(wlk -> !classNameSet.contains(wlk.formalizeClazzName()))
                .forEach(clazz -> clazzServiceClient.disableWalkingClazz(Collections.singleton(clazz.getId())));

        if (StringUtils.isNotBlank(errMsg.toString())) {
            TemplateEmailCreator info = emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .content(MapUtils.m("info", errMsg.toString()))
                    .to("yuechen.wang@17zuoye.com");
            if (RuntimeMode.isProduction()) {
                info.cc("yaxiang.zhao@17zuoye.com");
            }
            info.send();
        }

        logger.info("............Sync Seiue data done.");
        progressMonitor.done();
    }

    /**
     * 拉取希悦的数据
     */
    private void pullSeiueData(ISimpleProgressMonitor progressMonitor) {
        ISimpleProgressMonitor pullDataTask = progressMonitor.subTask(30, 10);

        logger.info("............Pulling SeiueTerm data.");
        MapMessage syncMsg = seiueSyncDataService.pullSeiueTerm();
        Long termId = SafeConverter.toLong(syncMsg.get("current"));
        if (!syncMsg.isSuccess() || termId <= 0L) {
            logger.error("Failed pull SeiueTerm data, errMsg = {}, errCode={}.", syncMsg.getInfo(), syncMsg.getErrorCode());
            errMsg.append("拉取希悦学期数据失败: ").append(syncMsg.getInfo()).append("<br/>");
            return;
        }
        pullDataTask.worked(1);

        logger.info("............Pulling SeiueClass data by current term.");
        syncMsg = seiueSyncDataService.pullSeiueClass(termId);
        List<Long> classIds = (List<Long>) syncMsg.get("classes");
        if (!syncMsg.isSuccess() || CollectionUtils.isEmpty(classIds)) {
            logger.error("Pulling SeiueClass data, errMsg = {}, errCode={}, termId={}", syncMsg.getInfo(), syncMsg.getErrorCode(), termId);
            errMsg.append("拉取希悦当前学期班级数据失败: ").append(syncMsg.getInfo()).append("<br/>");
            return;
        }
        pullDataTask.worked(3);

        logger.info("............Pulling SeiueUser data by classes.");
        ISimpleProgressMonitor syncStuTask = pullDataTask.subTask(6, classIds.size());
        int index = 0;
        for (Long classId : classIds) {
            index++;
            syncMsg = seiueSyncDataService.pullSeiueStudent(classId);
            if (!syncMsg.isSuccess()) {
                logger.error("Failed pull SeiueStudent data, errMsg = {}, errCode={}, classId={}", syncMsg.getInfo(), syncMsg.getErrorCode(), classId);
                errMsg.append("拉取希悦班级学生数据失败: ").append(syncMsg.getInfo()).append("<br/>");
            }
            syncStuTask.worked(1);
            if (index % 100 == 1 || index == classIds.size()) {
                logger.info("Pull SeiueUser data is success, {}/{}", index, classIds.size());
            }
        }
    }

    /**
     * 同步老师信息
     */
    private Long syncSeiueTeacher(SeiueUser seiueTeacher, List<Teacher> teacherList, Subject subject, Long schoolId) {
        // 先检查一下这个老师是不是已经做过关联
        LandingSource source = thirdPartyLoaderClient.loadLandingSource(SsoConnections.Seiue.getSource(), seiueTeacher.getId());
        Long boundId = source == null ? null : source.getUserId();
        Teacher boundTeacher = teacherLoaderClient.loadTeacher(boundId);
        if (boundTeacher != null) {
            // 如果已经绑定过了, 更新一下姓名就好了
            if (!StringUtils.equals(boundTeacher.fetchRealname(), seiueTeacher.getUserName())) {
                userServiceClient.changeName(boundTeacher.getId(), seiueTeacher.getUserName());
            }
            if (seiueTeacher.gender() != boundTeacher.fetchGender()) {
                userServiceClient.changeGender(boundTeacher.getId(), seiueTeacher.gender().getCode());
            }
            return boundId;
        }
        // 去学校里找到同名同学科的老师
        User matchTeacher = teacherList.stream()
                .filter(t -> StringUtils.equals(seiueTeacher.getUserName(), t.fetchRealname()))
                .filter(t -> t.getSubjects() != null && t.getSubjects().contains(subject))
                .findFirst().orElse(null);

        // 找不到同名的，那么就直接新建一个老师
        if (matchTeacher == null) {
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
            neonatalUser.setUserType(UserType.TEACHER);
            neonatalUser.setMobile("");
            neonatalUser.setPassword(schoolId + "17zy");
            neonatalUser.setRealname(seiueTeacher.getUserName());
            neonatalUser.setWebSource("bdfwlxx_eschool_sync");

            MapMessage message = userServiceClient.registerUser(neonatalUser);
            if (!message.isSuccess()) {
                logger.warn("Failed create SeiueTeacher, seiue id ={}, errMsg={}.", seiueTeacher.getId(), message.getInfo());
                errMsg.append("同步希悦老师数据失败: ").append(message.getInfo()).append("<br/>");
                return null;
            }
            matchTeacher = (User) message.get("user");
        }

        if (matchTeacher == null) {
            seiueTeacher.setSyncStatus(9);
            seiueTeacher.setSyncMessage("同步老师失败");
            seiueSyncDataService.updateSeiueUser(seiueTeacher);
            return null;
        }

        // 设置学科
        teacherServiceClient.setTeacherSubjectSchool(matchTeacher, subject, Ktwelve.SENIOR_SCHOOL, schoolId);

        // 更新用户ID
        thirdPartyService.persistLandingSource(SsoConnections.Seiue.getSource(), seiueTeacher.getId(), seiueTeacher.getUserName(), matchTeacher.getId());

        seiueTeacher.setSyncStatus(1);
        seiueTeacher.setSyncMessage("同步成功");
        seiueSyncDataService.updateSeiueUser(seiueTeacher);

        return matchTeacher.getId();
    }

    /**
     * 同步班级信息
     */
    private WalkingClass syncSeiueClass(SeiueClass seiueClass, List<Clazz> walkingClazzList, Subject subject, Long teacherId, Long schoolId) {
        ClazzLevel clazzLevel = seiueClass.clazzLevel();
        String className = subject.getValue() + seiueClass.suffix() + seiueClass.getClassName() + "班";

        Clazz clazz = walkingClazzList.stream()
                .filter(t -> clazzLevel == t.getClazzLevel())
                .filter(t -> StringUtils.equals(t.getClassName(), className))
                .findFirst()
                .orElse(null);
        if (clazz == null) {
            return createUnboundClass(seiueClass, className, subject, schoolId, teacherId);
        }

        Long clazzId = clazz.getId();

        // 找到班级Group
        Long groupId = deprecatedGroupLoaderClient.loadClazzGroups(clazzId)
                .stream()
                .map(GroupMapper::getId)
                .findFirst()
                .orElse(null);

        if (groupId == null) {
            return createUnboundGroup(seiueClass, clazzId, teacherId);
        }

        // 如果找到了同名班级，检查这个班的老师是否需要接管
        ClazzTeacher clazzTeacher = teacherLoaderClient.loadClazzTeacher(clazzId, subject);
        if (clazzTeacher == null || clazzTeacher.getTeacher() == null) {
            groupServiceClient.getGroupService().joinTeacherIntoGroups(teacherId, Collections.singleton(groupId));
        } else if (!Objects.equals(clazzTeacher.getTeacher().getId(), teacherId)) {
            groupServiceClient.getGroupService().replaceTeacherGroupForReplace(clazzTeacher.getTeacher().getId(), teacherId, clazzId, "SeiueSyncJob", UserOperatorType.SEIUE);
        }
        return successClass(seiueClass, clazzId, groupId);
    }

    /**
     * 同步学生信息
     */
    private KlxStudent syncSeiueStudent(SeiueUser seiueStudent, List<KlxStudent> klxStudents, Map<String, KlxStudent> seiueKlxMap, Long groupId, Long schoolId) {
        KlxStudent boundKlxStudent = seiueKlxMap.get(seiueStudent.getId());

        if (boundKlxStudent == null) {
            // 优先根据姓名+学号去找 --> 改成直接用姓名匹配吧
            KlxStudent syncKlxStudent = klxStudents.stream()
                    .filter(klx -> StringUtils.equals(seiueStudent.getUserName(), klx.realName()))
                    .filter(klx -> StringUtils.equals(seiueStudent.getUserCode(), klx.getStudentNumber()))
                    .filter(klx -> StringUtils.isBlank(klx.getSeiueId()))
                    .findFirst().orElse(null);

            // 没有找到就新建一个
            if (syncKlxStudent == null) {
                MapMessage message = newKuailexueServiceClient.createKlxStudent(schoolId, groupId, seiueStudent.getUserName(), seiueStudent.getUserCode());
                syncKlxStudent = message.isSuccess() ? (KlxStudent) message.get("klxStudent") : null;
            }

            if (syncKlxStudent != null) {
                syncKlxStudent.setSeiueId(seiueStudent.getId());
                newKuailexueServiceClient.upsertKlxStudent(syncKlxStudent);
                seiueKlxMap.put(seiueStudent.getId(), syncKlxStudent);

                seiueStudent.setSyncStatus(1);
                seiueStudent.setSyncMessage("同步成功");
                seiueSyncDataService.updateSeiueUser(seiueStudent);
            }
            return syncKlxStudent;
        }

        // 更新基础信息
        if (!StringUtils.equals(boundKlxStudent.getName(), seiueStudent.getUserName()) || !StringUtils.equals(boundKlxStudent.getStudentNumber(), seiueStudent.getUserCode())) {
            newKuailexueServiceClient.modifyKlxStudent(schoolId, boundKlxStudent.getId(), seiueStudent.getUserName(), seiueStudent.getUserCode());
        }

        // 加入班组
        GroupKlxStudentRef ref = GroupKlxStudentRef.newInstance(groupId, boundKlxStudent.getId(), boundKlxStudent.getA17id());
        newKuailexueServiceClient.persistGroupKlxStudentRef(ref);

        return boundKlxStudent;
    }

    private WalkingClass createUnboundClass(SeiueClass seiueClass, String className, Subject subject, Long schoolId, Long teacherId) {
        ClazzLevel clazzLevel = seiueClass.clazzLevel();
        // 创建班级 & 老师加入班组
        ClassMapper classMapper = new ClassMapper();
        classMapper.setSchoolId(schoolId);
        classMapper.setClassLevel(SafeConverter.toString(clazzLevel.getLevel()));
        classMapper.setClazzName(className);
        classMapper.setFreeJoin(Boolean.TRUE);
        classMapper.setEduSystem(EduSystemType.S4.name());
        classMapper.setOperatorId("SeiueSyncJob");

        MapMessage message = clazzServiceClient.createWalkingClazzWithoutLimit(teacherId, classMapper);
        if (!message.isSuccess()) {
            return failureClass(seiueClass, "创建教学班失败：" + message.getInfo());
        }

        schoolExtServiceClient.getSchoolExtService()
                .addWalkingClazzName(schoolId, clazzLevel, subject, className)
                .getUninterruptibly();

        NeonatalClazz created = (NeonatalClazz) message.get("neonatalClazz");

        if (created == null) {
            return failureClass(seiueClass, "教学班创建失败");
        }

        Long clazzId = created.getClazzId();
        Long groupId = deprecatedGroupLoaderClient.loadClazzGroups(clazzId)
                .stream()
                .map(GroupMapper::getId)
                .findFirst()
                .orElse(null);

        return successClass(seiueClass, clazzId, groupId);
    }

    private WalkingClass createUnboundGroup(SeiueClass seiueClass, Long clazzId, Long teacherId) {
        MapMessage message = groupServiceClient.createWalkingGroup(teacherId, clazzId);
        if (!message.isSuccess()) {
            return failureClass(seiueClass, "创建班组失败：" + message.getInfo());
        }
        return successClass(seiueClass, clazzId, SafeConverter.toLong(message.get("groupId")));
    }

    private WalkingClass successClass(SeiueClass seiueClass, Long clazzId, Long groupId) {
        seiueClass.setSyncStatus(1);
        seiueClass.setSyncMessage("同步成功");

        seiueSyncDataService.updateSeiueClass(seiueClass);
        // 绑定学科
        seiueSyncDataService.bindSeiueClassGroup(SafeConverter.toLong(seiueClass.getTermId()), seiueClass.getId(), clazzId, groupId);

        return new WalkingClass(clazzId, groupId);
    }

    private WalkingClass failureClass(SeiueClass seiueClass, String failureMessage) {
        seiueClass.setSyncStatus(9);
        seiueClass.setSyncMessage(failureMessage);
        seiueSyncDataService.updateSeiueClass(seiueClass);

        return new WalkingClass(failureMessage);
    }

    private class WalkingClass {
        private Long clazzId;
        private Long groupId;

        private String errorMessage;

        WalkingClass(Long clazzId, Long groupId) {
            this.clazzId = clazzId;
            this.groupId = groupId;
        }

        WalkingClass(String errorMessage) {
            this.clazzId = 0L;
            this.groupId = 0L;
            this.errorMessage = errorMessage;
        }

        boolean valid() {
            return clazzId > 0L && groupId > 0L;
        }

        @Override
        public String toString() {
            return "[clazz=" + clazzId + " , group=" + groupId + "]";
        }
    }

    private void takeABreak(long mills) {
        try {
            Thread.sleep(mills);
        } catch (Exception ignored) {
        }
    }
}

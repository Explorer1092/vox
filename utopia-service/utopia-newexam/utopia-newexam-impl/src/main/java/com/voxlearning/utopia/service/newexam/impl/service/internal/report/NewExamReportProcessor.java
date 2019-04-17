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

package com.voxlearning.utopia.service.newexam.impl.service.internal.report;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableDouble;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableInt;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.helper.DateHelper;
import com.voxlearning.utopia.entity.ExamPageReq;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamConstants;
import com.voxlearning.utopia.service.newexam.api.entity.*;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamBrief;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationParentCacheManager;
import com.voxlearning.utopia.service.newexam.impl.consumer.cache.EvaluationTeacherOpenReportCacheManager;
import com.voxlearning.utopia.service.newexam.impl.service.internal.student.result.ER_ProcessFile;
import com.voxlearning.utopia.service.newexam.impl.service.internal.student.work.DoNewExamProcess;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamPaperHelper;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class NewExamReportProcessor extends NewExamSpringBean {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private DoNewExamProcess doNewExamProcess;

    public final static String INDEPENDENT_EXAM_REPORT_SHARE_PREFIX = "independent_exam_report_share_";

    public MapMessage crmUnifyExamList(Long teacherId, Long clazzId, Subject subject, Long groupId) {
        // iDisplayLength : 显示长度
        // iDisplayStart : 从第几行开始，默认0

        MapMessage mapMessage = this.crmIndependentExamList(groupId);

        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage();
        }
        mapMessage.add("appExamList", this.crmAppExamList(groupId));

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        if (subject == null) {
            return MapMessage.errorMessage("学科错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (exRegion == null) {
            return MapMessage.errorMessage("地区不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_EXREGION_NOT_EXIST);
        }

        if (school.getLevel() == SchoolLevel.MIDDLE.getLevel()) {
            switch (subject) {
                case ENGLISH:
                    subject = Subject.JENGLISH;
                    break;
                case MATH:
                    subject = Subject.JMATH;
                    break;
                case CHINESE:
                    subject = Subject.JCHINESE;
                    break;
                default:
                    break;
            }
        }
        List<NewExam> dataPage = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, DateUtils.nextDay(new Date(), -365), clazz.getSchoolId(), subject, exRegion, clazz.getClazzLevel().getLevel());
        List<Map<String, Object>> results = new ArrayList<>();
        for (NewExam newExam : dataPage) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", newExam.getStatus() != null ? newExam.getStatus().getDesc() : "");
            result.put("newExamId", newExam.getId());
            result.put("newExamName", newExam.getName());
            result.put("startAt", DateUtils.dateToString(newExam.getExamStartAt(), DateUtils.FORMAT_SQL_DATETIME));
            result.put("stopAt", DateUtils.dateToString(newExam.getExamStopAt(), DateUtils.FORMAT_SQL_DATETIME));
            result.put("clazzName", clazz.formalizeClazzName());
            results.add(result);
        }
        mapMessage.add("list", results);
        return mapMessage;

    }

    /**
     * 1.先获取分页的作业
     * 2.newExamBrief
     * 3.获取 NewExamRegistration 信息
     * 4.数据 后处理
     */
    public MapMessage newPageUnifyExamList(Teacher teacher, Long clazzId, Subject subject, Integer iDisplayLength, Integer iDisplayStart) {
        // iDisplayLength : 显示长度
        // iDisplayStart : 从第几行开始，默认0
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        if (subject == null) {
            return MapMessage.errorMessage("学科错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (exRegion == null) {
            return MapMessage.errorMessage("地区不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_EXREGION_NOT_EXIST);
        }
        Map<Long, User> userMap = loadClazzStudents(teacher, subject, clazzId);
        if (iDisplayLength <= 0) {
            iDisplayLength = 10;
        }
        if (school.getLevel() == SchoolLevel.MIDDLE.getLevel()) {
            switch (subject) {
                case ENGLISH:
                    subject = Subject.JENGLISH;
                    break;
                case MATH:
                    subject = Subject.JMATH;
                    break;
                case CHINESE:
                    subject = Subject.JCHINESE;
                    break;
                default:
                    break;
            }
        }
        // 老师报名考试
        List<String> groupRegisterNewExamIds = null;
        if (school.getLevel() == SchoolLevel.JUNIOR.getLevel()) {
            List<Long> groupIds = new ArrayList<>();
            List<GroupMapper> groupMappers = groupLoaderClient.loadClazzGroups(clazzId);
            for (GroupMapper groupMapper : groupMappers) {
                if (groupMapper.getSubject().equals(subject)) {
                    groupIds.add(groupMapper.getId());
                }
            }
            groupRegisterNewExamIds = groupExamRegistrationDao.loadByClazzGroupIds(groupIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(GroupExamRegistration::getNewExamId)
                    .collect(Collectors.toList());
        }

        Pageable pageable = new PageRequest(iDisplayStart / iDisplayLength, iDisplayLength);
        Page<NewExam> dataPage = getNewExamByPage(subject, clazz, exRegion, groupRegisterNewExamIds, pageable);
        Map<String, List<String>> newExamIdToNewExamRegistrationIds = dataPage.getContent()
                .stream()
                .collect(Collectors.toMap(NewExam::getId, o -> {
                    String month = MonthRange.newInstance(o.getCreatedAt().getTime()).toString();
                    List<String> newExamRegistrationIds = new LinkedList<>();
                    for (Long userId : userMap.keySet()) {
                        NewExamRegistration.ID newExamRegistrationId = new NewExamRegistration.ID(month, o.getSubject(), o.getId(), userId.toString());
                        newExamRegistrationIds.add(newExamRegistrationId.toString());
                    }
                    return newExamRegistrationIds;
                }));
        List<String> newExamRegistrationIds = newExamIdToNewExamRegistrationIds.values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationDao.loads(newExamRegistrationIds);
        Map<String, NewExamBrief> newExamBriefMap = new LinkedHashMap<>();
        Date currentDate = new Date();
        for (NewExam newExam : dataPage.getContent()) {
            NewExamBrief newExamBrief = new NewExamBrief();
            newExamBrief.setNewExamId(newExam.getId());
            newExamBrief.setOldExam(StringUtils.isNotBlank(newExam.getPaperId()));
            newExamBrief.setClazzId(clazzId);
            //当前时间＞成绩发布时间允许分享报告
            if (newExam.getResultIssueAt().before(currentDate)) {
                newExamBrief.setShareUrl(UrlUtils.buildUrlQuery("view/newexamv2/share_report_toParent", MapUtils.m("clazzId", clazzId, "examId", newExam.getId())));
            }
            newExamBrief.setNewExamName(newExam.getName());
            newExamBrief.setStartAt(DateUtils.dateToString(newExam.getExamStartAt(), "MM-dd HH:mm"));
            newExamBrief.setStopAt(DateUtils.dateToString(newExam.getExamStopAt(), "MM-dd HH:mm"));
            newExamBrief.setClazzName(clazz.formalizeClazzName());
            ExamBanViewInfo examBanViewInfo = new ExamBanViewInfo(newExam).invoke();
            String banReason = examBanViewInfo.getBanReason();
            if (newExam.isOldNewExam()) {
                banReason = "由于系统升级11月13日之前的考试请在PC端查看报告";
            }
            newExamBrief.setBanView(examBanViewInfo.isBanView() || newExam.isOldNewExam());
            newExamBrief.setBanReason(banReason);
            newExamBriefMap.put(newExam.getId(), newExamBrief);
        }
        for (NewExamRegistration registration : newExamRegistrationMap.values()) {
            if (newExamBriefMap.containsKey(registration.getNewExamId())) {
                NewExamBrief newExamBrief = newExamBriefMap.get(registration.getNewExamId());
                Long userId = registration.getUserId();
                newExamBrief.getJoinUsers().add(userId);
                if (registration.getSubmitAt() != null) {
                    newExamBrief.getSubmitUsers().add(userId);
                }
                if (registration.getFinishAt() != null) {
                    newExamBrief.getFinishedUsers().add(userId);
                }
            }
        }
        List<NewExamBrief> results = new ArrayList<>();
        for (NewExam newExam : dataPage.getContent()) {
            if (newExamBriefMap.containsKey(newExam.getId())) {
                NewExamBrief newExamBrief = newExamBriefMap.get(newExam.getId());
                for (Map.Entry<Long, User> entry : userMap.entrySet()) {
                    if (!newExamBrief.getJoinUsers().contains(entry.getKey())) {
                        newExamBrief.getUnJoinStudents().add(entry.getKey());
                    }
                }
                newExamBrief.setJointNum(newExamBrief.getJoinUsers().size());
                newExamBrief.setUnJointNum(newExamBrief.getUnJoinStudents().size());
                results.add(newExamBrief);
            }
        }
        return MapMessage.successMessage().add("pageable", new PageImpl<>(results, pageable, dataPage.getTotalElements()));
    }

    private Page<NewExam> getNewExamByPage(Subject subject, Clazz clazz, ExRegion exRegion, List<String> groupRegisterNewExamIds, Pageable pageable) {
        ExamPageReq examPageReq = new ExamPageReq();
        examPageReq.setExamType(NewExamType.unify);
        examPageReq.setExamStartAt(DateUtils.nextDay(new Date(), -365));
        examPageReq.setSchoolId(clazz.getSchoolId());
        examPageReq.setSubject(subject);
        examPageReq.setExRegion(exRegion);
        examPageReq.setClazzLevel(clazz.getClazzLevel().getLevel());
        examPageReq.setPageable(pageable);
        examPageReq.setOtherExamIds(groupRegisterNewExamIds);
        return newExamLoaderClient.getExamByPage(examPageReq);
    }


    public MapMessage pageUnifyExamList(Long teacherId, Long clazzId, Subject subject, Integer iDisplayLength, Integer iDisplayStart) {
        // iDisplayLength : 显示长度
        // iDisplayStart : 从第几行开始，默认0
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        if (subject == null) {
            return MapMessage.errorMessage("学科错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (exRegion == null) {
            return MapMessage.errorMessage("地区不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_EXREGION_NOT_EXIST);
        }

        if (iDisplayLength <= 0) {
            iDisplayLength = 10;
        }

        List<Long> teacherGroupIds = groupLoaderClient.loadTeacherGroups(teacherId, false).stream().map(GroupTeacherMapper::getId).collect(Collectors.toList());
        List<Long> teacherStudentIds = studentLoaderClient.loadGroupStudentIds(teacherGroupIds).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        if (school.getLevel() == SchoolLevel.MIDDLE.getLevel()) {
            switch (subject) {
                case ENGLISH:
                    subject = Subject.JENGLISH;
                    break;
                case MATH:
                    subject = Subject.JMATH;
                    break;
                case CHINESE:
                    subject = Subject.JCHINESE;
                    break;
                default:
                    break;
            }
        }

        Pageable pageable = new PageRequest(iDisplayStart / iDisplayLength, iDisplayLength);
        Page<NewExam> dataPage = newExamLoaderClient.getExamByPage(NewExamType.unify, DateUtils.nextDay(new Date(), -365), clazz.getSchoolId(), subject, exRegion, clazz.getClazzLevel().getLevel(), pageable);
        List<Map<String, Object>> results = new ArrayList<>();
        for (NewExam newExam : dataPage.getContent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("newExamId", newExam.getId());
            result.put("newExamName", newExam.getName());
            result.put("startAt", DateUtils.dateToString(newExam.getExamStartAt(), DateUtils.FORMAT_SQL_DATETIME));
            result.put("stopAt", DateUtils.dateToString(newExam.getExamStopAt(), DateUtils.FORMAT_SQL_DATETIME));
            result.put("clazzName", clazz.formalizeClazzName());
            List<Long> joinUsers = new ArrayList<>();
            List<Long> submitUsers = new ArrayList<>();
            List<Long> finishedUsers = new ArrayList<>();
            List<String> registrationIds = newExamRegistrationDao.findByNewExamAndClazzId(newExam, clazzId);
            Map<String, NewExamRegistration> registrations = newExamRegistrationDao.loads(registrationIds);
            if (registrations != null) {
                for (NewExamRegistration registration : registrations.values()) {
                    if (!teacherStudentIds.contains(registration.getUserId())) {
                        continue;
                    }
                    Long userId = registration.getUserId();
                    joinUsers.add(userId);
                    if (registration.getSubmitAt() != null) {
                        submitUsers.add(userId);
                    }
                    if (registration.getFinishAt() != null) {
                        finishedUsers.add(userId);
                    }
                }
            }
            result.put("joinUsers", joinUsers);
            result.put("submitUsers", submitUsers);
            result.put("finishedUsers", finishedUsers);
            results.add(result);
        }

        return MapMessage.successMessage().add("pageable", new PageImpl<>(results, pageable, dataPage.getTotalElements()));

    }


    private MapMessage crmIndependentExamList(Long groupId) {
        // iDisplayLength : 显示长度
        // iDisplayStart : 从第几行开始，默认0


        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());

        Map<Long, List<NewExam>> longListMap = newExamLoaderClient.loadByGroupIds(Collections.singleton(groupId));
        if (!longListMap.containsKey(groupId) || CollectionUtils.isEmpty(longListMap.get(groupId))) {
            return MapMessage.successMessage();
        }
        List<NewExam> newExams = longListMap.get(groupId);


        newExams = newExams.stream()
                .sorted((e1, e2) -> e2.getId().compareTo(e1.getId()))
                .collect(Collectors.toList());


        List<Map<String, Object>> results = new ArrayList<>();

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(groupId)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));

        for (NewExam newExam : newExams) {
            Map<String, Object> result = new HashMap<>();
            result.put("newExamId", newExam.getId());
            result.put("newExamName", newExam.getName());
            result.put("startAt", DateUtils.dateToString(newExam.getExamStartAt(), "MM-dd HH:mm"));
            result.put("stopAt", DateUtils.dateToString(newExam.getExamStopAt(), "MM-dd HH:mm"));
            result.put("clazzName", clazz.formalizeClazzName());
            results.add(result);
        }
        return MapMessage.successMessage().add("results", results);
    }

    private List<Map<String, Object>> crmAppExamList(Long groupId) {

        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return Collections.EMPTY_LIST;
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        List<String> groupRegisterNewExamIds = groupExamRegistrationDao.loadByClazzGroupIds(Collections.singletonList(groupId))
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupExamRegistration::getNewExamId)
                .collect(Collectors.toList());

        Map<String, NewExam> newExamMap = newExamLoaderClient.loads(groupRegisterNewExamIds);
        List<NewExam> newExams = newExamMap.values().stream()
                .sorted((e1, e2) -> e2.getId().compareTo(e1.getId()))
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        for (NewExam newExam : newExams) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", newExam.getStatus() != null ? newExam.getStatus().getDesc() : "");
            result.put("newExamId", newExam.getId());
            result.put("newExamName", newExam.getName());
            result.put("startAt", DateUtils.dateToString(newExam.getExamStartAt(), DateUtils.FORMAT_SQL_DATETIME));
            result.put("stopAt", DateUtils.dateToString(newExam.getExamStopAt(), DateUtils.FORMAT_SQL_DATETIME));
            result.put("clazzName", clazz.formalizeClazzName());
            results.add(result);
        }
        return results;
    }

    public MapMessage pageUnitTestList(Teacher teacher, Subject subject, List<Long> groupIds, Integer iDisplayLength, Integer iDisplayStart) {
        // iDisplayLength : 显示长度
        // iDisplayStart : 从第几行开始，默认0

        if (iDisplayLength <= 0) {
            iDisplayLength = 10;
        }
        // 有传班组
        if (CollectionUtils.isEmpty(groupIds)) {
            // 没有传班组则根据所选的学科查老师的所有班组
            List<Subject> supportedSubjects = Collections.singletonList(subject);
            Set<Long> realTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(realTeacherIds, true);
            Map<Long, List<Long>> clazzIdGroupIdsMap = new LinkedHashMap<>();
            teacherGroups.forEach((tId, groups) -> groups.forEach(group -> {
                if (group.isTeacherGroupRefStatusValid(tId)
                        && CollectionUtils.isNotEmpty(group.getStudents())
                        && supportedSubjects.contains(group.getSubject())) {
                    clazzIdGroupIdsMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
                }
            }));
            List<Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(clazzIdGroupIdsMap.keySet())
                    .stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .collect(Collectors.toList());
            for (Clazz clazz : clazzs) {
                List<Long> clazzGroupIds = clazzIdGroupIdsMap.get(clazz.getId());
                if (CollectionUtils.isNotEmpty(clazzGroupIds)) {
                    groupIds.addAll(clazzGroupIds);
                }
            }
        }

        Map<Long, GroupMapper> groupMappers = groupLoaderClient.loadGroups(groupIds, false);
        if (MapUtils.isEmpty(groupMappers)) {
            return MapMessage.errorMessage("班组为空");
        }
        List<Long> clazzIds = groupMappers.values()
                .stream()
                .map(GroupMapper::getClazzId)
                .collect(Collectors.toList());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        Map<Long, List<NewExam>> newExamMap = newExamLoaderClient.loadByGroupIds(groupIds);
        List<NewExam> newExams = newExamMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(n -> n.getCreatedAt().after(NewExamConstants.UNIT_TEST_DUE_DATE))
                .sorted(Comparator.comparing(NewExam::getCreatedAt).reversed())
                .collect(Collectors.toList());
        Pageable pageable = new PageRequest(iDisplayStart / iDisplayLength, iDisplayLength);
        Page<NewExam> dataPage = PageableUtils.listToPage(newExams, pageable);
        List<Map<String, Object>> results = new ArrayList<>();

        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        EvaluationParentCacheManager evaluationParentCacheManager = newExamCacheClient.getEvaluationParentCacheManager();
        EvaluationTeacherOpenReportCacheManager evaluationTeacherOpenReportCacheManager = newExamCacheClient.getEvaluationTeacherOpenReportCacheManager();
        Date currentDate = new Date();
        Date notShowOpenReportParentsDate = DateUtils.addDays(currentDate, -30);
        for (NewExam newExam : dataPage.getContent()) {
            Long examGroupId = newExam.getGroupId();
            GroupMapper groupMapper = groupMappers.get(examGroupId);
            Clazz clazz = null;
            if (groupMapper != null) {
                clazz = clazzMap.get(groupMapper.getClazzId());
            }

            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            int submitUserCount = 0;
            int allUserCount = 0;
            List<Long> studentIds = groupStudentIds.get(examGroupId);
            if (CollectionUtils.isNotEmpty(studentIds)) {
                allUserCount = studentIds.size();
                List<String> newExamResultIds = studentIds
                        .stream()
                        .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                        .collect(Collectors.toList());
                Map<String, NewExamResult> newExamResults = newExamResultDao.loads(newExamResultIds);
                for (NewExamResult newExamResult : newExamResults.values()) {
                    if (newExamResult.getSubmitAt() != null) {
                        submitUserCount++;
                    }
                }
            }

            String cacheKey = evaluationParentCacheManager.getCacheKey(newExam.getId());
            Set<Long> pids = evaluationParentCacheManager.load(cacheKey);
            int openReportParentsCount = CollectionUtils.isEmpty(pids) ? 0 : pids.size();

            String status = "";
            if (currentDate.after(newExam.getExamStopAt())) {
                String teacherCacheKey = evaluationTeacherOpenReportCacheManager.getCacheKey(newExam.getId());
                Set<Long> tids = evaluationTeacherOpenReportCacheManager.load(teacherCacheKey);
                status = CollectionUtils.isEmpty(tids) ? "UNOPENED" : "OPENED";
            } else if (currentDate.after(newExam.getExamStartAt())) {
                status = "ONGOING";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("newExamId", newExam.getId());
            result.put("newExamName", newExam.getName());
            result.put("startAt", SafeConverter.toLong(newExam.getExamStartAt()));
            result.put("stopAt", SafeConverter.toLong(newExam.getExamStopAt()));
            result.put("clazzName", clazz != null ? clazz.formalizeClazzName() : "");
            result.put("finishUserCount", submitUserCount);
            result.put("allUserCount", allUserCount);
            result.put("openReportParentsCount", openReportParentsCount);
            result.put("showOpenReportParents", newExam.getExamStartAt().after(notShowOpenReportParentsDate));
            result.put("status", status);
            result.put("clazzId", clazz != null ? clazz.getId() : "");
            result.put("showDelete", true);
            result.put("showAdjust", currentDate.before(newExam.getExamStopAt()));
            //当前时间＞成绩发布时间允许分享报告
            if (newExam.getResultIssueAt().before(currentDate)) {
                result.put("shareUrl", UrlUtils.buildUrlQuery("view/newexamv2/share_report_toParent", MapUtils.m("clazzId", clazz != null ? clazz.getId() : "", "examId", newExam.getId())));
            }
            results.add(result);
        }

        return MapMessage.successMessage().add("pageable", new PageImpl<>(results, pageable, dataPage.getTotalElements()));
    }


    /**
     * 处理每道题的做题人明细
     */
    private MapMessage internalProcessExamAnswerDetail(NewExam newExam, Collection<NewExamProcessResult> processResultList) {
//        NewPaper newPaper = paperLoaderClient.loadPaperByDocid(newExam.getPaperId());
        NewPaper newPaper = paperLoaderClient.loadLatestPaperByDocId(newExam.getPaperId());
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
        }
        List<NewPaperQuestion> newPaperQuestions = newPaper.getQuestions();
        List<String> qids = new ArrayList<>();
        Map<String, Double> standardScoreMap = new HashMap<>();

        for (NewPaperQuestion nq : newPaperQuestions) {
            standardScoreMap.put(nq.getId(), nq.getScore());
            qids.add(nq.getId());
        }
        Map<String, NewQuestion> questionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);
        //题对应的模块名称
        Map<String, String> questionPart = new LinkedHashMap<>();
        for (NewPaperParts newPaperPart : newPaper.getParts()) {
            for (NewPaperQuestion newPaperQuestion : newPaperPart.getQuestions()) {
                NewQuestion newQuestion = questionMap.get(newPaperQuestion.getId());
                if (newQuestion != null) {
                    questionPart.put(newQuestion.getDocId(), newPaperPart.getTitle());
                }
            }
        }
        Map<String, MutableInt> examDoCount = new HashMap<>(); //每道题的作答人数
        Map<String, MutableDouble> examTotalScore = new HashMap<>(); //每道题所有人答题总分
        Boolean examNeedCorrect = false;

        // <试题id，map<答案, Set<用户id>>>
        Map<String, LinkedHashMap<String, Set<Long>>> errorExamMap = new LinkedHashMap<>();
        Set<Long> userIds = new HashSet<>();

        //doc初始化
        Map<String, NewQuestion> questionDocMap = questionMap.values().stream().collect(Collectors.toMap(NewQuestion::getDocId, newQuestion -> newQuestion));
        Map<String, Double> questionDocstandardScoreMap = new HashMap<>();
        for (String qid : standardScoreMap.keySet()) {
            NewQuestion newQuestion = questionMap.get(qid);
            if (newQuestion != null) {
                questionDocstandardScoreMap.put(newQuestion.getDocId(), standardScoreMap.get(qid));
            }
        }
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        // key: userId-qid
        Map<String, NewExamProcessResult> uidQidMap = new HashMap<>();
        for (NewExamProcessResult processResult : processResultList) {
            String qDocId = processResult.getQuestionDocId();
            //如果
            NewQuestion question = questionDocMap.get(qDocId);
            if (question == null) {
                continue;
            }
            Long userId = processResult.getUserId();
            userIds.add(userId);


            // 计算每题做题人数
            if (examDoCount.get(qDocId) != null) {
                examDoCount.get(qDocId).add(1);
            } else {
                examDoCount.put(qDocId, new MutableInt(1));
            }

            Double score = processResult.getCorrectScore() != null ? processResult.getCorrectScore() : SafeConverter.toDouble(processResult.getScore());
            //计算每题总分
            if (examTotalScore.get(qDocId) != null) {
                examTotalScore.get(qDocId).add(score);
            } else {
                examTotalScore.put(qDocId, new MutableDouble(score));
            }

            List<NewQuestionsSubContents> nscs = question.getContent().getSubContents();
            String answer = pressNewExamAnswer(nscs, processResult);
            LinkedHashMap<String, Set<Long>> em = errorExamMap.get(qDocId);
            em = (em == null) ? new LinkedHashMap<>() : em;
            Set<Long> uids = (em.containsKey(answer) && em.get(answer) != null) ? em.get(answer) : new HashSet<>();
            uids.add(processResult.getUserId());
            em.put(answer, uids);
            errorExamMap.put(qDocId, em);
            uidQidMap.put(processResult.getUserId() + "-" + qDocId, processResult);
        }

        Map<Long, User> userMap = userLoaderClient.loadUsersIncludeDisabled(userIds);

        Map<String, List<Map<String, Object>>> partErrorExamListMap = new LinkedHashMap<>();
        //计算题的平均分
        // 这里使用试卷里面题的顺序来重新排序学生作答过的题
        for (String qid : questionPart.keySet()) {
            if (!errorExamMap.containsKey(qid)) {
                continue;
            }
            NewQuestion question = questionDocMap.get(qid);
            String partName = questionPart.get(qid);
            if (question == null) {
                continue;
            }
            int showType = 0;
            List<Integer> submitWays = question.getSubmitWays().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(submitWays) && submitWays.contains(1)) {
                showType = 1;
            } else if (CollectionUtils.isNotEmpty(submitWays) && (submitWays.contains(2) || submitWays.contains(3))) {
                showType = 2;
            }
            List<Integer> subContentTypeIds = question.findSubContentTypeIds();
            boolean isSubjective = questionContentTypeLoaderClient.isSubjective(subContentTypeIds);
            boolean isOral = questionContentTypeLoaderClient.isOral(subContentTypeIds);
            boolean isNewOral = questionContentTypeLoaderClient.isNewOral(subContentTypeIds);
            boolean questionNeedCorrect = false;
            if (isSubjective || isOral) {
                examNeedCorrect = true;
                questionNeedCorrect = true;
            }

            double avgScore = new BigDecimal(SafeConverter.toDouble(examTotalScore.get(qid))).divide(new BigDecimal(SafeConverter.toInt(examDoCount.get(qid))), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            List<Map<String, Object>> errorAnswerList = new ArrayList<>();
            LinkedHashMap<String, Set<Long>> answerMaps = errorExamMap.get(qid);
            //这段只为把全对人放到最后(包括纯主观题)
            Set<Long> rights = answerMaps.get("答案正确");
            if (rights != null) {
                answerMaps.remove("答案正确");
                answerMaps.put("答案正确", rights);
            }
            for (String answer : answerMaps.keySet()) {
                List<Map<String, Object>> ums = new ArrayList<>();
                for (Long uid : answerMaps.get(answer)) {
                    User user = userMap.get(uid);
                    if (user != null) {
                        NewExamProcessResult tempResult = uidQidMap.get(uid + "-" + qid);
                        List<String> showFiles = new ArrayList<>();
                        List<String> files = tempResult.findAllFiles().stream()
                                .map(this::getFileUrl)
                                .collect(Collectors.toList());
                        List<String> oralFiles = tempResult.findOralFiles().stream()
                                .map(NewExamProcessResult.OralDetail::getAudio)
                                .collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(files)) showFiles.addAll(files);
                        if (CollectionUtils.isNotEmpty(oralFiles)) showFiles.addAll(oralFiles);

                        Double score = tempResult.getCorrectScore() != null ? tempResult.getCorrectScore() : SafeConverter.toDouble(tempResult.getScore());

                        ums.add(MiscUtils.m("userId", uid, "userName", user.fetchRealnameIfBlankId(), "imgUrl", user.fetchImageUrl(), "showFiles", showFiles, "needCorrect", questionNeedCorrect, "score", score));
                    }
                }
                errorAnswerList.add(MiscUtils.m("answer", answer, "users", ums));
            }

            List<List> newOralAnswerList = new ArrayList<>();
            Double questionStandardScore = questionDocstandardScoreMap.get(qid) != null ? questionDocstandardScoreMap.get(qid) : 0D;
            Double subQuestionStandardScore = questionStandardScore;
            if (isNewOral) {
                int subContentSize = question.getContent().getSubContents().size();
                subQuestionStandardScore = new BigDecimal(questionStandardScore).divide(new BigDecimal(subContentSize), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                Set<Long> userIdList = answerMaps.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
                for (int i = 0; i < subContentSize; i++) {
                    List<Map<String, Object>> subAnswerList = new ArrayList<>();
                    for (Long uid : userIdList) {
                        User user = userMap.get(uid);
                        if (user != null) {
                            NewExamProcessResult tempResult = uidQidMap.get(uid + "-" + qid);
                            List<Double> subScore = tempResult.getCorrectSubScore() != null ? tempResult.getCorrectSubScore() : tempResult.getSubScore();
                            Double score = 0D;
                            if (subScore != null && subScore.size() == subContentSize) {
                                score = subScore.get(i);
                            }
                            List<NewExamProcessResult.OralDetail> oralDetails = tempResult.getOralDetails().get(i);
                            List<String> voiceUrlList = oralDetails.stream().map(NewExamProcessResult.OralDetail::getAudio).collect(Collectors.toList());
                            subAnswerList.add(MiscUtils.m("userId", uid, "userName", user.fetchRealnameIfBlankId(), "imgUrl", user.fetchImageUrl(), "score", score, "showFiles", voiceUrlList));
                        }
                    }
                    newOralAnswerList.add(subAnswerList);
                }
            }

            List<Map<String, Object>> errorExamList = partErrorExamListMap.get(partName);
            if (errorExamList == null) {
                errorExamList = new ArrayList<>();
            }

            errorExamList.add(
                    MiscUtils.m("qid", question.getId(),
                            "standardScore", new BigDecimal(questionStandardScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(),
                            "subStandardScore", new BigDecimal(subQuestionStandardScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(),
                            "contentType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                            "difficulty", question.getDifficultyInt(),
                            "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt()),
                            "avgScore", avgScore,
                            "showType", showType,
                            "isSubjective", isSubjective,
                            "isNewOral", isNewOral,
                            "questionNeedCorrect", questionNeedCorrect,
                            "errorAnswerList", errorAnswerList,
                            "newOralAnswerList", newOralAnswerList));
            partErrorExamListMap.put(partName, errorExamList);
        }


        List<Map<String, Object>> partQuestionInfos = partErrorExamListMap.keySet().stream().map(partName -> MiscUtils.m("partName", partName,
                "questionInfo", partErrorExamListMap.get(partName))).collect(Collectors.toList());

        MapMessage mapMessage = new MapMessage();
        mapMessage.add("partQuestionInfos", partQuestionInfos);
        mapMessage.add("examNeedCorrect", examNeedCorrect);
        mapMessage.add("examName", newExam.getName());
        mapMessage.add("fullScore", SafeConverter.toInt(newPaper.getTotalScore()));
        if (examNeedCorrect) {
            mapMessage.add("correctStopAt", newExam.getCorrectStopAt() != null ? DateUtils.dateToString(newExam.getCorrectStopAt(), DateUtils.FORMAT_SQL_DATETIME) : null);
            Date currentDate = new Date();
            boolean allowCorrect = false;
            //批改时间是在考试结束时间开始到批改截止时间结束
            if (currentDate.after(newExam.getExamStopAt()) && currentDate.before(newExam.getCorrectStopAt())) {
                allowCorrect = true;
            }
            mapMessage.add("allowCorrect", allowCorrect);
        }
        mapMessage.setSuccess(true);
        return mapMessage;
    }

    /**
     * 处理答案，各种题型的答案结果会有不同
     *
     * @return 没做的返回无法查看；做完的，返回“,”分隔的答案字符串
     */
    private String pressNewExamAnswer(List<NewQuestionsSubContents> qscs, NewExamProcessResult processResult) {
        if (processResult.getGrasp()) {
            return "答案正确";
        }
        String answer = "暂时无法查看学生答案";
        List<String> answers = new ArrayList<>();
        int i = 0;
        for (NewQuestionsSubContents qsc : qscs) {
            // 主观题的userAnswer是没有值的
            if (CollectionUtils.isNotEmpty(processResult.getUserAnswers())) {
                List<String> subContentAnswers = processResult.getUserAnswers().get(i);
                i++;
                if (qsc.getSubContentTypeId() != QuestionConstants.LianXianTi && qsc.getSubContentTypeId() != QuestionConstants.GuiLeiTi) {
                    if (qsc.getSubContentTypeId() == QuestionConstants.XuanZe_DanXuan
                            || qsc.getSubContentTypeId() == QuestionConstants.XuanZe_DuoXuan
                            || qsc.getSubContentTypeId() == QuestionConstants.XuanZe_BuDingXiang
                            || qsc.getSubContentTypeId() == QuestionConstants.PanDuanTi) {

                        List<String> ans = new ArrayList<>();
                        for (String an : subContentAnswers) {
                            if (StringUtils.isNotBlank(an)) {
                                for (String a : StringUtils.split(an, ",")) {
                                    int ann = conversionService.convert(a, Integer.class);
                                    ans.add(Character.valueOf((char) (ann + 65)).toString());
                                }
                            } else {
                                ans.add("未作答");
                            }
                        }
                        answer = StringUtils.join(ans, ",");
                    } else if (qsc.getSubContentTypeId() == QuestionConstants.XuanCiTianKong) {
                        // 排序题和一级题型为选词填空，答案的处理
                        List<String> ans = new ArrayList<>();
                        for (String an : subContentAnswers) {
                            List<String> as = new ArrayList<>();
                            for (String a : StringUtils.split(an, ",")) {
                                if (StringUtils.isNumeric(a)) {
                                    int ann = conversionService.convert(a, Integer.class);
                                    as.add(Character.valueOf((char) (ann + 65)).toString());
                                }
                            }
                            ans.add(StringUtils.join(as, ","));
                        }
                        answer = StringUtils.join(ans, ",");
                    } else if (qsc.getSubContentTypeId() == QuestionConstants.PaiXuTi) {

                        List<String> ans = new ArrayList<>();
                        for (String an : subContentAnswers) {
                            List<String> as = new ArrayList<>();

                            Map<Integer, Integer> map = new LinkedHashMap<>();
                            int value = 0;
                            String[] split = StringUtils.isNotBlank(an) ? an.split(",") : new String[0];
                            for (String a : split) {
                                if (StringUtils.isNumeric(a)) {

                                    int ann = SafeConverter.toInt(a);
                                    map.put(ann, value);

                                }
                                value++;
                            }

                            for (int p = 0; p < split.length; p++) {
                                if (map.containsKey(p)) {
                                    as.add(Character.valueOf((char) (map.get(p) + 65)).toString());
                                } else {
                                    as.add("");
                                }

                            }
                            ans.add(StringUtils.join(as, ","));
                        }
                        answer = StringUtils.join(ans, ",");


                    } else {
                        answer = StringUtils.join(subContentAnswers, ",");
                    }
                }
            }
            answers.add(answer);
        }
        if (CollectionUtils.isNotEmpty(answers)) {
            answer = StringUtils.join(answers, ";");
        }
        return answer;
    }

    public MapMessage independentExamGeneralViewDate(MapMessage mapMessage, Map<Long, User> userMap, Map<String, NewExamResult> newExamResults) {
        List<Map<String, Object>> scoreLevels = new ArrayList<>();
        List<String> level1 = new ArrayList<>();
        List<String> level2 = new ArrayList<>();
        List<String> level3 = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        for (NewExamResult newExamResult : newExamResults.values()) {
            if (newExamResult.getFinishAt() != null) {
                User user = userMap.get(newExamResult.getUserId());
                if (user == null) continue;
                double score = newExamResult.getScore();
                scores.add(score);
                if (score >= 25) {
                    level1.add(user.fetchRealname());
                } else if (score >= 18) {
                    level2.add(user.fetchRealname());
                } else {
                    level3.add(user.fetchRealname());
                }
            }
        }
        scoreLevels.add(MapUtils.m("scoreLevel", "25-30分", "students", level1));
        scoreLevels.add(MapUtils.m("scoreLevel", "18-24分", "students", level2));
        scoreLevels.add(MapUtils.m("scoreLevel", "17分以下", "students", level3));
        double maxScore = scores.stream().max(Double::compareTo).orElse(0.0);
        maxScore = new BigDecimal(maxScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        double minScore = scores.stream().max(Comparator.reverseOrder()).orElse(0.0);
        minScore = new BigDecimal(minScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        DoubleSummaryStatistics summaryStatistics = scores.stream().collect(Collectors.summarizingDouble(x -> x));
        double avgScore = new BigDecimal(summaryStatistics.getAverage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        mapMessage.add("scoreLevels", scoreLevels);
        mapMessage.add("avgScore", avgScore);
        mapMessage.add("maxScore", maxScore);
        mapMessage.add("minScore", minScore);
        return mapMessage;
    }

    public MapMessage independentExamDetail(Teacher teacher, String newExamId, String type) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }
        Long groupId = newExam.getGroupId();
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }

        boolean hasPermission = teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), groupId);
        if (!hasPermission) {
            return MapMessage.errorMessage("您无权限查看本次测试。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_PERMISSION);
        }

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(groupId)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResults = newExamResultDao.loads(newExamResultIds);
//        if (MapUtils.isEmpty(newExamResults)) {
//            return MapMessage.errorMessage("没有人参与考试").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
//        }

        List<String> processIds = new ArrayList<>();
        int joinCount = 0; //参与人数
        int submitCount = 0;//交卷人数
        Map<Long, NewExamResult> userResultMap = new HashMap<>();
        for (NewExamResult result : newExamResults.values()) {
            joinCount++;
            if (result.getFinishAt() != null) {
                submitCount++;
            }
            if (result.getAnswers() != null) {
                processIds.addAll(result.getAnswers().values());
            }
            userResultMap.put(result.getUserId(), result);
        }

        Map<String, NewExamProcessResult> examProcessResultMap = newExamProcessResultDao.loads(processIds);

        MapMessage mapMessage;
        if (type.equals("clazz")) {
            mapMessage = internalProcessExamAnswerDetail(newExam, examProcessResultMap.values());
        } else {
            Map<String, NewExamProcessResult> eprMapByUidAndQid = new HashMap<>();
            for (String id : examProcessResultMap.keySet()) {
                NewExamProcessResult epr = examProcessResultMap.get(id);
                eprMapByUidAndQid.put(StringUtils.join(Arrays.asList(epr.getUserId(), epr.getQuestionDocId()), "#"), epr);
            }
            mapMessage = internalProcessExamPartInfo(newExam, userMap, userResultMap, eprMapByUidAndQid);
            mapMessage.add("examName", newExam.getName());
        }
        mapMessage = independentExamGeneralViewDate(mapMessage, userMap, newExamResults);
        if (mapMessage.isSuccess()) {
            mapMessage.add("joinCount", joinCount);
            mapMessage.add("submitCount", submitCount);
            mapMessage.add("clazzName", clazz.formalizeClazzName());
            mapMessage.add("schoolName", school.getCname());
            mapMessage.add("examStartAt", DateUtils.dateToString(newExam.getExamStartAt(), "yyyy-MM-dd HH:mm"));
            mapMessage.add("examStopAt", DateUtils.dateToString(newExam.getExamStopAt(), "yyyy-MM-dd HH:mm"));
        }

        CacheObject<Boolean> cacheObject = newExamCacheClient.cacheSystem.CBS.unflushable.get(INDEPENDENT_EXAM_REPORT_SHARE_PREFIX + newExamId);
        boolean hasShared = cacheObject != null && SafeConverter.toBoolean(cacheObject.getValue());
        mapMessage.add("hasShared", hasShared);
        mapMessage.add("subject", newExam.getSubject());
        return mapMessage;
    }

    public MapMessage examDetailForClazz(Teacher teacher, String newExamId, Long clazzId) {
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

        // 包班制支持，将当前老师转为考试对应学科的老师
        teacher = getRealTeacher(teacher, newExam);

        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (!doNewExamProcess.haveNewExamPermission(exRegion, newExam, school, clazz, Collections.singletonList(teacher.getSubject()))) {
            return MapMessage.errorMessage("您无权限查看本次测试。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_PERMISSION);
        }

//        List<String> newExamResultIds = newExamResultDao.findByNewExamAndClazzId(newExam, clazzId);

//        Map<Long, User> userMap = studentLoaderClient.loadClazzStudents(clazzId)
//                .stream()
//                .filter(Objects::nonNull)
//                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        Map<Long, User> userMap = loadClazzStudents(teacher, newExam.getSubject(), clazzId);


        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newExamResultIds)) {
            return MapMessage.errorMessage("没有人参与考试").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        List<String> processIds = new ArrayList<>();

        int joinCount = 0; //参与人数
        int submitCount = 0;//交卷人数
        Map<String, NewExamResult> newExamResults = newExamResultDao.loads(newExamResultIds);
        for (NewExamResult result : newExamResults.values()) {
            joinCount++;
            if (result.getFinishAt() != null) {
                submitCount++;
            }
            if (result.getAnswers() != null) {
                processIds.addAll(result.getAnswers().values());
            }
        }

        Map<String, NewExamProcessResult> examProcessResultMap = newExamProcessResultDao.loads(processIds);

        MapMessage mapMessage = internalProcessExamAnswerDetail(newExam, examProcessResultMap.values());
        if (mapMessage.isSuccess()) {
            mapMessage.add("joinCount", joinCount);
            mapMessage.add("submitCount", submitCount);
            mapMessage.add("clazzName", clazz.formalizeClazzName());
            mapMessage.add("schoolName", school.getCname());
            mapMessage.add("examStopAt", DateUtils.dateToString(newExam.getExamStopAt()));
        }

        return mapMessage;

    }

    public MapMessage examDetailForStudent(Teacher teacher, String newExamId, Long clazzId) {
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

        // 包班制支持，将当前老师转为考试对应学科的老师
        teacher = getRealTeacher(teacher, newExam);

        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (!doNewExamProcess.haveNewExamPermission(exRegion, newExam, school, clazz, Collections.singletonList(teacher.getSubject()))) {
            return MapMessage.errorMessage("您无权限查看本次测试。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_PERMISSION);
        }

//        List<String> newExamResultIds = newExamResultDao.findByNewExamAndClazzId(newExam, clazzId);
//        Map<Long, User> userMap = studentLoaderClient.loadClazzStudents(clazzId)
//                .stream()
//                .filter(Objects::nonNull)
//                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        Map<Long, User> userMap = loadClazzStudents(teacher, newExam.getSubject(), clazzId);
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(newExamResultIds)) {
            return MapMessage.errorMessage("没有人参与考试").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
        }
        List<String> processIds = new ArrayList<>();

        int joinCount = 0; //参与人数
        int submitCount = 0;//交卷人数

        Map<Long, NewExamResult> userResultMap = new HashMap<>();
        Map<String, NewExamResult> newExamResults = newExamResultDao.loads(newExamResultIds);
        for (NewExamResult result : newExamResults.values()) {
            joinCount++;
            if (result.getFinishAt() != null) {
                submitCount++;
            }
            if (result.getAnswers() != null) {
                processIds.addAll(result.getAnswers().values());
            }
            userResultMap.put(result.getUserId(), result);

        }

        Map<String, NewExamProcessResult> eprMapByUidAndQid = new HashMap<>();
        Map<String, NewExamProcessResult> examProcessResultMap = newExamProcessResultDao.loads(processIds);
        for (String id : examProcessResultMap.keySet()) {
            NewExamProcessResult epr = examProcessResultMap.get(id);
            eprMapByUidAndQid.put(StringUtils.join(Arrays.asList(epr.getUserId(), epr.getQuestionDocId()), "#"), epr);
        }
        MapMessage mapMessage = internalProcessExamPartInfo(newExam, null, userResultMap, eprMapByUidAndQid);
        if (mapMessage.isSuccess()) {
            mapMessage.add("joinCount", joinCount);
            mapMessage.add("submitCount", submitCount);
            mapMessage.add("clazzName", clazz.formalizeClazzName());
            mapMessage.add("schoolName", school.getCname());
            mapMessage.add("examName", newExam.getName());
            mapMessage.add("correctStopAt", DateUtils.dateToString(newExam.getCorrectStopAt(), DateUtils.FORMAT_SQL_DATETIME));
            mapMessage.add("examStopAt", DateUtils.dateToString(newExam.getExamStopAt()));
        }

        return mapMessage;

    }

    /**
     * 处理每道题的做题人明细
     */
    private MapMessage internalProcessExamPartInfo(NewExam newExam,
                                                   Map<Long, User> allUserMap,
                                                   Map<Long, NewExamResult> userResultMap,
                                                   Map<String, NewExamProcessResult> eprMapByUidAndQid) {
//        NewPaper newPaper = paperLoaderClient.loadPaperByDocid(newExam.getPaperId());
        NewPaper newPaper = paperLoaderClient.loadLatestPaperByDocId(newExam.getPaperId());
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
        }
        List<String> parts = new ArrayList<>();
        List<String> questionIds = new ArrayList<>();
        for (NewPaperParts part : newPaper.getParts()) {
            parts.add(part.getTitle());
            questionIds.addAll(part.getQuestions().stream().map(XxBaseQuestion::getId).collect(Collectors.toList()));
        }

        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Map<Long, User> userMap = userLoaderClient.loadUsersIncludeDisabled(userResultMap.keySet());
        List<Map<String, Object>> userInfos = new ArrayList<>();
        for (User user : userMap.values()) {
            Map<String, Object> userInfo = new HashMap<>();
            Long userId = user.getId();
            NewExamResult newExamResult = userResultMap.get(userId);
            userInfo.put("userId", user.getId());
            String userName = user.fetchRealname();
            if (StringUtils.isBlank(userName)) {
                userName = SafeConverter.toString(userId);
            }
            userInfo.put("userName", userName);
            userInfo.put("finish", newExamResult.getFinishAt() != null);
            userInfo.put("submit", newExamResult.getSubmitAt() != null);
            userInfo.put("durationMilliseconds", DateHelper.processDate(SafeConverter.toLong(newExamResult.getDurationMilliseconds())));
            double totalScore = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
            userInfo.put("score", new BigDecimal(totalScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

            userInfo.put("gradeType", SafeConverter.toInt(newExam.getGradeType()));
//            userInfo.put("scoreLevel", embedRank == null ? "无等级" : embedRank.getRankName());
            List<Double> partScores = new ArrayList<>();
            for (NewPaperParts part : newPaper.getParts()) {
                double partScore = 0d;
                for (NewPaperQuestion np : part.getQuestions()) {
                    NewQuestion newQuestion = questionMap.get(np.getId());
                    if (newQuestion != null) {
                        NewExamProcessResult nep = eprMapByUidAndQid.get(StringUtils.join(Arrays.asList(userId, newQuestion.getDocId()), "#"));
                        if (nep != null) {
                            double score = nep.getCorrectScore() != null ? nep.getCorrectScore() : SafeConverter.toDouble(nep.getScore());
                            partScore += score;
                        }
                    }
                }
                partScores.add(new BigDecimal(partScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            userInfo.put("partScores", partScores);
            userInfos.add(userInfo);
        }

        //自主布置模考,显示所有未参加测试的学生
        if (allUserMap != null) {
            List<Map<String, Object>> undoUserInfoList = new ArrayList<>();
            Set<Long> allUserIds = allUserMap.keySet();
            Set<Long> doneUserIds = userMap.keySet();
            List<String> partScoresList = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                partScoresList.add("--");
            }
            for (Long userId : allUserIds) {
                if (!doneUserIds.contains(userId)) {
                    Map<String, Object> undoUserInfo = new HashMap<>();
                    undoUserInfo.put("userId", userId);
                    String userName = allUserMap.get(userId).fetchRealname();
                    if (StringUtils.isBlank(userName)) {
                        userName = SafeConverter.toString(userId);
                    }
                    undoUserInfo.put("userName", userName);
                    undoUserInfo.put("finish", false);
                    undoUserInfo.put("submit", false);
                    undoUserInfo.put("durationMilliseconds", "--");
                    undoUserInfo.put("score", "--");
                    undoUserInfo.put("partScores", partScoresList);
                    undoUserInfoList.add(undoUserInfo);
                }
            }
            userInfos.addAll(undoUserInfoList);
        }

        userInfos = userInfos.stream().sorted((u1, u2) -> Double.compare(SafeConverter.toDouble(u2.get("score")), SafeConverter.toDouble(u1.get("score")))).collect(Collectors.toList());
        MapMessage mapMessage = new MapMessage();
        mapMessage.add("users", userInfos);
        mapMessage.add("parts", parts);
        mapMessage.add("fullScore", SafeConverter.toInt(newPaper.getTotalScore()));
        mapMessage.setSuccess(true);
        return mapMessage;
    }


    /**
     * 考试预览
     */
    public MapMessage viewStudentNewexam(String newExamId, StudentDetail studentDetail, String cdnUrl) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), studentDetail.getId().toString());
        NewExamRegistration newExamRegistration = newExamRegistrationDao.load(id.toString());

        String paperId = NewExamPaperHelper.fetchPaperId(newExamRegistration, newExam, studentDetail.getId());
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
        }

        Date currentDate = new Date();
        if (newExam.getExamStartAt().after(currentDate)) {
            return MapMessage.errorMessage("测试未开始，请耐心等待。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_START);
        }

        List<String> qIds = newPaper.getQuestions().stream().map(NewPaperQuestion::getId).collect(Collectors.toList());
        List<NewQuestion> newQuestions = tikuStrategy.loadQuestionsIncludeDisabledAsList(qIds, newExam.getSchoolLevel());
        List<Integer> contentTypeIds = new ArrayList<>();
        for (NewQuestion newQuestion : newQuestions) {
            contentTypeIds.addAll(newQuestion.getContent().getSubContents().stream().map(NewQuestionsSubContents::getSubContentTypeId).collect(Collectors.toList()));
        }
        boolean isOral = questionContentTypeLoaderClient.isOral(contentTypeIds);

        NewExamResult newExamResult = newExamResultDao.load(id.toString());
        double score = 0;
        if (newExamResult != null) {
            score = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
            score = new BigDecimal(score).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        double rate = 0;
        if (newPaper.getTotalScore() != null && newPaper.getTotalScore() > 0) {
            rate = new BigDecimal(score * 100).divide(new BigDecimal(newPaper.getTotalScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        NewExam.EmbedRank embedRank = newExam.processScoreLevel(rate);
        if (embedRank == null) {
            return MapMessage.errorMessage("模考自定义等级制有问题，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_EMBED_RANK_IS_NULL);
        }

        Integer gradeType = newExam.getGradeType();
        String scoreLevel = embedRank.getRankName();
        DecimalFormat df = new DecimalFormat("###.##");
        Map<String, Object> vars = new HashMap<>();
        vars.put("uid", studentDetail.getId());
        vars.put("userName", studentDetail.fetchRealnameIfBlankId());
        vars.put("cid", studentDetail.getClazzId());
        vars.put("newExamId", newExamId);
        vars.put("gradeType", gradeType);
        vars.put("score", score);
        vars.put("scoreStr", df.format(score));
        vars.put("scoreLevel", scoreLevel);
        vars.put("paperId", paperId);
        vars.put("name", newExam.getName());
        vars.put("subject", newExam.processSubject());
        vars.put("isOral", isOral);
        vars.put("learningType", StudyType.examination);
        vars.put("imgDomain", cdnUrl);
        vars.put("completedUrl", "/flash/loader/newexam/questions/view/answer" + Constants.AntiHijackExt);
        vars.put("paperUrl", "/exam/flash/load/newexam/paper/parts/byid" + Constants.AntiHijackExt);
        vars.put("questionUrl", "/exam/flash/load/newquestion/byids" + Constants.AntiHijackExt);
        vars.put("currentDate", new Date());
        vars.put("fullScore", newPaper.getTotalScore());
        vars.put("testCategory", newExam.getTestCategory());
        vars.put("oralRepeatCount", newExam.getOralRepeatCount());

        return MapMessage.successMessage().add("result", vars);

    }


    public Map<Long, User> loadClazzStudents(Teacher teacher, Subject subject, Long clazzId) {
        Long teacherId;
        switch (subject) {
            case JENGLISH:
                subject = Subject.ENGLISH;
                break;
            case JMATH:
                subject = Subject.MATH;
                break;
            case JCHINESE:
                subject = Subject.CHINESE;
                break;
            default:
                break;
        }
        if (subject == teacher.getSubject()) {
            teacherId = teacher.getId();
        } else {
            teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
        }
        GroupMapper groupMapper = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
        if (groupMapper == null) {
            return Collections.emptyMap();
        }
        return studentLoaderClient.loadGroupStudents(groupMapper.getId())
                .stream()
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
    }

    // 文件地址 http://image.oss.17zuoye.com/2016/01/16/20160116155033512486.jpg
    public String getFileUrl(NewExamQuestionFile inst) {
        if (inst.getFileType() == NewExamQuestionFile.FileType.IMAGE) {
            return ER_ProcessFile.OSS_IMAGE_HOST + inst.getRelativeUrl();
        } else {
            return ER_ProcessFile.OSS_HOST + inst.getRelativeUrl();
        }
    }

    // 包班制支持，获取考试对应学科的老师
    private Teacher getRealTeacher(Teacher teacher, NewExam newExam) {
        if (teacher.isPrimarySchool() && teacher.getSubject() != newExam.getSubject()) {
            Long realTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), newExam.getSubject());
            Teacher realTeacher = teacherLoaderClient.loadTeacher(realTeacherId);
            if (realTeacher != null) {
                return realTeacher;
            }
        }
        return teacher;
    }

    @Getter
    public static class ExamBanViewInfo {
        private NewExam newExam;
        private String banReason;
        private boolean banView;

        public ExamBanViewInfo(NewExam newExam) {
            this.newExam = newExam;
        }

        public ExamBanViewInfo invoke() {
            Date currentDate = new Date();
            boolean isBegin = currentDate.after(newExam.getExamStartAt());
            boolean allowViewScore = newExam.getTeacherVisible() == null || newExam.getTeacherVisible() == 1;
            if (!isBegin) {
                banReason = "当前考试未到考试开始时间，不能查看详情";
            } else if (!allowViewScore) {
                banReason = "当前考试暂不支持查看详情";
            }

            banView = !isBegin || !allowViewScore;
            return this;
        }
    }
}

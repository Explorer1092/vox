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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.athena.api.jzt.entity.GroupReportDetail;
import com.voxlearning.athena.api.jzt.entity.StudentReportDetail;
import com.voxlearning.athena.api.jzt.entity.StudentReportWrapper;
import com.voxlearning.athena.api.jzt.entity.WeeklyReportWrapper;
import com.voxlearning.athena.api.jzt.loader.WeeklyReportLoader;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.WeekReportLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.report.WeekPushTeacher;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.WeekReportClazzInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.WeekReportForClazz;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.WeekReportForStudent;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.WeekReportList;
import com.voxlearning.utopia.service.newhomework.consumer.cache.WeekReportCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.report.WeekPushTeacherPersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = WeekReportLoader.class)
@ExposeService(interfaceClass = WeekReportLoader.class)
public class WeekReportLoaderImpl extends SpringContainerSupport implements WeekReportLoader {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private WeekPushTeacherPersistence weekPushTeacherPersistence;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    @ImportService(interfaceClass = WeeklyReportLoader.class)
    private WeeklyReportLoader weeklyReportLoader;

    public static final int SHOW_CHECK_DAY = -21;//二十一天内周报告显示家长查看的数量


    @Override
    public MapMessage fetchWeekReportBrief(Long studentId) {
        List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
        WeekReportList weekReportList = new WeekReportList();
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, SHOW_CHECK_DAY);
            Date m = c.getTime();
            for (Group groupMapper : groupMappers) {
                List<WeeklyReportWrapper> groupReports = weeklyReportLoader.getGroupReports(groupMapper.getSubject().getId(), Collections.singleton(groupMapper.getId()));
                if (groupReports != null) {
                    List<WeekReportList.WeekReportBrief> briefs = groupReports.stream()
                            .filter(Objects::nonNull)
                            .map(
                                    o -> {
                                        WeekReportList.WeekReportBrief weekReportBrief = new WeekReportList.WeekReportBrief();
                                        Date sDate = DateUtils.stringToDate(o.getStartTime(), "yyyyMMdd");
                                        if (sDate != null) {
                                            weekReportBrief.setStartTime(DateUtils.dateToString(sDate, "yyyy.MM.dd"));
                                            if (sDate.after(m)) {
                                                weekReportBrief.setShowCheckedNum(true);
                                            }
                                        } else {
                                            return null;
                                        }
                                        Date eDate = DateUtils.stringToDate(o.getEndTime(), "yyyyMMdd");
                                        if (eDate != null) {
                                            weekReportBrief.setEndTime(DateUtils.dateToString(eDate, "yyyy.MM.dd"));
                                        } else {
                                            return null;
                                        }
                                        if (MapUtils.isNotEmpty(o.getReports())) {
                                            o.getReports().forEach((k, v) -> weekReportBrief.getGroupIdAndReportId().add(k + "|" + v));
                                        }
                                        if (CollectionUtils.isNotEmpty(weekReportBrief.getGroupIdAndReportId())) {
                                            WeekReportCacheManager weekReportCacheManager = newHomeworkCacheService.getWeekReportCacheManager();
                                            int pNum = 0;
                                            for (String gid : weekReportBrief.getGroupIdAndReportId()) {
                                                String key = weekReportCacheManager.cacheKey(gid);
                                                Set<Long> pids = weekReportCacheManager.load(key);
                                                if (pids != null) {
                                                    pNum += pids.size();
                                                }
                                            }
                                            weekReportBrief.setCheckedNum(pNum);
                                        }
                                        return weekReportBrief;
                                    })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    weekReportList.getWeekReportBriefMap().put(groupMapper.getSubject(), briefs);
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            Subject[] subjects1 = new Subject[]{Subject.ENGLISH, Subject.MATH, Subject.CHINESE};
            Map<Subject, List<WeekReportList.WeekReportBrief>> weekReportBriefMap = new LinkedHashMap<>();
            for (Subject s : subjects1) {
                if (weekReportList.getWeekReportBriefMap().containsKey(s)) {
                    weekReportBriefMap.put(s, weekReportList.getWeekReportBriefMap().get(s));
                }
            }
            weekReportList.setWeekReportBriefMap(weekReportBriefMap);
            List<Map<String, String>> subjects = new LinkedList<>();
            for (Subject subject : weekReportList.getWeekReportBriefMap().keySet()) {
                Map<String, String> map = new HashMap<>();
                map.put("key", subject.name());
                map.put("value", subject.getValue());
                subjects.add(map);
            }
            mapMessage.put("subjects", subjects);
            //学科顺序显示
            mapMessage.put("weekReportBriefMap", weekReportList.getWeekReportBriefMap());
            return mapMessage;
        } catch (Exception e) {
            logger.error("get sid {} week report failed", studentId, e);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage fetchWeekReportBriefV2(Teacher teacher) {
        //1 teacher to list subject
        //2 teacher subject to list groupIds
        //3 subject to list WeekReportBrief
        try {
            Set<Long> tids = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            if (CollectionUtils.isEmpty(tids)) {
                return MapMessage.errorMessage("tids is empty");
            }
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(tids);
            if (MapUtils.isEmpty(teacherMap)) {
                return MapMessage.errorMessage("Teacher is empty");
            }
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, SHOW_CHECK_DAY);
            Date m = c.getTime();
            //tid To GroupMapper
            Map<Long, List<GroupMapper>> tidToGroup = groupLoaderClient.loadTeacherGroupsByTeacherId(tids, false);
            WeekReportList weekReportList = new WeekReportList();

            for (Long tid : tidToGroup.keySet())
                if (teacherMap.containsKey(tid)) {
                    Teacher t = teacherMap.get(tid);
                    if (t.getSubject() != null) {
                        weekReportList.getWeekReportBriefMap().put(t.getSubject(), new LinkedList<>());
                        if (tidToGroup.containsKey(tid) && CollectionUtils.isNotEmpty(tidToGroup.get(tid))) {
                            List<Long> gids = tidToGroup.get(tid)
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(GroupMapper::getId)
                                    .collect(Collectors.toList());
                            List<WeeklyReportWrapper> groupReports = weeklyReportLoader.getGroupReports(t.getSubject().getId(), gids);
                            if (groupReports != null) {
                                List<WeekReportList.WeekReportBrief> briefs = groupReports.stream()
                                        .filter(Objects::nonNull)
                                        .map(
                                                o -> {
                                                    WeekReportList.WeekReportBrief weekReportBrief = new WeekReportList.WeekReportBrief();
                                                    Date sDate = DateUtils.stringToDate(o.getStartTime(), "yyyyMMdd");
                                                    if (sDate != null) {
                                                        if (sDate.after(m)) {
                                                            weekReportBrief.setShowCheckedNum(true);
                                                        }
                                                        weekReportBrief.setStartTime(DateUtils.dateToString(sDate, "yyyy.MM.dd"));
                                                    } else {
                                                        return null;
                                                    }
                                                    Date eDate = DateUtils.stringToDate(o.getEndTime(), "yyyyMMdd");
                                                    if (eDate != null) {
                                                        weekReportBrief.setTeacherIdReportEndTime(tid + "|" + o.getEndTime());
                                                        weekReportBrief.setEndTime(DateUtils.dateToString(eDate, "yyyy.MM.dd"));
                                                    } else {
                                                        return null;
                                                    }
                                                    if (MapUtils.isNotEmpty(o.getReports())) {
                                                        o.getReports().forEach((k, v) -> weekReportBrief.getGroupIdAndReportId().add(k + "|" + v));
                                                    }
                                                    if (CollectionUtils.isNotEmpty(weekReportBrief.getGroupIdAndReportId())) {
                                                        WeekReportCacheManager weekReportCacheManager = newHomeworkCacheService.getWeekReportCacheManager();
                                                        int pNum = 0;
                                                        for (String gid : weekReportBrief.getGroupIdAndReportId()) {
                                                            String key = weekReportCacheManager.cacheKey(gid);
                                                            Set<Long> pids = weekReportCacheManager.load(key);
                                                            if (pids != null) {
                                                                pNum += pids.size();
                                                            }
                                                        }
                                                        weekReportBrief.setCheckedNum(pNum);
                                                    }
                                                    String path = "/view/mobile/common/weekreport/clazzreport?f=reportlist&garId=" + StringUtils.join(weekReportBrief.getGroupIdAndReportId(), ",") + "&&tret=" + weekReportBrief.getTeacherIdReportEndTime();
                                                    weekReportBrief.setPath(path);
                                                    weekReportBrief.setGroupIdAndReportId(null);
                                                    return weekReportBrief;
                                                })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                weekReportList.getWeekReportBriefMap().put(t.getSubject(), briefs);
                            }
                        }
                    }
                }
            MapMessage mapMessage = MapMessage.successMessage();
            Subject[] subjects1 = new Subject[]{Subject.ENGLISH, Subject.MATH, Subject.CHINESE};
            List<Map<String, Object>> result = new LinkedList<>();
            for (Subject s : subjects1) {
                if (weekReportList.getWeekReportBriefMap().containsKey(s)) {
                    result.add(
                            MapUtils.m("subject", s,
                                    "subjectName", s.getValue(),
                                    "integral", 5,
                                    "weekReportBriefList", weekReportList.getWeekReportBriefMap().get(s)
                            ));
                }
            }
            mapMessage.put("weekReportList", result);
            return mapMessage;
        } catch (Exception e) {
            logger.info("fetch Week ReportBrief  tid of {} failed ", teacher.getId(), e);
            return MapMessage.errorMessage("fetch Week ReportBrief  tid of {} failed ", teacher.getId(), e);
        }

    }


    @Override
    public MapMessage fetchWeekReportBrief(Teacher teacher) {
        //1 teacher to list subject
        //2 teacher subject to list groupIds
        //3 subject to list WeekReportBrief
        try {
            Set<Long> tids = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            if (CollectionUtils.isEmpty(tids)) {
                return MapMessage.errorMessage("tids is empty");
            }
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(tids);
            if (MapUtils.isEmpty(teacherMap)) {
                return MapMessage.errorMessage("Teacher is empty");
            }
            //tid To GroupMapper
            Map<Long, List<GroupMapper>> tidToGroup = groupLoaderClient.loadTeacherGroupsByTeacherId(tids, false);
            WeekReportList weekReportList = new WeekReportList();
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, SHOW_CHECK_DAY);
            Date m = c.getTime();
            for (Long tid : tidToGroup.keySet())
                if (teacherMap.containsKey(tid)) {
                    Teacher t = teacherMap.get(tid);
                    if (t.getSubject() != null) {
                        weekReportList.getWeekReportBriefMap().put(t.getSubject(), new LinkedList<>());
                        if (tidToGroup.containsKey(tid) && CollectionUtils.isNotEmpty(tidToGroup.get(tid))) {
                            List<Long> gids = tidToGroup.get(tid)
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(GroupMapper::getId)
                                    .collect(Collectors.toList());
                            List<WeeklyReportWrapper> groupReports = weeklyReportLoader.getGroupReports(t.getSubject().getId(), gids);
                            if (groupReports != null) {
                                List<WeekReportList.WeekReportBrief> briefs = groupReports.stream()
                                        .filter(Objects::nonNull)
                                        .map(
                                                o -> {
                                                    WeekReportList.WeekReportBrief weekReportBrief = new WeekReportList.WeekReportBrief();
                                                    Date sDate = DateUtils.stringToDate(o.getStartTime(), "yyyyMMdd");
                                                    if (sDate != null) {
                                                        if (sDate.after(m)) {
                                                            weekReportBrief.setShowCheckedNum(true);
                                                        }
                                                        weekReportBrief.setStartTime(DateUtils.dateToString(sDate, "yyyy.MM.dd"));
                                                    } else {
                                                        return null;
                                                    }
                                                    Date eDate = DateUtils.stringToDate(o.getEndTime(), "yyyyMMdd");
                                                    if (eDate != null) {
                                                        weekReportBrief.setTeacherIdReportEndTime(tid + "|" + o.getEndTime());
                                                        weekReportBrief.setEndTime(DateUtils.dateToString(eDate, "yyyy.MM.dd"));
                                                    } else {
                                                        return null;
                                                    }
                                                    if (MapUtils.isNotEmpty(o.getReports())) {
                                                        o.getReports().forEach((k, v) -> weekReportBrief.getGroupIdAndReportId().add(k + "|" + v));
                                                    }
                                                    if (CollectionUtils.isNotEmpty(weekReportBrief.getGroupIdAndReportId())) {
                                                        WeekReportCacheManager weekReportCacheManager = newHomeworkCacheService.getWeekReportCacheManager();
                                                        int pNum = 0;
                                                        for (String gid : weekReportBrief.getGroupIdAndReportId()) {
                                                            String key = weekReportCacheManager.cacheKey(gid);
                                                            Set<Long> pids = weekReportCacheManager.load(key);
                                                            if (pids != null) {
                                                                pNum += pids.size();
                                                            }
                                                        }
                                                        weekReportBrief.setCheckedNum(pNum);
                                                    }
                                                    return weekReportBrief;
                                                })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                weekReportList.getWeekReportBriefMap().put(t.getSubject(), briefs);
                            }
                        }
                    }
                }
            MapMessage mapMessage = MapMessage.successMessage();
            Subject[] subjects1 = new Subject[]{Subject.ENGLISH, Subject.MATH, Subject.CHINESE};
            Map<Subject, List<WeekReportList.WeekReportBrief>> weekReportBriefMap = new LinkedHashMap<>();
            for (Subject s : subjects1) {
                if (weekReportList.getWeekReportBriefMap().containsKey(s)) {
                    weekReportBriefMap.put(s, weekReportList.getWeekReportBriefMap().get(s));
                }
            }
            weekReportList.setWeekReportBriefMap(weekReportBriefMap);
            List<Map<String, String>> subjects = new LinkedList<>();
            for (Subject subject : weekReportList.getWeekReportBriefMap().keySet()) {
                Map<String, String> map = new HashMap<>();
                map.put("key", subject.name());
                map.put("value", subject.getValue());
                subjects.add(map);
            }
            mapMessage.put("subjects", subjects);
            //学科顺序显示
            mapMessage.put("weekReportBriefMap", weekReportList.getWeekReportBriefMap());
            return mapMessage;
        } catch (Exception e) {
            logger.info("fetch Week ReportBrief  tid of {} failed ", teacher.getId(), e);
            return MapMessage.errorMessage("fetch Week ReportBrief  tid of {} failed ", teacher.getId(), e);
        }

    }

    @Override
    public MapMessage fetchWeekReportForClazz(String groupIdAndReportId, User user) {
        //1 has sid
        //2 list groupIdAndReportIds to class name
        //3 list groupIdAndReportIds to report
        //4 WeekReport
        try {
            String[] st = StringUtils.split(groupIdAndReportId, "|");
            if (st == null || st.length != 2) {
                return MapMessage.errorMessage("groupIdAndReportId:{} is error", groupIdAndReportId).setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
            }
            Long gid = SafeConverter.toLong(st[0]);
            if (gid == 0) {
                return MapMessage.errorMessage("组id参数错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
            }
            Group group = raikouSDK.getClazzClient().getGroupLoaderClient()
                    ._loadGroup(gid).firstOrNull();
            if (group == null || group.getSubject() == null) {
                return MapMessage.errorMessage("班组不存在或者班组学科不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
            }
            List<Long> userIds = studentLoaderClient.loadGroupStudentIds(gid);
            if (userIds.isEmpty()) {
                return MapMessage.errorMessage("班级学生为空").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_STUDENT_NOT_EXIST);
            }
            //命中等级灰度显示等级，默认显示分数
            boolean needScoreLevel = false;
            StudentDetail student = studentLoaderClient.loadStudentDetail(userIds.get(0));
            if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "ShowScoreLevel", "WhiteList")) {
                needScoreLevel = Boolean.TRUE;
            }

            //注意：取学生姓名用，老师端打开需要全部显示学生姓名；qq、微信打开只显示学号；家长打开需要显示孩子的姓名其他同学都显示学号
            Map<Long, User> userMap = new HashMap<>();
            if (user != null) {
                if (user.isTeacher()) {
                    userMap = userLoaderClient.loadUsers(userIds);
                } else if (user.isParent()) {
                    userMap = studentLoaderClient.loadParentStudents(user.getId()).stream().collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
                }
            }

            Subject subject = group.getSubject();
            GroupReportDetail groupReportDetail = weeklyReportLoader.getGroupReportDetail(subject.getId(), st[1], user != null && user.isParent());
            if (groupReportDetail == null) {
                LogCollector.info("backend-general",
                        MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "op", "WeekReportForClazz",
                                "mod1", st[1],
                                "mod3", subject.getId()));
                return MapMessage.errorMessage();
            }
            WeekReportForClazz weekReportForClazz = new WeekReportForClazz();
            //添加家长查看
            WeekReportCacheManager weekReportCacheManager = newHomeworkCacheService.getWeekReportCacheManager();
            String key = weekReportCacheManager.cacheKey(groupIdAndReportId);
            Set<Long> pids = weekReportCacheManager.load(key);
            if (pids != null) {
                weekReportForClazz.setCheckedNum(pids.size());
            }
            if (user != null && user.isParent()) {
                if (pids == null) {
                    Set<Long> p = new HashSet<>();
                    p.add(user.getId());
                    if (weekReportCacheManager.set(key, p)) {
                        weekReportForClazz.setCheckedNum(1);
                    } else {
                        weekReportCacheManager.evict(key);
                    }
                } else if (!pids.contains(user.getId())) {
                    pids.add(user.getId());
                    weekReportCacheManager.replace(key, pids);
                    weekReportForClazz.setCheckedNum(pids.size());
                }
            }
            Date sDate = DateUtils.stringToDate(groupReportDetail.getStartTime(), "yyyyMMdd");
            if (sDate == null) {
                return MapMessage.errorMessage("startTime is error");
            }
            weekReportForClazz.setStartTime(DateUtils.dateToString(sDate, "yyyy.MM.dd"));

            Date eDate = DateUtils.stringToDate(groupReportDetail.getEndTime(), "yyyyMMdd");
            if (eDate == null) {
                return MapMessage.errorMessage("endTime is error");
            }
            weekReportForClazz.setEndTime(DateUtils.dateToString(eDate, "yyyy.MM.dd"));
            weekReportForClazz.setHomeworkNum(SafeConverter.toInt(groupReportDetail.getHomeworkCnt()));
            weekReportForClazz.setGroupIdToReportId(groupIdAndReportId);
            weekReportForClazz.setSubject(subject);
            weekReportForClazz.setSubjectName(subject.getValue());
            Map<Long, StudentReportDetail> srdMap = CollectionUtils.isNotEmpty(groupReportDetail.getStudentReport()) ?
                    groupReportDetail.getStudentReport().stream().collect(Collectors.toMap(StudentReportDetail::getStudentId, Function.identity())) : Collections.emptyMap();
            for (Long uid : userIds) {
                WeekReportForClazz.StudentWeekReportBrief ws = new WeekReportForClazz.StudentWeekReportBrief();
                ws.setSid(uid);
                if (userMap.get(uid) != null) {
                    ws.setSname(userMap.get(uid).fetchRealnameIfBlankId());
                } else {
                    ws.setSname("" + uid);
                }
                //默认学生上周没有完成过作业的时候平均成绩显示--
                ws.setAvgScoreStr("--");
                StudentReportDetail srd = srdMap.get(uid);
                if (srd != null && srd.getId() != null && srd.getStudentId() != null) {
                    ws.setStudentReportId(srd.getId());
                    ws.setFinishedNum(SafeConverter.toInt(srd.getFinishCnt()));

                    if (srd.getAvgScore() != null) {
                        int avgScore = new BigDecimal(srd.getAvgScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                        ws.setAvgScore(avgScore);
                        String scoreStr = needScoreLevel ? ScoreLevel.processLevel(avgScore).getLevel() : SafeConverter.toString(avgScore);
                        ws.setAvgScoreStr(scoreStr);
                    }
                    if (ws.getFinishedNum() != 0 && weekReportForClazz.getHomeworkNum() != 0) {
                        int rate = new BigDecimal(ws.getFinishedNum() * 100).divide(new BigDecimal(weekReportForClazz.getHomeworkNum()), BigDecimal.ROUND_HALF_UP).intValue();
                        ws.setFinishedRate(rate);
                    }
                }
                //家长端访问需要特殊处理自己的孩子前置显示
                if (user != null && user.isParent() && userMap.get(uid) != null) {
                    weekReportForClazz.getChildInfos().add(ws);
                }
                weekReportForClazz.getStudentWeekReportBriefs().add(ws);
            }

            weekReportForClazz.getChildInfos().sort((o1, o2) -> {
                int compare = Integer.compare(o2.getAvgScore(), o1.getAvgScore());
                if (compare == 0) {
                    compare = Integer.compare(o2.getFinishedRate(), o1.getFinishedRate());
                }
                return compare;
            });
            weekReportForClazz.getStudentWeekReportBriefs().sort((o1, o2) -> {
                int compare = Integer.compare(o2.getAvgScore(), o1.getAvgScore());
                if (compare == 0) {
                    compare = Integer.compare(o2.getFinishedRate(), o1.getFinishedRate());
                }
                return compare;
            });
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("weekReportForClazz", weekReportForClazz);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Week Report For Clazz groupIdAndReportId of {}", groupIdAndReportId, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchWeekClazzInfo(List<String> groupIdAndReportIds, String teacherIdReportEndTime) {

        try {
            Map<Long, String> gidMap = new HashMap<>();
            for (String s : groupIdAndReportIds) {
                String[] strings = StringUtils.split(s, "|");
                if (strings != null && strings.length == 2) {
                    Long l = SafeConverter.toLong(strings[0]);
                    gidMap.put(l, s);
                }
            }
            Map<Long, GroupMapper> longGroupMap = groupLoaderClient.loadGroups(gidMap.keySet(), false);
            Map<Long, Set<Long>> clazzToG = new HashMap<>();
            Subject subject = null;
            for (Long l : longGroupMap.keySet()) {
                GroupMapper group = longGroupMap.get(l);
                if (subject == null) {
                    subject = group.getSubject();
                }
                clazzToG.computeIfAbsent(group.getClazzId(), k -> new HashSet<>()).add(l);
            }
            if (subject == null) {
                return MapMessage.errorMessage();
            }
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(clazzToG.keySet())
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            WeekReportClazzInfo weekReportClazzInfo = new WeekReportClazzInfo();
            weekReportClazzInfo.setSubject(subject);
            weekReportClazzInfo.setSubjectName(subject.getValue());
            List<WeekReportClazzInfo.GroupToReport> l = new LinkedList<>();
            weekReportClazzInfo.setGroupToReports(l);
            List<Clazz> collect = clazzMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .collect(Collectors.toList());
            for (Clazz clazz : collect) {
                Long c = clazz.getId();
                Set<Long> groupIds = clazzToG.get(c);
                for (Long gid : groupIds) {
                    WeekReportClazzInfo.GroupToReport g = new WeekReportClazzInfo.GroupToReport();
                    g.setClazzName(clazz.formalizeClazzName());
                    ClazzLevel clazzLevel = clazz.getClazzLevel();
                    if (clazzLevel != null) {
                        g.setClazzLevel(clazzLevel.getLevel());
                    }
                    if (clazz.getLevel() != null) {
                        g.setLevel(clazz.getLevel());
                    }
                    g.setGroupIdToReportId(gidMap.get(gid));
                    l.add(g);
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("clazzInfo", weekReportClazzInfo);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Week Clazz Info failed : groupIdAndReportIds of {}, teacherIdReportEndTime of  {}", groupIdAndReportIds, teacherIdReportEndTime, e);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public Page<WeekPushTeacher> loadWeekPushTeacherByPage(Pageable pageable) {
        return weekPushTeacherPersistence.loadWeekPushTeacherByPage(pageable);
    }

    @Override
    public MapMessage fetchWeekReportForStudent(Subject subject, String studentReportId, User user) {
        //1 fetch reportId
        //2 get report for personal
        //3 complete the  WeekReportForStudent
        try {
            StudentReportWrapper studentReportWrapper = weeklyReportLoader.getStudentReportDetail(subject.getId(), studentReportId, user.isParent());
            if (studentReportWrapper == null || studentReportWrapper.getCurrentWeek() == null) {
                LogCollector.info("backend-general",
                        MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "op", "WeekReportForStudent",
                                "mod1", studentReportId,
                                "mod2", user.isParent(),
                                "mod3", subject.getId(),
                                "usertoken", user.getId()));

                return MapMessage.errorMessage();
            }
            WeekReportForStudent weekReportForStudent = new WeekReportForStudent();
            StudentReportDetail currentWeek = studentReportWrapper.getCurrentWeek();
            StudentReportDetail lastWeek = studentReportWrapper.getLastWeek();
            weekReportForStudent.setSid(currentWeek.getStudentId());
            Student student = studentLoaderClient.loadStudent(weekReportForStudent.getSid());
            if (student != null) {
                weekReportForStudent.setSname(student.fetchRealname());
            }
            weekReportForStudent.setSubjectName(subject.getValue());
            Map<Long, List<GroupMapper>> longListMap = groupLoaderClient.loadStudentGroups(Collections.singleton(weekReportForStudent.getSid()), false);
            GroupMapper target = null;
            if (longListMap.containsKey(weekReportForStudent.getSid())) {
                List<GroupMapper> groupMappers = longListMap.get(weekReportForStudent.getSid());
                for (GroupMapper g : groupMappers) {
                    if (g != null && g.getSubject() == subject) {
                        target = g;
                        break;
                    }
                }
            }
            if (target == null) {
                return MapMessage.errorMessage();
            }
            Map<Long, List<Teacher>> longListMap1 = teacherLoaderClient.loadGroupTeacher(Collections.singleton(target.getId()), RefStatus.VALID);
            if (longListMap1.containsKey(target.getId()) && CollectionUtils.isNotEmpty(longListMap1.get(target.getId()))) {
                Teacher t = longListMap1.get(target.getId()).get(0);
                weekReportForStudent.setTeacherName(t.fetchRealname());
                weekReportForStudent.setTeacherUrl(t.fetchImageUrl());
            } else {
                return MapMessage.errorMessage();
            }
            weekReportForStudent.setHomeworkNum(SafeConverter.toInt(currentWeek.getHomeworkCnt()));
            weekReportForStudent.setFinishedHomeworkNum(SafeConverter.toInt(currentWeek.getFinishCnt()));
            if (weekReportForStudent.getFinishedHomeworkNum() != 0 && weekReportForStudent.getHomeworkNum() != 0) {
                weekReportForStudent.setFinishedRate(new BigDecimal(weekReportForStudent.getFinishedHomeworkNum() * 100).divide(new BigDecimal(weekReportForStudent.getHomeworkNum()), BigDecimal.ROUND_HALF_UP).intValue());
            }
            if (lastWeek != null) {
                int upFinishedRate = 0;
                if (SafeConverter.toInt(lastWeek.getFinishCnt()) != 0) {
                    upFinishedRate = new BigDecimal(SafeConverter.toInt(lastWeek.getFinishCnt() * 100)).divide(new BigDecimal(SafeConverter.toInt(lastWeek.getHomeworkCnt())), BigDecimal.ROUND_HALF_UP).intValue();
                }
                weekReportForStudent.setUpFinishedRate(upFinishedRate);
            }
            if (currentWeek.getAvgScore() != null) {
                weekReportForStudent.setScore(new BigDecimal(currentWeek.getAvgScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                weekReportForStudent.setScoreLevel(ScoreLevel.processLevel(weekReportForStudent.getScore()).getLevel());
            }
            if (currentWeek.getGroupAvgScore() != null) {
                weekReportForStudent.setAvgScore(new BigDecimal(currentWeek.getGroupAvgScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                weekReportForStudent.setAvgScoreLevel(ScoreLevel.processLevel(weekReportForStudent.getAvgScore()).getLevel());
            }
            if (lastWeek != null && lastWeek.getAvgScore() != null) {
                weekReportForStudent.setUpScore(new BigDecimal(lastWeek.getAvgScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            }
            weekReportForStudent.setWrongQuestionNum(SafeConverter.toInt(currentWeek.getWrongCnt()));
            weekReportForStudent.setAvgWrongQuestionNum(SafeConverter.toInt(currentWeek.getGroupWrongCnt()));
            weekReportForStudent.setCumulativeWrongQuestionNum(SafeConverter.toInt(currentWeek.getTotalWrongCnt()));
            if (lastWeek != null) {
                weekReportForStudent.setUpWrongQuestionNum(SafeConverter.toInt(lastWeek.getWrongCnt()));
            }
            if (currentWeek.getGroupAvgScore() != null) {
                weekReportForStudent.setHighestScore(new BigDecimal(currentWeek.getGroupMaxScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            }
            if (CollectionUtils.isNotEmpty(currentWeek.getUnits())) {
                Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(currentWeek.getUnits());
                if (MapUtils.isNotEmpty(stringNewBookCatalogMap)) {
                    List<NewBookCatalog> newBookCatalogs = stringNewBookCatalogMap.values()
                            .stream()
                            .sorted((o1, o2) -> Integer.compare(o1.getRank(), o2.getRank()))
                            .collect(Collectors.toList());
                    weekReportForStudent.setUnitNum(stringNewBookCatalogMap.size());
                    for (NewBookCatalog newBookCatalog : newBookCatalogs) {
                        if (subject == Subject.ENGLISH) {
                            weekReportForStudent.getUnitNames().add(newBookCatalog.getAlias());
                        } else {
                            weekReportForStudent.getUnitNames().add(newBookCatalog.getName());
                        }
                    }
                }
            }
            weekReportForStudent.setQuestionNum(SafeConverter.toInt(currentWeek.getTotalQuestionCnt()));
            weekReportForStudent.setWordNum(SafeConverter.toInt(currentWeek.getWordCnt()));
            weekReportForStudent.setKnowledgeNum(SafeConverter.toInt(currentWeek.getKpCnt()));
            weekReportForStudent.setHandleKnowledgeNum(SafeConverter.toInt(currentWeek.getMasterKpCnt()));
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("weekReportForStudent", weekReportForStudent);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Week Report For Student failed : subject of  {}, studentReportId of {},  user of {}", subject, studentReportId, user, e);
            return MapMessage.errorMessage();
        }
    }
}

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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.api.entity.MentorRewardHistory;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.BusinessTeacherServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.mentor.client.MentorServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.api.constant.MentorCategory.*;
import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.MENTOR_SYSTEM_TEACHER_MENTOR_LIST;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/7/2015
 */
@Named
@Deprecated
public class DeprecatedMentorService extends BusinessServiceSpringBean {

    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;

    @Inject private BusinessCacheSystem businessCacheSystem;

    @Inject private BusinessTeacherServiceImpl businessTeacherService;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private CertificationServiceClient certificationServiceClient;
    @Inject private MentorServiceClient mentorServiceClient;

    public MentorHistory findAvailableMentorHistoryByMenteeId(Long menteeId, String mentorType) {
        if (menteeId == null) {
            return null;
        }
        return mentorServiceClient.getRemoteReference()
                .findMentorHistoriesByMenteeId(menteeId)
                .getUninterruptibly()
                .stream()
                .filter(e -> DateUtils.dayDiff(new Date(), e.getCreateDatetime()) < 10)
                .filter(e -> !Boolean.TRUE.equals(e.getSuccess()))
                .filter(e -> StringUtils.equals(e.getMentorCategory(), mentorType))
                .findFirst()
                .orElse(null);
    }

    public List<MentorHistory> findAvailableMentorHistoryByMentorId(Long mentorId) {
        if (mentorId == null) {
            return Collections.emptyList();
        }
        return mentorServiceClient.getRemoteReference()
                .findMentorHistoriesByMentorId(mentorId)
                .getUninterruptibly()
                .stream()
                .filter(e -> DateUtils.dayDiff(new Date(), e.getCreateDatetime()) < 10)
                .filter(e -> !Boolean.TRUE.equals(e.getSuccess()))
                .collect(Collectors.toList());
    }

    public Map<Long, MentorHistory> findAvailableMentorHistoryByMenteeIds(Collection<Long> menteeIds, String mentorCategory) {
        if (CollectionUtils.isEmpty(menteeIds) || StringUtils.isBlank(mentorCategory)) {
            return Collections.emptyMap();
        }
        AlpsFutureMap<Long, List<MentorHistory>> futureMap = new AlpsFutureMap<>();
        for (Long menteeId : menteeIds) {
            futureMap.put(menteeId, mentorServiceClient.getRemoteReference().findMentorHistoriesByMenteeId(menteeId));
        }
        Map<Long, List<MentorHistory>> mhm = futureMap.regularize();
        Map<Long, MentorHistory> result = new HashMap<>();
        for (Long menteeId : mhm.keySet()) {
            MentorHistory history = mhm.getOrDefault(menteeId, Collections.emptyList()).stream()
                    .filter(e -> DateUtils.dayDiff(new Date(), e.getCreateDatetime()) < 10)
                    .filter(e -> !Boolean.TRUE.equals(e.getSuccess()))
                    .filter(e -> StringUtils.equals(e.getMentorCategory(), mentorCategory))
                    .findFirst()
                    .orElse(null);

            //FIXME: 这里原来的逻辑没有判null，即便是null的MentorHistory也会被放入map中
            result.put(menteeId, history);
        }
        return result;
    }

    //获取未认证老师的mentor 或者 可选择的mentor列表
    public MapMessage findMyMentorOrCandidates(Long menteeId, MentorCategory mentorCategory) {
        MentorHistory history = findAvailableMentorHistoryByMenteeId(menteeId, mentorCategory.name());
        if (history != null) {
            User mentor = userLoaderClient.loadUser(history.getMentorId());
            if (mentor == null) {
                return MapMessage.errorMessage();
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", mentor.getId());
            map.put("name", mentor.fetchRealname());
            map.put("img", mentor.fetchImageUrl());
            String phone = sensitiveUserDataServiceClient.showUserMobile(mentor.getId(), "be:findMyMentorOrCandidates", SafeConverter.toString(menteeId));
            map.put("mobile", StringUtils.defaultString(phone));
            return MapMessage.successMessage().add("mentor", map);
        } else {
            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(menteeId)
                    .getUninterruptibly();
            if (school == null) {
                return MapMessage.errorMessage();
            }
            List<Map<String, Object>> list = new ArrayList<>();
            List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(school.getId())
                    .stream().filter(source -> source.fetchCertificationState() == SUCCESS).collect(Collectors.toList());
            List<Long> teacherIdList = teachers.stream()
                    .map(Teacher::getId)
                    .collect(Collectors.toList());
            // 过滤掉副账号老师
            Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(teacherIdList);
            teachers = teachers.stream().filter(t -> mainTeacherIds.get(t.getId()) == null).collect(Collectors.toList());

            Map<Long, List<MentorHistory>> teacher_history_map;
            if (CollectionUtils.isEmpty(teacherIdList)) {
                teacher_history_map = Collections.emptyMap();
            } else {
                AlpsFutureMap<Long, List<MentorHistory>> futureMap = new AlpsFutureMap<>();
                for (Long tid : teacherIdList) {
                    futureMap.put(tid, mentorServiceClient.getRemoteReference().findMentorHistoriesByMentorId(tid));
                }
                teacher_history_map = futureMap.regularize();
            }
            Map<Long, Teacher> ambassadorMap = ambassadorLoaderClient.getAmbassadorLoader().loadSchoolAmbassadors(school.getId());
            for (Teacher teacher : teachers) {
                List<MentorHistory> histories = teacher_history_map.get(teacher.getId());
                if (CollectionUtils.isNotEmpty(histories)) {
                    long count = histories.stream()
                            .filter(e -> DateUtils.dayDiff(new Date(), e.getCreateDatetime()) < 10)
                            .filter(e -> !SafeConverter.toBoolean(e.getSuccess()))
                            .count();
                    if (count >= 4) {
                        continue;
                    }
                }
                //最近一个月 登陆过
//                Date lastLoginTime = userLoaderClient.findUserLastLoginTime(teacher);
                Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacher.getId());
                if (lastLoginTime == null || DateUtils.dayDiff(new Date(), lastLoginTime) > 30) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", teacher.getId());
                map.put("name", teacher.fetchRealname());
                map.put("img", teacher.fetchImageUrl());
                map.put("days", DateUtils.dayDiff(new Date(), teacher.getCreateTime()));
                map.put("subject", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
                if (!ambassadorMap.containsKey(teacher.getId())) {
                    map.put("isAmbassador", false);
                    list.add(map);
                } else {
                    map.put("isAmbassador", true);
                    list.add(0, map);
                }
            }
            list = list.stream().sorted((o1, o2) ->
                    Integer.compare(SafeConverter.toInt(o2.get("days")), SafeConverter.toInt(o1.get("days"))))
                    .sorted((o1, o2) -> Boolean.compare(SafeConverter.toBoolean(o2.get("isAmbassador")), SafeConverter.toBoolean(o1.get("isAmbassador")))).collect(Collectors.toList());
            return MapMessage.successMessage().add("mentorList", list);
        }
    }

    public MapMessage setUpMMRelationship(Long mentorId, Long menteeId, MentorCategory mentorCategory, MentorType mentorType) {
        if (menteeId == null || mentorId == null) {
            return MapMessage.errorMessage("操作失败");
        }
        Map<Long, User> map = userLoaderClient.loadUsers(Arrays.asList(menteeId, mentorId));
        User mentor = map.get(mentorId);
        User mentee = map.get(menteeId);
        if (mentor == null || mentee == null) {
            return MapMessage.errorMessage("操作失败");
        }
        if (Objects.equals(mentor.getId(), mentee.getId())) {
            return MapMessage.errorMessage("自己不能帮助自己哦~");
        }
        if (mentor.fetchCertificationState() != SUCCESS) {
            return MapMessage.errorMessage("操作失败");
        }
        List<MentorHistory> histories = mentorServiceClient.getRemoteReference()
                .findMentorHistoriesByMentorId(mentorId)
                .getUninterruptibly()
                .stream()
                .filter(e -> DateUtils.dayDiff(new Date(), e.getCreateDatetime()) < 10)
                .filter(e -> !SafeConverter.toBoolean(e.getSuccess()))
                .collect(Collectors.toList());
        if (histories.size() >= 4) {
            return MapMessage.errorMessage("导师只能同时帮助四个老师");
        }
        for (MentorHistory history : histories) {
            if (history.getMenteeId().equals(menteeId) && Objects.equals(history.getMentorCategory(), mentorCategory.name())) {
                return MapMessage.successMessage();
            }
        }
        histories = mentorServiceClient.getRemoteReference()
                .findMentorHistoriesByMenteeId(menteeId)
                .getUninterruptibly()
                .stream()
                .filter(e -> DateUtils.dayDiff(new Date(), e.getCreateDatetime()) < 10)
                .filter(e -> !Boolean.TRUE.equals(e.getSuccess()))
                .filter(e -> StringUtils.equals(e.getMentorCategory(), mentorCategory.name()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(histories)) {
            Set<Long> ids = histories.stream().map(MentorHistory::getMentorId).collect(Collectors.toSet());
            if (ids.contains(mentorId)) {
                return MapMessage.errorMessage("您已经与该老师建立帮助关系了").add("type", "HBYM");
            } else {
                return MapMessage.errorMessage("该老师当前已经被其他老师帮助了").add("type", "HBYO");
            }
        }
        int stCount;
        switch (mentorCategory) {
            case MENTOR_AUTHENTICATION:
                if (mentee.fetchCertificationState() == SUCCESS) {
                    return MapMessage.errorMessage("您要帮助的人已经认证成功了");
                }
                mentorServiceClient.getRemoteReference()
                        .insertMentorHistory(new MentorHistory(mentorId,
                                menteeId,
                                mentorType.name(),
                                mentorCategory.name(),
                                MentorLevel.AUTHENTICATION_DEFAULT.name()))
                        .awaitUninterruptibly();
                break;
            case MENTOR_NEW_ST_COUNT:
                if (mentee.fetchCertificationState() != SUCCESS) {
                    return MapMessage.errorMessage("您要帮助的人还没有认证");
                }
                //记录当前老师的新学生级别
                MentorLevel mentorLevel = MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_ONE;
                stCount = businessTeacherService.studentsFinishedHomeworkCount(menteeId, mentee.getCreateTime());
                if (stCount >= 30 && stCount < 60) {
                    mentorLevel = MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_TWO;
                } else if (stCount >= 60 && stCount < 90) {
                    mentorLevel = MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_THREE;
                }
                mentorServiceClient.getRemoteReference()
                        .insertMentorHistory(new MentorHistory(mentorId,
                                menteeId,
                                mentorType.name(),
                                mentorCategory.name(),
                                mentorLevel.name()))
                        .awaitUninterruptibly();
                break;
            case MENTOR_TERM_END:
                //记录当前老师的新学生级别
                MentorLevel termEndLevel = MentorLevel.MENTOR_TERM_END_LEVEL_ONE;
                stCount = businessTeacherService.studentsFinishedHomeworkCount(menteeId, DateUtils.stringToDate("2015-05-26 00:00:00"));
                if (stCount >= 30 && stCount < 60) {
                    termEndLevel = MentorLevel.MENTOR_TERM_END_LEVEL_TWO;
                } else if (stCount >= 60 && stCount < 90) {
                    termEndLevel = MentorLevel.MENTOR_TERM_END_LEVEL_THREE;
                }
                mentorServiceClient.getRemoteReference()
                        .insertMentorHistory(new MentorHistory(mentorId,
                                menteeId,
                                mentorType.name(),
                                mentorCategory.name(),
                                termEndLevel.name()))
                        .awaitUninterruptibly();
                break;
            default:
        }
        // 清除缓存(按照mentee的学校清除，因为mentor和mentee有可能不是同校的【邀请】)
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(menteeId)
                .getUninterruptibly();
        if (school != null) {
            //清除帮助邀请列表缓存
            if (MENTOR_NEW_ST_COUNT == mentorCategory) {
                asyncBusinessCacheService.MentorCacheManager_clean(school.getId()).awaitUninterruptibly();
            }
            if (MENTOR_TERM_END == mentorCategory) {
                asyncBusinessCacheService.MentorTermEndCacheManager_clean(school.getId()).awaitUninterruptibly();
            }

        }
        String key = CacheKeyGenerator.generateCacheKey(MENTOR_SYSTEM_TEACHER_MENTOR_LIST, null, new Object[]{mentorId});
        businessCacheSystem.CBS.flushable.delete(key);
        return MapMessage.successMessage();
    }


    public PageImpl<Map<String, Object>> getUncertificatedTeacherListPage(Long schoolId, int pageNum, int pageSize) {
        if (schoolId == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 学校未认证教师
        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId)
                .stream().filter(source -> source.fetchCertificationState() != SUCCESS).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(teachers)) {
            return new PageImpl<>(Collections.emptyList());
        }
        List<Long> teacherIds = teachers.stream()
                .map(Teacher::getId)
                .collect(Collectors.toList());
        // 过滤虚假老师
        Map<Long, TeacherExtAttribute> extAttributeMap = teacherLoaderClient.loadTeacherExtAttributes(teacherIds);
        teachers = teachers.stream()
                .filter(t -> extAttributeMap.get(t.getId()) == null || !extAttributeMap.get(t.getId()).isFakeTeacher())
                .collect(Collectors.toList());
        // 继续过滤掉3天内登录过的老师
        List<Long> filterTeacherIds = teachers.stream()
                .map(Teacher::getId)
                .collect(Collectors.toList());
//        Map<Long, UserLoginInfo> loginInfoMap = userLoaderClient.loadUserLoginInfo(filterTeacherIds);
        Map<Long, UserLoginInfo> loginInfoMap = userLoginServiceClient.getUserLoginService()
                .loadUserLoginInfo(filterTeacherIds).getUninterruptibly();
        teachers = teachers.stream()
                .filter(t -> {
                    Date lastLoginTime = null;
                    if (loginInfoMap.containsKey(t.getId()) && loginInfoMap.get(t.getId()).getLoginTime() != null) {
                        lastLoginTime = loginInfoMap.get(t.getId()).getLoginTime();
                    }
                    return lastLoginTime == null || DateUtils.dayDiff(new Date(), lastLoginTime) >= 3;
                })
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(teachers)) {
            return new PageImpl<>(Collections.emptyList());
        }
        teacherIds = teachers.stream()
                .map(Teacher::getId)
                .collect(Collectors.toList());
        // 过滤绑定手机的老师
        Map<Long, UserAuthentication> userAuthenticationMap = userLoaderClient.loadUserAuthentications(teacherIds);
        teachers = teachers.stream().filter(t -> userAuthenticationMap.get(t.getId()) != null && userAuthenticationMap.get(t.getId()).isMobileAuthenticated())
                .collect(Collectors.toList());
        // 包班制支持
        // 过滤副账号老师
        Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(teacherIds);
        teachers = teachers.stream().filter(t -> mainTeacherIds.get(t.getId()) == null).collect(Collectors.toList());
        // 排序
        Collections.sort(teachers, (o1, o2) -> {
            int name1 = StringUtils.isBlank(o1.fetchRealname()) ? 0 : 1;
            int name2 = StringUtils.isBlank(o2.fetchRealname()) ? 0 : 1;
            if (name1 > name2) {
                return -1;
            } else if (name1 < name2) {
                return 1;
            } else {
                long time1 = ConversionUtils.toLong(o1.getCreateTime());
                long time2 = ConversionUtils.toLong(o2.getCreateTime());
                return Long.compare(time2, time1);
            }
        });
        // 分页处理
        long total = teachers.size();
        if (pageNum * pageSize > total) {
            // 请正确填写页码
            return new PageImpl<>(Collections.emptyList());
        }
        int start = pageNum * pageSize;
        int end = Math.min((int) total, ((pageNum + 1) * pageSize));
        teachers = new LinkedList<>(teachers.subList(start, end));
        // 处理数据
        List<Long> tids = teachers.stream().map(Teacher::getId).collect(Collectors.toList());
        // 学生
        Map<Long, List<User>> sm = studentLoaderClient.loadTeacherStudents(tids);
        // 作业
        Map<Long, UserActivity> uacm = userActivityServiceClient.getUserActivityService()
                .findUserActivities(tids)
                .getUninterruptibly()
                .values()
                .stream()
                .map(t -> t.stream()
                        .filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                        .sorted((o1, o2) -> {
                            long a1 = SafeConverter.toLong(o1.getActivityTime());
                            long a2 = SafeConverter.toLong(o2.getActivityTime());
                            return Long.compare(a2, a1);
                        })
                        .findFirst()
                        .orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toMap(UserActivity::getUserId, t -> t));
        // 导师
        Map<Long, MentorHistory> mhm = findAvailableMentorHistoryByMenteeIds(tids, MENTOR_AUTHENTICATION.name());
        List<Long> mentorIds = new ArrayList<>();
        for (Long menteeId : mhm.keySet()) {
            MentorHistory history = mhm.get(menteeId);
            if (history != null) {
                mentorIds.add(history.getMentorId());
            }
        }
        Map<Long, User> mentors = userLoaderClient.loadUsers(mentorIds);
        // 组装数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (Teacher teacher : teachers) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", teacher.getId());
            data.put("userName", teacher.fetchRealname());
            String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "be:getUncertificatedTeacherListPage", SafeConverter.toString(teacher.getId()));
            data.put("mobile", StringUtils.defaultString(phone));
            data.put("login", userLoginServiceClient.findUserLastLoginTime(teacher.getId()) != null);
            List<User> students = sm.get(teacher.getId());
            if (CollectionUtils.isEmpty(students)) {
                data.put("clazzFlag", false);
                data.put("studentLoginFlag", false);
            } else {
                data.put("clazzFlag", true);
                data.put("studentLoginFlag", userLoginServiceClient.validateLoginUserCount(
                        students.stream().map(User::getId).collect(Collectors.toList()), 2));
            }
            data.put("hkFlag", uacm.get(teacher.getId()) != null);
            MentorHistory history = mhm.get(teacher.getId());
            if (history != null) {
                data.put("mentorExist", true);
                data.put("mentorName", mentors.get(history.getMentorId()) != null ? mentors.get(history.getMentorId()).fetchRealname() : "");
            } else {
                data.put("mentorExist", false);
            }
            result.add(data);
        }
        return new PageImpl<>(result, new PageRequest(pageNum, pageSize), total);
    }

    public List<Map<String, Object>> getMentoringTeacherList(Long mentorId) {
        if (mentorId == null) {
            return Collections.emptyList();
        }
        List<MentorHistory> histories = findAvailableMentorHistoryByMentorId(mentorId);
        Set<Long> menteeIds = histories.stream().map(MentorHistory::getMenteeId).collect(Collectors.toSet());
        Map<Long, User> mentees = userLoaderClient.loadUsers(menteeIds);
        // 手机号码
        Map<Long, UserAuthentication> uaum = userLoaderClient.loadUserAuthentications(menteeIds);
        // 学生
        Map<Long, List<User>> sm = studentLoaderClient.loadTeacherStudents(menteeIds);
        // 作业
        Map<Long, UserActivity> uacm = userActivityServiceClient.getUserActivityService()
                .findUserActivities(menteeIds)
                .getUninterruptibly()
                .values()
                .stream()
                .map(t -> t.stream()
                        .filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                        .sorted((o1, o2) -> {
                            long a1 = SafeConverter.toLong(o1.getActivityTime());
                            long a2 = SafeConverter.toLong(o2.getActivityTime());
                            return Long.compare(a2, a1);
                        })
                        .findFirst()
                        .orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toMap(UserActivity::getUserId, t -> t));
        //班级
        Map<Long, List<Clazz>> cm = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(menteeIds);

        // 组装数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (MentorHistory history : histories) {
            User mentee = mentees.get(history.getMenteeId());
            if (mentee == null) {
                continue;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("category", history.getMentorCategory());
            data.put("userId", mentee.getId());
            data.put("userName", mentee.fetchRealname());
            data.put("userImg", mentee.fetchImageUrl());
            String phone = sensitiveUserDataServiceClient.showUserMobile(mentee.getId(), "be:getMentoringTeacherList", SafeConverter.toString(mentorId));
            data.put("mobile", StringUtils.defaultString(phone));
            data.put("daysLeft", Math.max(0, 10 - DateUtils.dayDiff(new Date(), history.getCreateDatetime())));
            data.put("mhid", history.getId());
            MentorCategory category = MENTOR_AUTHENTICATION;
            if (StringUtils.isNotBlank(history.getMentorCategory()) &&
                    MentorCategory.valueOf(history.getMentorCategory()) != null) {
                category = MentorCategory.valueOf(history.getMentorCategory());
            }
            switch (category) {
                case MENTOR_AUTHENTICATION:
//                    Date lastLoginTime = userLoaderClient.findUserLastLoginTime(mentee);
                    Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(mentee.getId());
                    boolean login = lastLoginTime != null;
                    data.put("login", login);
                    boolean clazzFlag = false;
                    boolean studentLoginFlag = false;
                    List<User> students = sm.get(mentee.getId());
                    if (login && CollectionUtils.isNotEmpty(students)) {
                        clazzFlag = true;
//                        studentLoginFlag = userLoaderClient.validateLoginUserCount(students, 2);
                        studentLoginFlag = userLoginServiceClient.validateLoginUserCount(
                                students.stream().map(User::getId).collect(Collectors.toList()), 2);
                    }
                    data.put("clazzFlag", clazzFlag);
                    data.put("studentLoginFlag", studentLoginFlag);
                    data.put("hkFlag", uacm.get(mentee.getId()) != null);
                    break;
                case MENTOR_TERM_END:
                    data.put("clazzCount", 0);
                    if (CollectionUtils.isNotEmpty(cm.get(mentee.getId()))) {
                        data.put("clazzCount", cm.get(mentee.getId()).size());
                    }
                    data.put("stCount", businessTeacherService.studentsFinishedHomeworkCount(mentee.getId(),
                            DateUtils.stringToDate("2015-05-26 00:00:00")));
                    break;
                case MENTOR_NEW_ST_COUNT:
                    data.put("clazzCount", 0);
                    if (CollectionUtils.isNotEmpty(cm.get(mentee.getId()))) {
                        data.put("clazzCount", cm.get(mentee.getId()).size());
                    }
                    data.put("stCount", businessTeacherService.studentsFinishedHomeworkCount(mentee.getId(), mentee.getCreateTime()));
                    break;
                default:
            }
            result.add(data);
        }
        return result;
    }

    //完成认证 结束mentor关系， 加奖励
    public void addRewardToMentorForAuth(Long menteeId) {
        if (menteeId == null) {
            return;
        }
        MentorHistory history = findAvailableMentorHistoryByMenteeId(menteeId, MENTOR_AUTHENTICATION.name());
        if (history == null) {
            return;
        }
        User mentor = userLoaderClient.loadUser(history.getMentorId());
        if (mentor == null || mentor.fetchCertificationState() != SUCCESS) {
            return;
        }
        IntegralHistory integralHistory = new IntegralHistory(mentor.getId(), IntegralType.教师帮助教师认证_产品平台, 1000);
        integralHistory.setComment("帮助教师认证获得园丁豆");
        if (userIntegralService.changeIntegral(integralHistory).isSuccess()) {
            mentorServiceClient.getRemoteReference().setMentorHistorySuccess(history.getId()).awaitUninterruptibly();
        }

        //记录奖励历史
        MentorRewardHistory rewardHistory = MentorRewardHistory.newInstance(mentor.getId());
        rewardHistory.setMenteeId(menteeId);
        rewardHistory.setMentorCategory(MENTOR_AUTHENTICATION.name());
        rewardHistory.setAmount(100);
        rewardHistory.setRewardCategory("GOLD");
        mentorServiceClient.getRemoteReference()
                .persistMentorRewardHistory(rewardHistory)
                .awaitUninterruptibly();

        School menteeSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(menteeId)
                .getUninterruptibly();
        School mentorSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(mentor.getId())
                .getUninterruptibly();
        //预备大使 加努力值  只有同校的才加
        if (Objects.equals(menteeSchool.getId(), mentorSchool.getId())) {
            ambassadorServiceClient.getAmbassadorService().addCompetitionScore(mentor.getId(), menteeId, AmbassadorCompetitionScoreType.HELP_TEACHER);
            // 正式大使添加积分
            ambassadorServiceClient.getAmbassadorService().addAmbassadorScore(mentor.getId(), menteeId, AmbassadorCompetitionScoreType.HELP_TEACHER);
        }
        String key = CacheKeyGenerator.generateCacheKey(MENTOR_SYSTEM_TEACHER_MENTOR_LIST, null, new Object[]{mentor.getId()});
        businessCacheSystem.CBS.flushable.delete(key);
        //清除首页动态
        asyncBusinessCacheService.MentorLatestCacheManager_clean(mentor.getId()).awaitUninterruptibly();
    }

    //帮助老师扩充学生数量：本校所有认证30天以内的老师
    public List<Map<String, Object>> getIncrStudentCountTeacherList(Long schoolId) {
        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
        List<Teacher> authTeachers = teachers.stream()
                .filter(t -> AuthenticationState.SUCCESS == t.fetchCertificationState())
                .collect(Collectors.toList());
        List<Long> tids = authTeachers.stream()
                .map(Teacher::getId)
                .distinct()
                .collect(Collectors.toList());
        // 导师
        Map<Long, MentorHistory> mhm = findAvailableMentorHistoryByMenteeIds(tids, MENTOR_NEW_ST_COUNT.name());
        List<Long> mentorIds = new ArrayList<>();
        for (Long menteeId : mhm.keySet()) {
            MentorHistory history = mhm.get(menteeId);
            if (history != null) {
                mentorIds.add(history.getMentorId());
            }
        }
        Map<Long, User> mentors = userLoaderClient.loadUsers(mentorIds);
        // 是否是副账号
        Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(tids);
        Map<Long, AlpsFuture<Date>> futures = new HashMap<>();
        for (Long tid : tids) {
            futures.put(tid, certificationServiceClient.getRemoteReference().getAuthenticationDate(tid));
        }
        //认证时间
        List<Map<String, Object>> result = new ArrayList<>();
        for (Teacher teacher : authTeachers) {
            //过滤掉副账号
            if (mainTeacherIds.get(teacher.getId()) != null) {
                continue;
            }
            //老师认证时间不在30天以内
            Date authDate = null;
            AlpsFuture<Date> future = futures.get(teacher.getId());
            if (future != null) {
                authDate = future.getUninterruptibly();
            }
            if (authDate == null || DateUtils.calculateDateDay(new Date(), -30).after(authDate)) {
                continue;
            }
            //count 大于90人 不显示
            int stCount = businessTeacherService.studentsFinishedHomeworkCount(teacher.getId(), teacher.getCreateTime());
            if (stCount > 90) {
                continue;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("userId", teacher.getId());
            data.put("userName", teacher.fetchRealname());
            data.put("userImg", teacher.fetchImageUrl());
            data.put("createTime", teacher.fetchCreateTimestamp());
            String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "be:getIncrStudentCountTeacherList", SafeConverter.toString(teacher.getId()));
            data.put("mobile", StringUtils.defaultString(phone));
            MentorHistory history = mhm.get(teacher.getId());
            if (history != null) {
                data.put("mentorExist", true);
                data.put("mentorName", mentors.get(history.getMentorId()) != null ? mentors.get(history.getMentorId()).fetchRealname() : "");
            } else {
                data.put("mentorExist", false);
            }
            data.put("sCount", stCount);
            result.add(data);
        }
        return result;
    }

    //获取老师首页 mentor 动态内容
    public Map<String, Object> getMentorLatestInfo(Long teacherId) {
        Map<String, Object> dataMap = new HashMap<>();
        List<MentorRewardHistory> rewardHistories = mentorServiceClient.getRemoteReference()
                .findMentorRewardHistoriesByMentorId(teacherId)
                .getUninterruptibly();
        String oneContent = "";
        String twoContent = "";
        int warningCount = 0;
        int mentoringCount = 0;
        int stCount = 0;
        // 过滤虚假老师
        rewardHistories = rewardHistories.stream().filter(h -> !teacherLoaderClient.isFakeTeacher(h.getMenteeId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(rewardHistories)) {
            MentorRewardHistory history = MiscUtils.firstElement(rewardHistories);
            Teacher mentee = teacherLoaderClient.loadTeacher(history.getMenteeId());
            if (mentee != null) {
                switch (MentorCategory.valueOf(history.getMentorCategory())) {
                    case MENTOR_AUTHENTICATION:
                        oneContent = "老师" + mentee.fetchRealname() + "完成认证，园丁豆";
                        twoContent = mentee.fetchRealname() + "<span>完成认证</span>";
                        break;
                    case MENTOR_NEW_ST_COUNT:
                        stCount = businessTeacherService.studentsFinishedHomeworkCount(mentee.getId(), mentee.getCreateTime());
                        oneContent = mentee.fetchRealname() + "完成" + stCount + "个新生，园丁豆";
                        twoContent = mentee.fetchRealname() + "<span>完成" + stCount + "个新生</span>";
                        break;
                    case MENTOR_TERM_END:
                        stCount = businessTeacherService.studentsFinishedHomeworkCount(mentee.getId(),
                                DateUtils.stringToDate("2015-05-26 00:00:00"));
                        oneContent = "老师" + mentee.fetchRealname() + "完成" + stCount + "个新生，园丁豆";
                        twoContent = mentee.fetchRealname() + "<span>完成" + stCount + "个新生</span>";
                        break;
                    default:
                }
            }
        }
        List<MentorHistory> histories = findAvailableMentorHistoryByMentorId(teacherId);
        if (CollectionUtils.isNotEmpty(histories)) {
            mentoringCount = histories.size();
        }
        List<MentorHistory> warningHis = histories.stream()
                .filter(m -> Math.max(0, 10 - DateUtils.dayDiff(new Date(), m.getCreateDatetime())) <= 3)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(warningHis)) {
            warningCount = warningHis.size();
        }
        dataMap.put("oneContent", oneContent);
        dataMap.put("twoContent", twoContent);
        dataMap.put("warningCount", warningCount);
        dataMap.put("mentoringCount", mentoringCount);
        return dataMap;
    }
}

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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.client.ClazzJournalLoaderClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineCacheKeyGenerator;
import com.voxlearning.washington.mapper.studentheadline.StudentAchievementHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentClazzAchievementHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHomeworkHeadlineMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 4/8/2016
 */
@Controller
@RequestMapping(value = "/studentMobile/clazz")
public class MobileStudentClazzController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    /**
     * 查询学生班级头条
     *
     * @deprecated Use {@link com.voxlearning.washington.controller.mobile.student.headline.MobileStudentClazzV1Controller#headline()} instead
     */
    @RequestMapping(value = "/headline.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage headline() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }

            List<ClazzJournal> clazzJournals;
            String journalsCacheKey = "APPLICATION_STD_CLAZZ_JOURNAL_" + clazz.getId();
            CacheObject<List<ClazzJournal>> clazzJournalCacheObject = washingtonCacheSystem.CBS.flushable.get(journalsCacheKey);
            if (null == clazzJournalCacheObject || null == clazzJournalCacheObject.getValue()) {

                Set<ClazzJournal.ComplexID> complexIDs = clazzJournalLoaderClient.getClazzJournalLoader().__queryByClazzId(clazz.getId());
                Set<Long> ids = complexIDs.stream()
                        .filter(p -> Objects.equals(ClazzJournalCategory.APPLICATION_STD.getId(), p.getCategory()))
                        .map(ClazzJournal.ComplexID::getId)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(ids)) {
                    return MapMessage.successMessage();
                }

                Map<Long, ClazzJournal> clazzJournalMap = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournals(ids);
                if (MapUtils.isEmpty(clazzJournalMap)) {
                    return MapMessage.successMessage();
                }

                clazzJournals = clazzJournalMap.values()
                        .stream()
                        //要7天以内的记录
                        .filter(cj -> Instant.now().minusSeconds(7 * 24 * 60 * 60).isBefore(Instant.ofEpochMilli(cj.getCreateDatetime().getTime())))
                        //按时间倒序排序
                        .sorted((c1, c2) -> c2.getCreateDatetime().compareTo(c1.getCreateDatetime()))
                        .collect(Collectors.toList());

                washingtonCacheSystem.CBS.flushable.set(journalsCacheKey, 10 * 60, clazzJournals);
            } else {
                clazzJournals = clazzJournalCacheObject.getValue();
            }

            List<Long> relativeUserIds = clazzJournals.stream()
                    .map(ClazzJournal::getRelevantUserId)
                    .collect(Collectors.toList());
            // 获得学生信息map
            Map<Long, StudentInfo> studentInfoMap = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfos(relativeUserIds);

            int studentCountInClazz;
//            String studentCountCacheKey = "STUDENT_APP_HEADLINE_STUDENT_COUNT_" + currentUserId();
            String studentCountCacheKey = HeadlineCacheKeyGenerator.studentCountKey(currentUserId());
            CacheObject<Integer> studentCountInClazzCacheObject = washingtonCacheSystem.CBS.flushable.get(studentCountCacheKey);
            if (null == studentCountInClazzCacheObject || null == studentCountInClazzCacheObject.getValue()) {
                studentCountInClazz = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), currentUserId()).size();

                washingtonCacheSystem.CBS.flushable.set(studentCountCacheKey, DateUtils.getCurrentToDayEndSecond(), studentCountInClazz);
            } else {
                studentCountInClazz = studentCountInClazzCacheObject.getValue();
            }

            List<StudentHeadlineMapper> mappers = new ArrayList<>();
            List<ClazzJournal> achievementJournals = new ArrayList<>();
            Set<Long> groupIds = getCurrentUserGroupIds();
            StudentInfo studentInfo;
            for (ClazzJournal clazzJournal : clazzJournals) {
                if (clazzJournal.getJournalType() == ClazzJournalType.HOMEWORK_HEADLINE) {
                    StudentHomeworkHeadlineMapper studentHomeworkHeadlineMapper = fillHomeworkHeadline(clazzJournal, groupIds);
                    if (null == studentHomeworkHeadlineMapper) continue;
                    studentHomeworkHeadlineMapper.setTotalCount(studentCountInClazz);

                    mappers.add(studentHomeworkHeadlineMapper);
                } else if (clazzJournal.getJournalType() == ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE) {
                    StudentClazzAchievementHeadlineMapper mapper = fillClazzAchievementHeadline(clazzJournal);
                    if (null == mapper) continue;

                    // 置上头饰
                    studentInfo = studentInfoMap.get(mapper.getUserId());
                    if (studentInfo != null) {
                        Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
                        if (headWearPrivilege != null)
                            mapper.setHeadWearImg(headWearPrivilege.getImg());
                    }

                    mappers.add(mapper);
                } else if (clazzJournal.getJournalType() == ClazzJournalType.ACHIEVEMENT_HEADLINE) {
                    achievementJournals.add(clazzJournal);
                }
            }

            if (CollectionUtils.isNotEmpty(achievementJournals)) {
                //成就头条只显示到前一天,不显示当天的数据,所以缓存一下
                String cacheKey = "ACHIEVEMENT_HEAD_LINE_" + clazz.getId();
                CacheObject<Object> achievementCacheObject = washingtonCacheSystem.CBS.flushable.get(cacheKey);
                if (null == achievementCacheObject || null == achievementCacheObject.getValue()) {
                    List<StudentAchievementHeadlineMapper> achievementHeadlineMappers = fillAchievementHeadline(achievementJournals);
                    mappers.addAll(achievementHeadlineMappers);

                    // 置上头像
                    achievementHeadlineMappers.forEach(a -> {
                        a.getUserInfos().forEach(userinfo -> {
                            Long userId = SafeConverter.toLong(userinfo.get("userId"));
                            StudentInfo sInfo = studentInfoMap.get(userId);
                            if (sInfo != null) {
                                Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(sInfo.getHeadWearId());
                                if (headWearPrivilege != null)
                                    userinfo.put("headWearImg", headWearPrivilege.getImg());
                            }

                        });
                    });

                    washingtonCacheSystem.CBS.flushable.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), achievementHeadlineMappers);
                } else {
                    mappers.addAll((List<StudentAchievementHeadlineMapper>) achievementCacheObject.getValue());
                }
            }

            //把所有头条放一起再排个序,按时间先后倒序
            mappers = mappers.stream().sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp())).collect(Collectors.toList());

            return MapMessage.successMessage().add("headlines", mappers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private List<StudentAchievementHeadlineMapper> fillAchievementHeadline(List<ClazzJournal> achievementJournals) {
        List<StudentAchievementHeadlineMapper> mappers = new ArrayList<>();

        //成就先按天,再按分类合并,显示到前一天的
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDate = formatter.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        Set<Long> userIds = new HashSet<>();
        Map<String, List<ClazzJournal>> dailyClazzJournalMap = new HashMap<>();
        for (ClazzJournal clazzJournal : achievementJournals) {
            String dt = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(clazzJournal.getCreateDatetime().getTime()), ZoneId.systemDefault()));
            if (currentDate.equals(dt)) continue; //过滤掉当天的

            userIds.add(clazzJournal.getRelevantUserId());

            if (!dailyClazzJournalMap.containsKey(dt)) {
                dailyClazzJournalMap.put(dt, new ArrayList<>());
            }

            dailyClazzJournalMap.get(dt).add(clazzJournal);
        }

        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        for (Map.Entry<String, List<ClazzJournal>> entry : dailyClazzJournalMap.entrySet()) {
            if (entry.getValue().size() == 0) continue;

            //处理每一天的数据,按成就类型分组
            EnumMap<AchievementType, List<ClazzJournal>> typeJournalMap = new EnumMap<>(AchievementType.class);
            for (ClazzJournal clazzJournal : entry.getValue()) {
                Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());
                if (!extInfo.containsKey("achievementType")) continue;
                AchievementType achievementType = AchievementType.of(extInfo.get("achievementType").toString());
                if (null == achievementType) continue;

                if (!typeJournalMap.containsKey(achievementType)) {
                    typeJournalMap.put(achievementType, new ArrayList<>());
                }

                typeJournalMap.get(achievementType).add(clazzJournal);
            }

            for (Map.Entry<AchievementType, List<ClazzJournal>> ety : typeJournalMap.entrySet()) {
                if (ety.getValue().size() == 0) continue;

                //处理每一组,按等级分类
                Map<Integer, List<ClazzJournal>> levelJournalMap = new HashMap<>();
                for (ClazzJournal clazzJournal : ety.getValue()) {
                    Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());
                    if (!extInfo.containsKey("level")) continue;

                    Integer level = SafeConverter.toInt(extInfo.get("level"));
                    if (!levelJournalMap.containsKey(level)) {
                        levelJournalMap.put(level, new ArrayList<>());
                    }
                    levelJournalMap.get(level).add(clazzJournal);
                }

                //转化成前端需要的mapper
                for (Map.Entry<Integer, List<ClazzJournal>> ey : levelJournalMap.entrySet()) {
                    if (ey.getValue().size() == 0) continue;

                    StudentAchievementHeadlineMapper mapper = new StudentAchievementHeadlineMapper();
                    mapper.setLevel(ey.getKey());
                    mapper.setType(ClazzJournalType.ACHIEVEMENT_HEADLINE.name());
                    mapper.setAchievementType(ety.getKey().name());
                    mapper.setAchievementTitle(ety.getKey().getTitle());

                    if (CollectionUtils.isEmpty(mapper.getUserInfos())) {
                        mapper.setUserInfos(new ArrayList<>());
                    }
                    for (ClazzJournal clazzJournal : ey.getValue()) {
                        if (!userMap.containsKey(clazzJournal.getRelevantUserId())) continue;

                        User user = userMap.get(clazzJournal.getRelevantUserId());
                        Map<String, Object> info = new HashMap<>();
                        info.put("userId", user.getId());
                        info.put("userName", user.fetchRealname());
                        info.put("userImg", user.fetchImageUrl());

                        mapper.getUserInfos().add(info);
                        //记录时间,方便排序
                        mapper.setDateTime(dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(clazzJournal.getCreateDatetime().getTime()), ZoneId.systemDefault())));
                        mapper.setTimestamp(clazzJournal.getCreateDatetime().getTime());
                    }

                    mappers.add(mapper);
                }
            }
        }

        return mappers;
    }

    private StudentClazzAchievementHeadlineMapper fillClazzAchievementHeadline(ClazzJournal clazzJournal) {

        StudentClazzAchievementHeadlineMapper mapper = new StudentClazzAchievementHeadlineMapper();
        mapper.setType(ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE.name());

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        if (!extInfo.containsKey("achievementType") || !extInfo.containsKey("level")) return null;
        String achievementType = SafeConverter.toString(extInfo.get("achievementType"));
        AchievementType type = AchievementType.of(achievementType);
        if (null == type) return null;
        mapper.setAchievementTitle(type.getTitle());
        mapper.setAchievementType(achievementType);
        mapper.setLevel(SafeConverter.toInt(extInfo.get("level"), 0));

        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;
        mapper.setUserName(user.fetchRealname());
        mapper.setUserImg(user.fetchImageUrl());
        mapper.setUserId(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        mapper.setDateTime(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(clazzJournal.getCreateDatetime().getTime()), ZoneId.systemDefault())));
        mapper.setTimestamp(clazzJournal.getCreateDatetime().getTime());

        return mapper;
    }

    private StudentHomeworkHeadlineMapper fillHomeworkHeadline(ClazzJournal clazzJournal, Set<Long> groupIds) {
        StudentHomeworkHeadlineMapper mapper = new StudentHomeworkHeadlineMapper();
        mapper.setType(ClazzJournalType.HOMEWORK_HEADLINE.name());

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        if (!extInfo.containsKey("homeworkId") || extInfo.get("homeworkId") == null || !extInfo.containsKey("subject")) {
            return null;
        }

        String homeworkId = SafeConverter.toString(extInfo.get("homeworkId"));
        mapper.setHomeworkId(homeworkId);

        mapper.setSubject(SafeConverter.toString(extInfo.get("subject")));
        Subject subject = Subject.ofWithUnknown(mapper.getSubject());
        if (Subject.UNKNOWN != subject) {
            mapper.setSubjectName(subject.getValue());
        }

        //查询作业完成人数,缓存30分钟
        Integer finishCount = 0;
        String cacheKey = "HOMEWORK_HEADLINE_FINISH_COUNT_" + homeworkId;
        CacheObject<Integer> finishCountCache = washingtonCacheSystem.CBS.flushable.get(cacheKey);
        if (null == finishCountCache || null == finishCountCache.getValue()) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (null == newHomework) return null;
            if (!groupIds.contains(newHomework.getClazzGroupId())) return null; //作业必须是当前用户所在的group的

            Map<String, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.findByHomeworkForReport(newHomework);
            if (MapUtils.isNotEmpty(homeworkResultMap)) {
                List<NewHomeworkResult> results = homeworkResultMap.values().stream().filter(r -> null != r.getFinishAt()).collect(Collectors.toList());
                finishCount = results.size();
            }

            washingtonCacheSystem.CBS.flushable.set(cacheKey, 10 * 60, finishCount);
        } else {
            finishCount = finishCountCache.getValue();
        }
        mapper.setFinishCount(finishCount);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        mapper.setDateTime(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(clazzJournal.getCreateDatetime().getTime()), ZoneId.systemDefault())));
        mapper.setTimestamp(clazzJournal.getCreateDatetime().getTime());

        return mapper;
    }

    private Set<Long> getCurrentUserGroupIds() {
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(currentUserId(), false);
        if (CollectionUtils.isEmpty(groupMappers)) return Collections.emptySet();
        return groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toSet());
    }
}

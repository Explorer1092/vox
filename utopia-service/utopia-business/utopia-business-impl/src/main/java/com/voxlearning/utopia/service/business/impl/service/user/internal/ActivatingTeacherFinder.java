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

package com.voxlearning.utopia.service.business.impl.service.user.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.ActivationType;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.api.constant.ActivationType.*;

/**
 * @author xin.xin
 * @author Rui.Bao
 * @since 2014-04-02
 */
@Named
@Slf4j
@NoArgsConstructor
public class ActivatingTeacherFinder extends BusinessServiceSpringBean {
    @Inject private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;

    public List<ActivateInfoMapper> find(Long teacherId) {
        // 获取唤醒中的激活记录
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());

        List<ActivateInfoMapper> result = new ArrayList<>();
        for (TeacherActivateTeacherHistory history : histories) {
            Teacher teacher = teacherLoaderClient.loadTeacher(history.getInviteeId());
            if (null == teacher || teacher.isDisabledTrue() || teacher.fetchCertificationState() != SUCCESS || teacher.getSubject() == null) {
                continue; // 如果老师不存在，则不要影响其它老师的显示，未认证的老师不显示在唤醒中老师列表中
            }
            result.add(teacherActivateConditionCheck(teacher, history, teacher.getSubject()));
        }
        return result;
    }

//    private ActivateInfoMapper englishTeacherActivateConditioncheck(Teacher teacher, TeacherActivateTeacherHistory history) {
//        // step-1：首先查找此次激活创建时间以后的目标老师的所有班级的所有作业和测验
//        Set<Long> clazzIds = clazzLoaderClient.loadTeacherClazzs(history.getInviteeId()).stream()
//                .filter(t -> !t.isTerminalClazz())
//                .map(Clazz::getId)
//                .filter(t -> t != null)
//                .collect(Collectors.toCollection(LinkedHashSet::new));
//        Map<Long, List<Homework>> clazzHomeworks = homeworkLoaderClient.loadClazzEnglishHomeworks(clazzIds)
//                .toGroup(Homework::getClazzId);
//
//        // FIXME: 把所有的英语作业存在MAP里面，后面有用
//        Map<String, Homework> ALL_HOMEWORKS = new HashMap<>();
//        for (List<Homework> list : clazzHomeworks.values()) {
//            for (Homework homework : list) {
//                ALL_HOMEWORKS.put(homework.getId(), homework);
//            }
//        }
//
//        // FIXME: for keep compatibility
//        Map<Long, List<Quiz>> Q = quizLoaderClient.loadClazzQuizs(clazzIds)
//                .subject(teacher.getSubject()).toGroup(Quiz::getClazzId);
//        Map<ClazzSubject, List<Quiz>> map = new LinkedHashMap<>();
//        for (Map.Entry<Long, List<Quiz>> entry : Q.entrySet()) {
//            Long clazzId = entry.getKey();
//            List<Quiz> list = entry.getValue();
//            ClazzSubject clazzSubject = new ClazzSubject(clazzId, teacher.getSubject());
//            map.put(clazzSubject, list);
//        }
//        // FIXME: 把所有的QUIZ存在MAP里面，后面有用
//        Map<Long, Quiz> ALL_QUIZS = new HashMap<>();
//        for (List<Quiz> list : Q.values()) {
//            for (Quiz quiz : list) {
//                ALL_QUIZS.put(quiz.getId(), quiz);
//            }
//        }
//
//        // step-2：选取激活以后布置的作业和测验
//        List<InnerData> innerDatas = new ArrayList<>();
//        List<HomeworkLocation> checkedHomeworkIds = new ArrayList<>();
//        List<HomeworkLocation> uncheckedHomeworkIds = new ArrayList<>();
//        List<Long> checkedQuizIds = new ArrayList<>();
//        List<Long> uncheckedQuizIds = new ArrayList<>();
//        for (Long clazzId : clazzHomeworks.keySet()) {
//            List<Homework> list = clazzHomeworks.get(clazzId);
//            for (Homework homework : list) {
//                if (homework.getCreateTime().after(history.getCreateTime())) {
//                    innerDatas.add(fromHomework(homework));
//                    HomeworkLocation homeworkLocation = HomeworkLocation.newInstance(HomeworkType.ENGLISH, homework.getId());
//                    if (homework.isHomeworkChecked()) {
//                        checkedHomeworkIds.add(homeworkLocation);
//                    } else {
//                        uncheckedHomeworkIds.add(homeworkLocation);
//                    }
//                }
//            }
//        }
//        for (ClazzSubject clazzSubject : map.keySet()) {
//            List<Quiz> list = map.get(clazzSubject);
//            for (Quiz quiz : list) {
//                if (quiz.getCreateDatetime().after(history.getCreateTime())) {
//                    innerDatas.add(fromQuiz(quiz, HomeworkType.QUIZ_ENGLISH));
//                    if (quiz.isChecked()) {
//                        checkedQuizIds.add(quiz.getId());
//                    } else {
//                        uncheckedQuizIds.add(quiz.getId());
//                    }
//                }
//            }
//        }
//
//        // step-3：初始化ActivateInfoMapper
//        ActivateInfoMapper mapper = build(teacher, history.getActivationType(), history.getId());
//        if (null != teacher.getLastLoginDatetime() && teacher.getLastLoginDatetime().after(history.getCreateTime())) { // 有些历史数据这个字段没数据
//            mapper.setLastLoginDays(buildLoginDays(DateUtils.dayDiff(new Date(), teacher.getLastLoginDatetime())));
//        }
//        if (mapper.getLastLoginDays() == null) {
//            mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师登录网站并布置作业");
//        }
//
//        // step-4：如果老师被唤醒后布置过作业或者测验，先处理未检查的
//        if (!innerDatas.isEmpty()) {
//            TreeMap<Date, InnerData> uncheckedMap = new TreeMap<>(); // 未检查作业，按截止时间排序
//            TreeMap<Date, InnerData> uncheckedCreateMap = new TreeMap<>(); // 未检查作业，按布置时间排序
//            TreeMap<Date, InnerData> checkedCreateMap = new TreeMap<>(); // 已检查作业，按布置时间排序
//            for (InnerData innerData : innerDatas) {
//                if (innerData.getChecked()) {
//                    checkedCreateMap.put(innerData.getCreateDatetime(), innerData);
//                } else {
//                    uncheckedMap.put(innerData.getEndDatetime(), innerData);
//                    uncheckedCreateMap.put(innerData.getCreateDatetime(), innerData);
//                }
//            }
//
//            int count = 0;
//            if (uncheckedMap.size() > 0) {
//                // 如果有则取最后一个未检查作业的布置时间作为“作业布置时间”，
//                // 取结束时间最晚的作业的结束时间作业“作业检查时间”，
//                // 取所有未检查作业里完成人数最多的作为“完成人数”
//                mapper.setLastCheckHomeworkDays(buildUncheckDays(DateUtils.dayDiff(new Date(), uncheckedMap.lastEntry().getValue().getEndDatetime())));
//                mapper.setLastHomeworkDays(buildHomeworkDays(DateUtils.dayDiff(new Date(), uncheckedCreateMap.lastEntry().getValue().getCreateDatetime())));
//                for (HomeworkLocation location : uncheckedHomeworkIds) {
//                    Homework homework = ALL_HOMEWORKS.get(location.getHomeworkId());
//                    Accomplishment accomplishment = accomplishmentLoaderClient.loadAccomplishment(homework);
//                    int fcount = accomplishment == null ? 0 : accomplishment.size();
//                    if (fcount > count) {
//                        count = fcount;
//                    }
//                }
//                for (Long quizId : uncheckedQuizIds) {
//                    Quiz quiz = ALL_QUIZS.get(quizId);
//                    Accomplishment accomplishment = accomplishmentLoaderClient.loadAccomplishment(quiz);
//                    int fcount = accomplishment == null ? 0 : accomplishment.size();
//                    if (fcount > count) {
//                        count = fcount;
//                    }
//                }
//                mapper.setMaxHomeworkFinishCount(buildFinishCount(count));
//                mapper.setFinishLighted(count >= 8);
//                if (DateUtils.minuteDiff(new Date(), uncheckedMap.lastEntry().getValue().getEndDatetime()) > 0) {
//                    // 作业已到期但还未检查
//                    mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师检查作业");
//                } else if (count < 8) {
//                    mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师鼓励更多学生完成作业");
//                }
//            } else {
//                // 所有作业都已被检查，“检查时间”显示为“已检查”，“作业布置时间”取最后布置的一次作业的布置时间，“完成人数”取所有已检查作业里完成人数最多的
//                mapper.setLastCheckHomeworkDays("已检查");
//                mapper.setLastHomeworkDays(buildHomeworkDays(DateUtils.dayDiff(new Date(), checkedCreateMap.lastEntry().getValue().getCreateDatetime())));
//                for (HomeworkLocation location : checkedHomeworkIds) {
//                    Homework homework = ALL_HOMEWORKS.get(location.getHomeworkId());
//                    Accomplishment accomplishment = accomplishmentLoaderClient.loadAccomplishment(homework);
//                    int fcount = 0;
//                    if (accomplishment != null && accomplishment.getDetails() != null) {
//                        for (Accomplishment.Detail detail : accomplishment.getDetails().values()) {
//                            if (!detail.isRepairTrue()) {
//                                fcount++;
//                            }
//                        }
//                    }
//                    if (fcount > count) {
//                        count = fcount;
//                    }
//                }
//                for (Long quizId : checkedQuizIds) {
//                    Quiz quiz = ALL_QUIZS.get(quizId);
//                    Accomplishment accomplishment = accomplishmentLoaderClient.loadAccomplishment(quiz);
//                    int fcount = accomplishment == null ? 0 : accomplishment.size();
//                    if (fcount > count) {
//                        count = fcount;
//                    }
//                }
//                mapper.setMaxHomeworkFinishCount(buildFinishCount(count));
//                mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "布置新作业并鼓励更多学生完成");
//            }
//        } else if (mapper.getSuggestion() == null) {
//            mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师布置作业");
//        }
//        return mapper;
//    }

    private ActivateInfoMapper teacherActivateConditionCheck(Teacher teacher, TeacherActivateTeacherHistory history, Subject subject) {
        // step-1：首先查找此次激活创建时间以后的目标老师的所有班级的所有作业和测验
        Set<Long> clazzIds = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(history.getInviteeId()).stream()
                .filter(t -> !t.isTerminalClazz())
                .map(Clazz::getId)
                .filter(t -> t != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        Set<Long> groupIds = groups.values().stream().map(GroupMapper::getId).collect(Collectors.toSet());

        // 新作业
        Map<Long, List<NewHomework.Location>> newHomeworks = newHomeworkLoaderClient
                .loadNewHomeworksByClazzGroupIds(groupIds, subject);
        Map<String, NewHomework.Location> ALL_NEWHOMEWORKS = new HashMap<>();
        for (List<NewHomework.Location> locations : newHomeworks.values()) {
            for (NewHomework.Location location : locations) {
                ALL_NEWHOMEWORKS.put(location.getId(), location);
            }
        }

        // step-2：选取激活以后布置的作业和测验
        List<InnerData> innerDatas = new ArrayList<>();
        List<String> checkedNewHomeworkIds = new ArrayList<>();
        List<String> uncheckedNewHomeworkIds = new ArrayList<>();

        ALL_NEWHOMEWORKS.values().stream().filter(location -> new Date(location.getCreateTime())
                .after(history.getCreateTime()))
                .forEach(location -> {
                    innerDatas.add(fromNewHomework(location));
                    if (location.isChecked()) {
                        checkedNewHomeworkIds.add(location.getId());
                    } else {
                        uncheckedNewHomeworkIds.add(location.getId());
                    }
                });

        // step-3：初始化ActivateInfoMapper
        ActivateInfoMapper mapper = build(teacher, history.getActivationType(), history.getId());
//        Date lastLoginTime = userLoaderClient.findUserLastLoginTime(teacher);
        Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacher.getId());
        if (null != lastLoginTime && lastLoginTime.after(history.getCreateTime())) { // 有些历史数据这个字段没数据
            mapper.setLastLoginDays(buildLoginDays(DateUtils.dayDiff(new Date(), lastLoginTime)));
        }
        if (mapper.getLastLoginDays() == null) {
            mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师登录网站并布置作业");
        }
        // step-4：如果老师被唤醒后布置过作业或者测验，先处理未检查的
        if (!innerDatas.isEmpty()) {
            TreeMap<Date, InnerData> uncheckedMap = new TreeMap<>(); //未检查作业，按截止时间排序
            TreeMap<Date, InnerData> uncheckedCreateMap = new TreeMap<>(); //未检查作业，按布置时间排序
            TreeMap<Date, InnerData> checkedCreateMap = new TreeMap<>(); //已检查作业，按布置时间排序
            for (InnerData innerData : innerDatas) {
                if (innerData.getChecked()) {
                    checkedCreateMap.put(innerData.getCreateDatetime(), innerData);
                } else {
                    uncheckedMap.put(innerData.getEndDatetime(), innerData);
                    uncheckedCreateMap.put(innerData.getCreateDatetime(), innerData);
                }
            }
            int count = 0;
            if (uncheckedMap.size() > 0) {
                // 如果有则取最后一个未检查作业的布置时间作为“作业布置时间”，
                // 取结束时间最晚的作业的结束时间作业“作业检查时间”，
                // 取所有未检查作业里完成人数最多的作为“完成人数”
                mapper.setLastCheckHomeworkDays(buildUncheckDays(DateUtils.dayDiff(new Date(), uncheckedMap.lastEntry().getValue().getEndDatetime())));
                mapper.setLastHomeworkDays(buildHomeworkDays(DateUtils.dayDiff(new Date(), uncheckedCreateMap.lastEntry().getValue().getCreateDatetime())));
                for (String newHomeworkId : uncheckedNewHomeworkIds) {
                    NewHomework.Location location = ALL_NEWHOMEWORKS.get(newHomeworkId);
                    NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
                    int fcount = accomplishment == null ? 0 : accomplishment.size();
                    if (fcount > count) count = fcount;
                }
                mapper.setFinishLighted(count >= 8);
                mapper.setMaxHomeworkFinishCount(buildFinishCount(count));
                if (DateUtils.minuteDiff(new Date(), uncheckedMap.lastEntry().getValue().getEndDatetime()) > 0) {
                    // 作业已到期但还未检查
                    mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师检查作业");
                } else if (count < 8) {
                    mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师鼓励更多学生完成作业");
                }
            } else {
                // 所有作业都已被检查，“检查时间”显示为“已检查”，
                // “作业布置时间”取最后布置的一次作业的布置时间，
                // “完成人数”取所有已检查作业里完成人数最多的
                mapper.setLastCheckHomeworkDays("已检查");
                mapper.setLastHomeworkDays(buildHomeworkDays(DateUtils.dayDiff(new Date(), checkedCreateMap.lastEntry().getValue().getCreateDatetime())));
                for (String newHomeworkId : checkedNewHomeworkIds) {
                    NewHomework.Location location = ALL_NEWHOMEWORKS.get(newHomeworkId);
                    NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
                    int fcount = accomplishment == null ? 0 : accomplishment.size();
                    if (fcount > count) count = fcount;
                }
                mapper.setMaxHomeworkFinishCount(buildFinishCount(count));
                mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "布置新作业并鼓励更多学生完成");
            }
        } else if (mapper.getSuggestion() == null) {
            mapper.setSuggestion("请" + StringUtils.defaultString(teacher.getProfile().getRealname()) + "老师布置作业");
        }
        return mapper;
    }

    private ActivateInfoMapper build(Teacher teacher, ActivationType type, String historyId) {
        ActivateInfoMapper mapper = new ActivateInfoMapper();
        mapper.setHistoryId(historyId);
        mapper.setUserId(teacher.getId());
        mapper.setUserName(StringUtils.defaultString(teacher.getProfile().getRealname()));
        mapper.setUserAvatar(teacher.fetchImageUrl());
        mapper.setType(type);
        mapper.setSubject(teacher.getSubject());
        if (type == SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE || type == TEACHER_ACTIVATE_TEACHER_LEVEL_ONE) {
            mapper.setActivateIntegral("50");
        } else if (type == SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO || type == TEACHER_ACTIVATE_TEACHER_LEVEL_TWO) {
            mapper.setActivateIntegral("100");
        } else if (type == SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_THREE || type == TEACHER_ACTIVATE_TEACHER_LEVEL_THREE) {
            mapper.setActivateIntegral("150");
        }
        return mapper;
    }

    private String buildFinishCount(int count) {
        if (count == 0) {
            return "未做";
        } else if (count < 8) {
            return String.valueOf(count) + "人(差" + (8 - count) + "人)";
        } else {
            return String.valueOf(count) + "人";
        }
    }

    private String buildUncheckDays(long days) {
        if (days > 0) {
            return "待检查";
        } else if (days < 0) {
            return String.valueOf(0 - days) + "天后";
        } else {
            return "当天";
        }
    }

    private String buildLoginDays(long days) {
        if (days == 0) {
            return "当天";
        } else {
            return String.valueOf(days) + "天前";
        }
    }

    private String buildHomeworkDays(long days) {
        if (days > 0) {
            return String.valueOf(days) + "天前";
        } else {
            return "当天";
        }
    }

//    private InnerData fromHomework(Homework homework) {
//        InnerData data = new InnerData();
//        data.setId(homework.getId());
//        data.setHomeworkType(HomeworkType.ENGLISH);
//        data.setChecked(homework.isHomeworkChecked());
//        if (homework.isHomeworkChecked()) {
//            data.setCheckDatetime(homework.getCheckedTime());
//        }
//        data.setCreateDatetime(homework.getCreateTime());
//        data.setEndDatetime(homework.getEndDate());
//        return data;
//    }
//
//    private InnerData fromQuiz(Quiz quiz, HomeworkType homeworkType) {
//        InnerData data = new InnerData();
//        data.setId(quiz.getId());
//        data.setHomeworkType(homeworkType);
//        data.setChecked(quiz.isChecked());
//        if (quiz.isChecked()) {
//            data.setCheckDatetime(quiz.getCheckedTime());
//        }
//        data.setCreateDatetime(quiz.getCreateDatetime());
//        data.setEndDatetime(quiz.getEndDateTime());
//        return data;
//    }

    private InnerData fromNewHomework(NewHomework.Location location) {
        InnerData data = new InnerData();
        data.setId(location.getId());
        data.setHomeworkType(HomeworkType.of(location.getSubject().name()));
        data.setChecked(location.isChecked());
        if (location.isChecked()) data.setCheckDatetime(new Date(location.getCheckedTime()));
        data.setCreateDatetime(new Date(location.getCreateTime()));
        data.setEndDatetime(new Date(location.getEndTime()));
        return data;
    }

    @Data
    private static class InnerData {
        Serializable id;
        Boolean checked;
        Date checkDatetime;
        Date createDatetime;
        Date endDatetime;
        HomeworkType homeworkType;
    }
}

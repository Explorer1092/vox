/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordSoundShareMapper;
import com.voxlearning.utopia.service.zone.client.ClazzJournalLoaderClient;
import com.voxlearning.utopia.service.zone.client.ClazzRecordServiceClient;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级空间管理
 */
@Controller
@RequestMapping("/opmanager/clazzrecord")
public class ClazzRecordManageController extends OpManagerAbstractController {

    @Inject private ClazzRecordServiceClient clazzRecordServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;
    @Inject private ActionLoaderClient actionLoaderClient;

    private static Map<Integer, String> typeMap = MapUtils.map(
            ClazzJournalType.BIRTHDAY.getId(), ClazzJournalType.BIRTHDAY.getDescription(),
            ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE.getId(), ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE.getDescription(),
            ClazzJournalType.HOMEWORK_HEADLINE.getId(), ClazzJournalType.HOMEWORK_HEADLINE.getDescription(),
            ClazzJournalType.ACHIEVEMENT_SHARE_HEADLINE.getId(), ClazzJournalType.ACHIEVEMENT_SHARE_HEADLINE.getDescription(),
            ClazzJournalType.GROWN_WORD_NEW_PET.getId(), ClazzJournalType.GROWN_WORD_NEW_PET.getDescription(),
            ClazzJournalType.GROWN_WORD_PET_LEVEL_UP.getId(), ClazzJournalType.GROWN_WORD_PET_LEVEL_UP.getDescription(),
            ClazzJournalType.CLASS_BOSS_CHALLENGE_RANK.getId(), ClazzJournalType.CLASS_BOSS_CHALLENGE_RANK.getDescription(),
            ClazzJournalType.COMPETITION_ISLAND_LEVEL_UP.getId(), ClazzJournalType.COMPETITION_ISLAND_LEVEL_UP.getDescription(),
            ClazzJournalType.COMPETITION_ISLAND_SEASON_CLASS_TOP3.getId(), ClazzJournalType.COMPETITION_ISLAND_SEASON_CLASS_TOP3.getDescription(),
            ClazzJournalType.WONDERLAND_NEW_MEDAL.getId(), ClazzJournalType.WONDERLAND_NEW_MEDAL.getDescription(),
            ClazzJournalType.WONDERLAND_MEDAL_GRADE.getId(), ClazzJournalType.WONDERLAND_MEDAL_GRADE.getDescription(),
            ClazzJournalType.NORMAL_CLASS_COMPETITION_INVITE_MATE.getId(), ClazzJournalType.NORMAL_CLASS_COMPETITION_INVITE_MATE.getDescription(),
            ClazzJournalType.RECESSIVE_CLASS_COMPETITION_INVITE_MATE.getId(), ClazzJournalType.RECESSIVE_CLASS_COMPETITION_INVITE_MATE.getDescription()
    );

    // 一个简易的入口
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String recordIndex(Model model) {
        return "opmanager/clazzrecord/index";
    }

    /**
     * 查看某个班级学生的录音分享记录
     */
    @RequestMapping(value = "clazzrecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage checkClazzSoundRecord() {
        Long clazzId = getRequestLong("clazzId");
        if (clazzId <= 0L) {
            return MapMessage.errorMessage("无效的班级ID");
        }
        try {
            List<RecordSoundShareMapper> recordList = clazzRecordServiceClient
                    .getClazzRecordService()
                    .$directlyLoadFromCache(clazzId, null);
            return MapMessage.successMessage().add("recordList", mapRecordList(recordList));
        } catch (Exception ex) {
            return MapMessage.errorMessage("操作失败：" + ex.getMessage());
        }
    }


    /**
     * 查看某个学生的录音分享记录
     */
    @RequestMapping(value = "childrecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage checkStudentSoundRecord() {
        Long studentId = getRequestLong("studentId");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.getClazzId() == null) {
            return MapMessage.errorMessage("无效的学生ID");
        }
        try {
            List<RecordSoundShareMapper> recordList = clazzRecordServiceClient
                    .getClazzRecordService()
                    .$directlyLoadFromCache(studentDetail.getClazzId(), studentId);
            return MapMessage.successMessage().add("recordList", mapRecordList(recordList));
        } catch (Exception ex) {
            return MapMessage.errorMessage("操作失败：" + ex.getMessage());
        }
    }

    /**
     * 清除某个学生的录音分享记录
     */
    @RequestMapping(value = "naughtychild.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clearNaughtyChildSoundRecord() {
        Long studentId = getRequestLong("studentId");
        String uri = getRequestString("uri");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.getClazzId() == null) {
            return MapMessage.errorMessage("无效的学生ID");
        }
        try {
            // 清理之前
            List<RecordSoundShareMapper> before = clazzRecordServiceClient
                    .getClazzRecordService()
                    .$directlyLoadFromCache(studentDetail.getClazzId(), studentId);
            // 清理
            clazzRecordServiceClient.getClazzRecordService().$clearNaughtyRecord(
                    studentDetail.getClazzId(), studentId, uri
            );
            // 清理之后
            List<RecordSoundShareMapper> after = clazzRecordServiceClient
                    .getClazzRecordService()
                    .$directlyLoadFromCache(studentDetail.getClazzId(), studentId);
            return MapMessage.successMessage().add("beforeModify", before).add("afterModify", after);
        } catch (Exception ex) {
            return MapMessage.errorMessage("操作失败：" + ex.getMessage());
        }
    }

    /**
     * 查看某个学生 以及其 同学的 自学产品使用记录
     */
    @RequestMapping(value = "learningzone.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage queryLearningZoneRecord() {
        Long studentId = getRequestLong("studentId");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.getClazzId() == null) {
            return MapMessage.errorMessage("无效的学生ID");
        }
        try {
            // FIXME 这个功能不用了
            return MapMessage.successMessage().add("result", warpResult());
        } catch (Exception ex) {
            return MapMessage.errorMessage("操作失败：" + ex.getMessage());
        }
    }

    /**
     * 查看某个学生 相关的班级新鲜事
     */
    @RequestMapping(value = "headline.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage queryHeadline() {
        Long studentId = getRequestLong("studentId");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.getClazzId() == null) {
            return MapMessage.errorMessage("无效的学生ID");
        }
        try {
            // 获取班级动态信息
            Set<ClazzJournal.ComplexID> complexIDs = clazzJournalLoaderClient.getClazzJournalLoader().__queryByClazzId(studentDetail.getClazzId());
            Set<Long> ids = complexIDs.stream()
                    .filter(p -> typeMap.containsKey(p.getType()))
                    .map(ClazzJournal.ComplexID::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(ids)) {
                return MapMessage.successMessage().add("headlineList", Collections.emptyList());
            }

            Map<Long, ClazzJournal> clazzJournalMap = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournals(ids);
            if (MapUtils.isEmpty(clazzJournalMap)) {
                return MapMessage.successMessage().add("headlineList", Collections.emptyList());
            }

            //所有的班级动态
            List<Map<String, Object>> clazzJournals = clazzJournalMap.values()
                    .stream()
                    .filter(cj -> Objects.equals(studentId, cj.getRelevantUserId()))
                    .filter(cj -> Instant.now().minusSeconds(7 * 24 * 60 * 60).isBefore(Instant.ofEpochMilli(cj.getCreateDatetime().getTime())))
                    .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
                    .limit(10)
                    .map(cj -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", cj.getId());
                        info.put("userId", studentId);
                        info.put("userName", studentDetail.fetchRealname());
                        info.put("type", typeMap.get(cj.getJournalType().getId()));
                        info.put("createTime", DateUtils.dateToString(cj.getCreateDatetime()));

                        UserRecordEcho echo = userLikeServiceClient.loadCommentRecord(UserLikeType.CLAZZ_JOURNAL, cj.getId().toString());
                        if (echo == null) {
                            info.put("el", Collections.emptyList());
                            info.put("cl", Collections.emptyList());
                            return info;
                        }

                        if (CollectionUtils.isEmpty(echo.getCommentList())) {
                            info.put("cl", Collections.emptyList());
                        } else {
                            List<Map<String, String>> cl = echo.getCommentList().stream()
                                    .map(c -> {
                                        Map<String, String> ci = new LinkedHashMap<>();
                                        ci.put("userId", c.getUserId().toString());
                                        ci.put("userName", c.getUserName());
                                        ci.put("comment", c.getComment());
                                        ci.put("ct", DateUtils.dateToString(c.getCreateTime()));
                                        return ci;
                                    }).collect(Collectors.toList());
                            info.put("cl", cl);
                        }
                        return info;
                    })
                    .collect(Collectors.toList());
            return MapMessage.successMessage().add("headlineList", clazzJournals);
        } catch (Exception ex) {
            return MapMessage.errorMessage("操作失败：" + ex.getMessage());
        }
    }

    /**
     * 撤回某个学生 相关的班级新鲜事的评论或点赞
     */
    @RequestMapping(value = "recallheadline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recallHeadline() {
        Long journalId = getRequestLong("journalId");
        Long userId = getRequestLong("userId");
        String comment = getRequestString("comment");
        String type = getRequestString("type");

        if (StringUtils.equals("com", type)) {
            return userLikeServiceClient.getRemoteReference()
                    .recallCommentClazzJournal(journalId, userId, comment);
        }

        return MapMessage.errorMessage("类型错了");
    }

//    /**
//     * 查看某个学生 相关的点赞类型
//     */
//    @RequestMapping(value = "likedata.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    @ResponseBody
//    public MapMessage userLikeData() {
//        Long studentId = getRequestLong("studentId");
//        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
//        if (studentDetail == null || studentDetail.getClazzId() == null) {
//            return MapMessage.errorMessage("无效的学生ID");
//        }
//        Long clazzId = studentDetail.getClazzId();
//
//        UserRecordEcho attendanceRecord = null;
//        try {
//            // 签到
//            String curMonth = DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMM");
//            String attendanceRecordId = StringUtils.join(curMonth, "_", clazzId, "_", studentId);
//            attendanceRecord = userLikeServiceClient.loadLikeRecord(UserLikeType.ATTENDANCE_RANK, attendanceRecordId);
//        } catch (Exception ex) {
//            logger.warn("Failed load user attendance record in crm, user={}", studentId, ex);
//        }
//
//        List<List<String>> achievementRecords = new LinkedList<>();
//        try {
//            // 个人成就
//            List<UserAchievementRecord> uarList = actionLoaderClient.getRemoteReference().loadUserAchievementRecords(studentId);
//
//            for (UserAchievementRecord uar : uarList) {
//                Achievement achievement = AchievementBuilder.build(uar);
//                if (achievement != null && achievement.getType() != null) {
//                    for (int rank = 1; rank <= achievement.getRank(); ++rank) {
//                        String recordId = StringUtils.join(studentId, "_", achievement.getType().name(), "_", rank);
//                        UserRecordEcho achievementRecord = userLikeServiceClient.loadLikeRecord(UserLikeType.ACHIEVEMENT_RANK, recordId);
//                        achievementRecords.addAll(mapAchievement(achievementRecord));
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.warn("Failed load user achievements record in crm, user={}", studentId, ex);
//        }
//
//        // 班级动态
//        List<List<String>> clazzJournalRecords = new LinkedList<>();
//        try {
//            Set<ClazzJournal.ComplexID> complexIDs = clazzJournalLoaderClient.getClazzJournalLoader().__queryByUserId(studentId);
//            Set<Long> ids = complexIDs.stream()
//                    .filter(p -> typeMap.containsKey(p.getType()))
//                    .map(ClazzJournal.ComplexID::getId)
//                    .collect(Collectors.toSet());
//            Map<Long, ClazzJournal> clazzJournalMap = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournals(ids);
//            if (MapUtils.isNotEmpty(clazzJournalMap)) {
//                int time = 7 * 24 * 60 * 60;
//                clazzJournalMap.values()
//                        .stream()
//                        .filter(cj -> Instant.now().minusSeconds(time).isBefore(Instant.ofEpochMilli(cj.getCreateDatetime().getTime())))
//                        .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
//                        .map(cj -> userLikeServiceClient.loadLikeRecord(UserLikeType.CLAZZ_JOURNAL, cj.getId().toString()))
//                        .forEach(map -> clazzJournalRecords.addAll(mapClazzJournal(map)));
//            }
//        } catch (Exception ex) {
//            logger.warn("Failed load user journals record in crm, user={}", studentId, ex);
//        }
//
//        // 总赞
//        UserLikedSummary statistic = null;
//        try {
//            statistic = userLikeServiceClient.loadUserLikedSummary(studentId, new Date());
//        } catch (Exception ex) {
//            logger.warn("Failed load user like statistic in crm, user={}", studentId, ex);
//        }
//
//        return MapMessage.successMessage()
////                .add("attendance", mapAttendance(attendanceRecord))
//                .add("achievements", achievementRecords)
//                .add("statistic", mapLikeStatistic(statistic))
//                .add("journals", clazzJournalRecords);
//    }

    private List<Map<String, Object>> mapRecordList(List<RecordSoundShareMapper> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        Set<Long> studentIds = recordList.stream().map(RecordSoundShareMapper::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

        return recordList.stream().map(record -> {
            Map<String, Object> info = new HashMap<>();
            Long userId = record.getUserId();
            User user = userMap.get(userId);
            info.put("userId", record.getUserId());
            info.put("userName", user == null ? "--" : user.fetchRealname());
            info.put("type", ClazzRecordTypeEnum.safeParse(record.getRecordTypeEnumName()).getDesc());
            info.put("createTime", DateUtils.dateToString(new Date(record.getCreateTime())));
            info.put("uri", record.getUri());
            info.put("duration", record.getTime());
            return info;
        }).collect(Collectors.toList());
    }

    private List<String> warpResult() {
        List<String> viewList = new LinkedList<>();
        viewList.add("--");
        viewList.add("--");
        viewList.add("--");
        viewList.add("--");
        viewList.add("--");
        viewList.add("--");
        return viewList;
    }

    private Map<String, List<List<Object>>> mapLikeStatistic(UserLikedSummary statistic) {
        Map<String, List<List<Object>>> dateMap = new LinkedHashMap<>();
//        MonthRange current = MonthRange.current();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(current.getStartDate());
//        Date today = new Date();
//        do {
//            dateMap.put(DateUtils.dateToString(cal.getTime(), "MM月dd日"), new LinkedList<>());
//            cal.add(Calendar.DAY_OF_MONTH, 1);
//        } while (cal.getTime().before(today) && cal.getTime().before(current.getEndDate()));
//        if (statistic != null && CollectionUtils.isNotEmpty(statistic.getLikerList())) {
//            statistic.getLikerList()
//                    .stream()
//                    .sorted()
//                    .forEach(snapshot -> {
//                        String key = DateUtils.dateToString(snapshot.getCreateTime(), "MM月dd日");
//                        if (dateMap.containsKey(key)) {
//                            List<Object> data = new ArrayList<>();
//                            data.add(snapshot.getUserId());
//                            data.add(DateUtils.dateToString(snapshot.getCreateTime(), "HH:mm:ss"));
//                            data.add(JsonUtils.toJson(snapshot.getExtInfo()));
//                            dateMap.get(key).add(data);
//                        }
//                    });
//        }
        return dateMap;
    }

//    private List<List<String>> mapAttendance(UserRecordEcho attendance) {
//        if (attendance == null || CollectionUtils.isEmpty(attendance.getEncourageList())) {
//            return Collections.emptyList();
//        }
//        return attendance.getEncourageList()
//                .stream()
//                .map(snapshot -> Arrays.asList(
//                        String.valueOf(snapshot.getUserId()),
//                        DateUtils.dateToString(snapshot.getCreateTime()),
//                        JsonUtils.toJson(snapshot.getExtInfo())
//                )).collect(Collectors.toList());
//    }
//
//    private List<List<String>> mapAchievement(UserRecordEcho achievement) {
//        if (achievement == null || CollectionUtils.isEmpty(achievement.getEncourageList())) {
//            return Collections.emptyList();
//        }
//        return achievement.getEncourageList()
//                .stream()
//                .map(snapshot -> Arrays.asList(
//                        String.valueOf(snapshot.getUserId()),
//                        DateUtils.dateToString(snapshot.getCreateTime()),
//                        JsonUtils.toJson(snapshot.getExtInfo())
//                )).collect(Collectors.toList());
//    }
//
//    private List<List<String>> mapClazzJournal(UserRecordEcho clazzJournal) {
//        if (clazzJournal == null || CollectionUtils.isEmpty(clazzJournal.getEncourageList())) {
//            return Collections.emptyList();
//        }
//        return clazzJournal.getEncourageList()
//                .stream()
//                .map(snapshot -> Arrays.asList(
//                        String.valueOf(snapshot.getUserId()),
//                        DateUtils.dateToString(snapshot.getCreateTime()),
//                        JsonUtils.toJson(snapshot.getExtInfo())
//                )).collect(Collectors.toList());
//    }

}

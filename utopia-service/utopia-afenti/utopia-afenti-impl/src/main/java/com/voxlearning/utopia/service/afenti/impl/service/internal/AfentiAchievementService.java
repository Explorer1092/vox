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

package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementStatus;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLoginDetail;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiUserAchievementRecord;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.FootprintableSchoolController;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.utopia.service.afenti.api.constant.AchievementType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author peng.zhang.a
 * @since 16-7-24
 */
@Named
public class AfentiAchievementService extends UtopiaAfentiSpringBean {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject private FootprintableSchoolController footprintableSchoolController;

    public MapMessage receiveAchievement(StudentDetail student, Subject subject, AchievementType achievementType, Integer level) {
        AfentiUserAchievementRecord record = afentiUserAchievementRecordPersistence.find(student.getId(), subject)
                .stream()
                .filter(p -> p.getAchievementType() == achievementType)
                .filter(p -> Objects.equals(p.getLevel(), level))
                .findFirst()
                .orElse(null);
        if (record == null) {
            int currentNum = 0;
            if (achievementType == LOGIN) {
                currentNum = asyncAfentiCacheService.AfentiLoginCountCacheManager_fetchCurrentCount(student.getId(), subject)
                        .take();
            } else if (achievementType == INVITATION) {
                currentNum = (int) afentiInvitationRecordPersistence.findByUserIdAndSubject(student.getId(), subject).stream()
                        .filter(p -> SafeConverter.toBoolean(p.getAccepted(), false))
                        .count();
            } else if (achievementType == STUDY_POINT) {
                currentNum = afentiLearningPlanPushExamHistoryDao.queryKnowledgePoint(student.getId(), subject).size();
            }
            if (currentNum < achievementType.getCumulativeNum(level)) {
                return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode())
                        .set("message", "当前以获取值不足以领取");
            }
            return addAchievement(student.getId(), subject, achievementType, level, AchievementStatus.RECEIVED);
        } else if (record.getStatus() == AchievementStatus.NOT_RECEIVE) {
            afentiUserAchievementRecordPersistence.updateStatus(record.getId(), AchievementStatus.RECEIVED);
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode())
                    .set("message", "状态错误");
        }
    }

    public MapMessage learningStudyPointNotify(Long userId, Subject subject) {
        int studyPoint = afentiLearningPlanPushExamHistoryDao.queryKnowledgePoint(userId, subject).size();

        int beforeMaxLevel = findMaxLevel(userId, subject, STUDY_POINT);
        int nowMaxLevel = beforeMaxLevel;
        //生成新的成就
        while (existNewAchievement(nowMaxLevel, studyPoint, STUDY_POINT)) {
            MapMessage mapMessage = addAchievement(userId, subject, STUDY_POINT, nowMaxLevel + 1, AchievementStatus.NOT_RECEIVE);
            if (mapMessage.isSuccess()) {
                nowMaxLevel++;
            } else {
                break;
            }
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (beforeMaxLevel != nowMaxLevel) {
            asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                    .removeRecord(student, subject, beforeMaxLevel, STUDY_POINT);
            asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                    .addRecord(student, subject, nowMaxLevel, STUDY_POINT);
        }

        return MapMessage.successMessage();
    }

    @Deprecated
    public MapMessage inviteSuccessNotify(Long sendInvitationUserId, Subject subject) {
        if (sendInvitationUserId == 0 || subject == null) {
            return MapMessage.errorMessage("参数缺失");
        }
        int inviteNum = (int) afentiInvitationRecordPersistence.findByUserIdAndSubject(sendInvitationUserId, subject)
                .stream()
                .filter(p -> SafeConverter.toBoolean(p.getAccepted(), false))
                .count();
        int nowMaxLevel = findMaxLevel(sendInvitationUserId, subject, INVITATION);

        //生成新的成就
        while (existNewAchievement(nowMaxLevel, inviteNum, INVITATION)) {
            MapMessage mapMessage = addAchievement(sendInvitationUserId, subject, INVITATION, nowMaxLevel + 1, AchievementStatus.NOT_RECEIVE);
            if (mapMessage.isSuccess()) {
                nowMaxLevel++;
            } else {
                break;
            }
        }
        return MapMessage.successMessage();
    }

    public void loginNotify(Long userId, Subject subject, Date date) {
        int count;
        // 看看缓存是否存在，如果存在，就增加缓存，记录登陆数据，如果不存在，就尝试把老表中的数据迁移过来，估计一个月就够了
        if (asyncAfentiCacheService.AfentiLoginCountCacheManager_fetchCurrentCount(userId, subject).take() <= 0) {
            Set<String> dates = new HashSet<>();
            dates.add(DateUtils.dateToString(date, FORMAT_SQL_DATE));

            afentiLoginDetailDao.login(userId, subject, dates.toArray(new String[dates.size()]));
            asyncAfentiCacheService.AfentiLoginCountCacheManager_updateCurrentCount(userId, subject, dates.size())
                    .awaitUninterruptibly();
            count = dates.size();
        } else {
            AfentiLoginDetail detail = afentiLoginDetailDao.login(userId, subject, DateUtils.dateToString(date, FORMAT_SQL_DATE));
            asyncAfentiCacheService.AfentiLoginCountCacheManager_updateCurrentCount(userId, subject, detail.getDetails().size())
                    .awaitUninterruptibly();
            count = detail.getDetails().size();
        }

        int beforeMaxLevel = findMaxLevel(userId, subject, LOGIN);
        int nowMaxLevel = beforeMaxLevel;
        // 生成新的成就
        while (existNewAchievement(nowMaxLevel, count, LOGIN)) {
            MapMessage mapMessage = addAchievement(userId, subject, LOGIN, nowMaxLevel + 1, AchievementStatus.NOT_RECEIVE);
            if (mapMessage.isSuccess()) {
                nowMaxLevel++;
            } else {
                break;
            }
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (beforeMaxLevel != nowMaxLevel) {
            asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                    .removeRecord(student, subject, beforeMaxLevel, LOGIN);
            asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                    .addRecord(student, subject, nowMaxLevel, LOGIN);
        }
    }

    public MapMessage loadUserAchievements(StudentDetail student, Subject subject) {
        Map<AchievementType, List<AfentiUserAchievementRecord>> achievementTypeListMap = afentiUserAchievementRecordPersistence
                .find(student.getId(), subject)
                .stream()
                .collect(Collectors.groupingBy(AfentiUserAchievementRecord::getAchievementType, Collectors.toList()));

        //加载成就的时候初始化一下
        if (asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                .isRecord(student, subject)) {
            initUserMaxLevel(student, subject);
        }
        int loginNum = asyncAfentiCacheService.AfentiLoginCountCacheManager_fetchCurrentCount(student.getId(), subject)
                .take();
        int knowledgePointNum = afentiLearningPlanPushExamHistoryDao.queryKnowledgePoint(student.getId(), subject).size();

        int maxLoginLevel = findMaxLevel(student.getId(), subject, LOGIN);
        int maxSpLevel = findMaxLevel(student.getId(), subject, STUDY_POINT);
        List<Map<String, Object>> loginAchievements = fetchAchievements(achievementTypeListMap.getOrDefault(LOGIN, Collections.emptyList()), subject,
                loginNum, LOGIN, student, maxLoginLevel);
        List<Map<String, Object>> studyPointAchievements = fetchAchievements(achievementTypeListMap.getOrDefault(STUDY_POINT, Collections.emptyList()), subject,
                knowledgePointNum, STUDY_POINT, student, maxSpLevel);
        return MapMessage.successMessage()
                .set("loginAchievements", loginAchievements)
                .set("studyPointAchievements", studyPointAchievements);
    }

    public MapMessage fetchMaxLevelClassmates(StudentDetail student, Subject subject, AchievementType achievementType, Integer level) {
        int clazzLevel = student.getClazz().getClazzLevel().getLevel();
        Long schoolId = student.getClazz().getSchoolId();
        String jie = student.getClazz().getJie();
        Long clazzId = student.getClazzId();
        Set<Long> userIdList = asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                .fetchRecordByLevel(schoolId, clazzId, subject, jie, clazzLevel, level, achievementType);
        return MapMessage.successMessage().set("classmates", getClassmatesInfo(userIdList));
    }

    //*************************私有方法***********************/

    //初始化数据
    public void initUserMaxLevel(StudentDetail studentDetail, Subject subject) {
        Map<Integer, Set<Long>> loginLevelUserIds;
        Map<Integer, Set<Long>> studyPointLevelUserIds;
        Set<Long> classmateIds = getSameSchoolSameClazzLevelClassmateIds(studentDetail);
        //加载同班同学获得勋章的最大等级, 按照等级划分同班同学,如果用户同年级的学生大于2000个则不进行初始化
        if (classmateIds != null && classmateIds.size() >= 2000) {
            logger.warn("init user max level warn: classmates too much,userId={} classmateSize={} ", studentDetail.getId(), classmateIds.size());
            return;
        }

        Map<Long, Integer> loginMap = afentiUserAchievementRecordPersistence.findMaxLevelByUserIds(classmateIds, subject, LOGIN);
        loginLevelUserIds = groupUserIdByLevel(loginMap);
        asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager().addRecord(studentDetail, subject, LOGIN, loginLevelUserIds);

        //语文没有知识点属性
        if (subject != Subject.CHINESE) {
            Map<Long, Integer> studyPointMap = afentiUserAchievementRecordPersistence.findMaxLevelByUserIds(classmateIds, subject, STUDY_POINT);
            studyPointLevelUserIds = groupUserIdByLevel(studyPointMap);
            asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager().addRecord(studentDetail, subject, STUDY_POINT, studyPointLevelUserIds);

        }
    }

    private List<Map<String, Object>> fetchAchievements(List<AfentiUserAchievementRecord> achievements,
                                                        Subject subject,
                                                        Integer currentNum,
                                                        AchievementType achievementType,
                                                        StudentDetail student,
                                                        Integer achievedMaxLevel) {

        Map<Integer, AfentiUserAchievementRecord> recordMap = new HashMap<>();
        achievements.stream().filter(p -> p.getAchievementType() == achievementType).forEach(p -> recordMap.put(p.getLevel(), p));
        List<Map<String, Object>> result = new ArrayList<>();
        String desc = achievementType == LOGIN ? "累计{}天登陆"
                : (achievementType == INVITATION ? "成功邀请{}位同学使用" : "掌握{}个知识点");

        //部分用户的数据丢失，导致当前统计的currentNum比以前的少，所以取其中的最大值（已经获得勋章的累计值，当前统计值）
        currentNum = Math.max(currentNum, achievementType.getCumulativeNum(achievedMaxLevel));
        int clazzLevel = student.getClazz().getClazzLevel().getLevel();
        Long schoolId = student.getClazz().getSchoolId();
        String jie = student.getClazz().getJie();
        Long clazzId = student.getClazzId();

        for (int level = 1; level <= achievementType.levelNum; level++) {
            Map<String, Object> mid = new HashMap<>();

            AchievementStatus status;
            if (currentNum < achievementType.getCumulativeNum(level)) {
                status = AchievementStatus.NOT_ACHIEVED;
            } else if (recordMap.containsKey(level)) {
                status = recordMap.get(level).getStatus();
            } else {
                status = AchievementStatus.NOT_RECEIVE;
            }

            mid.put("userId", String.valueOf(student.getId()));
            mid.put("desc", StringUtils.formatMessage(desc, achievementType.getCumulativeNum(level)));
            mid.put("level", level);
            mid.put("currentCumulativeNum", currentNum);
            mid.put("upgradeCumulativeNum", achievementType.getCumulativeNum(level));
            mid.put("status", status);
            mid.put("achievementType", achievementType);

            //加载3个同校同年级同学获得此等级的信息
            List<Long> userIds = asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager()
                    .fetchRecordByLevel(schoolId, clazzId, subject, jie, clazzLevel, level, achievementType)
                    .stream().limit(3)
                    .collect(Collectors.toList());
            mid.put("achievedClassmates", getClassmatesInfo(userIds));
            result.add(mid);
        }
        return result;
    }

    // 获取同校同年级的同学列表,必须是认证的学校,包括自己在内
    private Set<Long> getSameSchoolSameClazzLevelClassmateIds(StudentDetail student) {
        if (null == student || null == student.getClazz()) return new HashSet<>();

        if (!footprintableSchoolController.isFootprintable(student.getClazz().getSchoolId())) {
            return new HashSet<>();
        }

        Clazz clazz = student.getClazz();

        // 获取同校班级
        Set<Long> clazzIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(clazz.getSchoolId())
                .originalLocationsAsList()
                .stream()
                .filter(t -> !t.isDisabled())
                .filter(t -> clazz.getClazzLevel() != null && t.getJie() == ClassJieHelper.fromClazzLevel(clazz.getClazzLevel()))
                .map(Clazz.Location::getId)
                .collect(Collectors.toSet());

        // 获取班级中的学生
        Map<Long, List<Long>> cid_sids_map = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzIds(clazzIds);
        Set<Long> studentIds = cid_sids_map.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        //将自己也添加进去
        studentIds.add(student.getId());
        return studentIds;
    }

    /**
     * 通过等级给用户分组
     *
     * @param maxLevelMap 　每个用户的等级
     */
    private Map<Integer, Set<Long>> groupUserIdByLevel(Map<Long, Integer> maxLevelMap) {
        return maxLevelMap.keySet()
                .stream()
                .collect(Collectors.groupingBy(p -> maxLevelMap.getOrDefault(p, 0), Collectors.toSet()));

    }

    /**
     * 获取用户信息
     */
    private List<Map<String, Object>> getClassmatesInfo(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyList();

        Map<Long, User> users = userLoaderClient.loadUsers(userIds);

        return userIds.stream().map(userId -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("userName", users.get(userId) == null ? "" : users.get(userId).fetchRealnameIfBlankId());
            map.put("imageUrl", users.get(userId) == null ? "" : users.get(userId).fetchImageUrl());
            return map;
        }).collect(Collectors.toList());
    }


    private boolean existNewAchievement(Integer nowMaxLevel, Integer currentNum, AchievementType achievementType) {
        return !achievementType.isMaxLevel(nowMaxLevel) && achievementType.getCumulativeNum(nowMaxLevel + 1) <= currentNum;
    }

    /**
     * 增加小红点与成就
     */
    private MapMessage addAchievement(Long userId, Subject subject, AchievementType achievementType, Integer newLevel, AchievementStatus achievementStatus) {

        AfentiUserAchievementRecord afentiUserAchievementRecord = AfentiUserAchievementRecord.newInstance(userId, achievementType, newLevel, subject, achievementStatus);
        try {
            afentiUserAchievementRecordPersistence.persist(afentiUserAchievementRecord);
            //增加成就小红点
            asyncAfentiCacheService.AfentiPromptCacheManager_record(userId, subject, AfentiPromptType.achievement)
                    .awaitUninterruptibly();
        } catch (Exception e) {
            logger.error("addAchievement Error:", e);
        }
        return MapMessage.successMessage();
    }

    private int findMaxLevel(Long userId, Subject subject, AchievementType achievementType) {
        return afentiUserAchievementRecordPersistence.find(userId, subject)
                .stream()
                .filter(p -> p.getAchievementType() == achievementType)
                .mapToInt(AfentiUserAchievementRecord::getLevel)
                .max()
                .orElse(0);
    }
}
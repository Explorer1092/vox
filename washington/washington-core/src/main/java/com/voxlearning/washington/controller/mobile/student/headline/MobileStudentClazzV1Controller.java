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

package com.voxlearning.washington.controller.mobile.student.headline;

import com.voxlearning.alps.api.monitor.ControllerMetric;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.ObjectUtils;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.client.ClazzJournalLoaderClient;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.student.headline.helper.*;
import com.voxlearning.washington.mapper.studentheadline.StudentAchievementHeadlineV1Mapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author qianxiaozhi
 * @since 3/1/2017
 */
@ControllerMetric
@Controller
@RequestMapping(value = "/studentMobile/clazz")
public class MobileStudentClazzV1Controller extends AbstractMobileController {

    @Inject private ActionServiceClient actionServiceClient;
    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Inject private MobileStudentClazzHelper mobileStudentClazzHelper;
    @Inject private HeadlineExecutorFactory headlineExecutorFactory;

    private static final Map<String, UserLikeType> likeTypeMap = MapUtils.map(
            ClazzJournalType.GROWN_WORD_NEW_PET.name(), UserLikeType.NEW_PET,
            ClazzJournalType.GROWN_WORD_PET_LEVEL_UP.name(), UserLikeType.PET_LEVEL_UP,
            ClazzJournalType.COMPETITION_ISLAND_SEASON_CLASS_TOP3.name(), UserLikeType.COMPETITION_SEASON_CLASS_TOP_3,
            ClazzJournalType.COMPETITION_ISLAND_LEVEL_UP.name(), UserLikeType.COMPETITION_LEVEL_UP,
            ClazzJournalType.WONDERLAND_NEW_MEDAL.name(), UserLikeType.NEW_MEDAL,
            ClazzJournalType.WONDERLAND_MEDAL_GRADE.name(), UserLikeType.MEDAL_LEVEL_UP,
            ClazzJournalType.CLASS_BOSS_CHALLENGE_RANK.name(), UserLikeType.CLASS_BOSS_TOP_3
    );

    /**
     * 查询头条信息 i.e. 班级新鲜事
     */
    @RequestMapping(value = "/newheadline.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage headline() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        boolean isInBlankList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(currentStudentDetail(), "ClassZoneNewheadline", "BlackList");
        if (isInBlankList) {
            return MapMessage.successMessage("无数据");
        }
        int pageSize = getRequestInt("pageSize", 10);  // 分页size
        long minId = getRequestLong("minId", -1L);    // minId 当前页面展示的最下面的记录 防止出现重复读
        int day = getRequestInt("day", 0);            // day   第几天的数据 当前时间（日）-day 预留字段

        //定义结果集
        try {
            // 获取班级动态信息
            List<ClazzJournal> clazzJournals = mobileStudentClazzHelper.getClazzJournals(clazz.getId());
            if (CollectionUtils.isEmpty(clazzJournals)) {
                return MapMessage.successMessage();
            }
            // 分页处理
            clazzJournals = clazzJournals.stream()
                    .filter(clazzJournal -> calPage(minId, clazzJournal))
                    .collect(Collectors.toList());
            List<StudentHeadlineMapper> mappers = new ArrayList<>();

            long currentMinId = minId;
            // 封装headlineContext
            HeadlineMapperContext context = HeadlineMapperContext.newInstance(currentUserId());

            for (ClazzJournal clazzJournal : clazzJournals) {
                StudentHeadlineMapper mapper = getMapper(clazzJournal, context);
                if (mapper == null) {
                    continue;
                }

                mappers.add(mapper);
                currentMinId = clazzJournal.getId();

                if (mappers.size() >= pageSize) {
                    break;
                }
            }

            // 把所有头条放一起再排个序,按时间先后倒序 ！！
            mappers = mappers.stream().sorted((m1, m2) -> m2.getDateTime().compareTo(m1.getDateTime())).collect(Collectors.toList());

            return MapMessage.successMessage().add("headlines", mappers).add("minId", currentMinId).add("day", day);

        } catch (Exception ex) {
            logger.error("Failed load student newheadlines, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 新鲜事动态评论
     */
    @RequestMapping(value = "/headline/comment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage headlineComment() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        Long clazzJournalId = getRequestLong("journalId");
        Long relevantUserId = getRequestLong("relevantUserId");
        if (clazzJournalId == 0L || relevantUserId == 0L) {
            return MapMessage.errorMessage("参数为空");
        }
        // 获取扩展信息
        Map<String, Object> extInfo = MapUtils.m(
                "comment", getRequestString("comment"),
                "message", getRequestString("message"),
                "icon", getRequestString("icon")
        );

        String comment = extInfo.get("comment").toString();
        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("请选择评论内容");
        }

        if (StringUtils.isBlank(extInfo.get("message").toString())) {
            return MapMessage.errorMessage("请选择评论内容");
        }

        Long studentId = currentUserId();
        User student = currentUser();

        try {
            UserRecordEcho echo = userLikeServiceClient.loadCommentRecord(UserLikeType.CLAZZ_JOURNAL, String.valueOf(clazzJournalId));

            if (echo != null && echo.alreadyComment(studentId)) {
                return MapMessage.errorMessage("只能评论三次哟");
            }

            UserRecordSnapshot snapshot = new UserRecordSnapshot(studentId, comment);
            snapshot.setUserName(student.fetchRealname());
            MapMessage resultMsg = userLikeServiceClient.commentClazzJournal(clazzJournalId, snapshot);
            if (resultMsg.isSuccess()) {
                actionServiceClient.headlineComment(currentUserId(), relevantUserId, extInfo);
            }
            return resultMsg;
        } catch (Exception ex) {
            logger.error("Failed comment student headline, clazzJournal={}, student={}", clazzJournalId, studentId, ex);
            return MapMessage.errorMessage("系统异常，请稍后重试");
        }
    }

    /**
     * 鼓励个人成就
     */
    @RequestMapping(value = "/headline/encourage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage headlineEncourage() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        Long clazzJournalId = getRequestLong("journalId");
        Long relevantUserId = getRequestLong("relevantUserId");
        String content = getRequestString("content");
        String type = getRequestString("type");
        if (clazzJournalId == 0L || relevantUserId == 0L) {
            return MapMessage.errorMessage("参数为空");
        }
        ClazzJournal clazzJournal = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournal(clazzJournalId);
        if (clazzJournal == null || clazzJournal.getJournalType() == null) {
            return MapMessage.errorMessage("这条新鲜事已经被删除了哦").add("errCode", "1032");
        }

        Map<String, Object> extInfo = MapUtils.m(
                "content", content,
                "icon", getRequestString("icon"),
                "journalId", clazzJournalId
        );

        User student = currentUser();

        String cacheKey = HeadlineCacheKeyGenerator.achievementEncouragerKey(clazzJournalId);
        String lockKey = cacheKey + "_lock";

        try {
            atomicLockManager.acquireLock(lockKey, 3);
        } catch (CannotAcquireLockException cae) {
            return MapMessage.errorMessage("鼓励该同学的人太多咯，再试下~");
        }

        try {
            RecordLikeInfo likeInfo = userLikeServiceClient.loadRecordLikeInfo(UserLikeType.CLAZZ_JOURNAL, SafeConverter.toString(clazzJournalId));
            if (likeInfo != null && likeInfo.hasLiked(currentUserId())) {
                return MapMessage.errorMessage("已经鼓励过该同学咯~");
            }
            actionServiceClient.likeHeadline(clazz.getId(), currentUserId(), relevantUserId, extInfo, clazzJournalId);
        } catch (Exception ex) {
            logger.error("Failed encourage student headline, student={}, relevantUserId={}", student.getId(), relevantUserId, ex);
            return MapMessage.errorMessage("系统异常");
        } finally {
            atomicLockManager.releaseLock(lockKey);
        }
        return MapMessage.successMessage();
    }

    /**
     * 更新单条新鲜事的状态
     */
    @RequestMapping(value = "/headline/refresh.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage refreshHeadline() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        Long journalId = getRequestLong("journalId");
        ClazzJournal clazzJournal = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournal(journalId);
        if (clazzJournal == null) {
            return MapMessage.errorMessage("新鲜事已经失效");
        }

        try {
            clearMapperCache(clazzJournal); // 先清除缓存
            StudentHeadlineMapper mapper = getMapper(clazzJournal, HeadlineMapperContext.newInstance(currentUserId()));
            if (mapper == null) {
                return MapMessage.errorMessage("新鲜事已经失效");
            }
            return MapMessage.successMessage().add("headline", mapper);
        } catch (Exception ex) {
            logger.error("Failed to refresh headline, student={}, journalId={}", currentUserId(), journalId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 删除单条新鲜事
     */
    @RequestMapping(value = "/headline/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteHeadline() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        Long journalId = getRequestLong("journalId");
        ClazzJournal clazzJournal = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournal(journalId);
        if (clazzJournal == null) {
            return MapMessage.successMessage();
        }

        if (!Objects.equals(currentUserId(), clazzJournal.getRelevantUserId())) {
            return MapMessage.errorMessage("不能删除不是自己发的新鲜事哟");
        }

        try {
            MapMessage response = clazzJournalServiceClient.delete(journalId, currentUserId());
            if (response.hasDuplicatedException()) {
                return MapMessage.errorMessage("请不要重复点击");
            }
            if (response.isSuccess()) {
                // 清除班级维度的缓存
                mobileStudentClazzHelper.clearClazzJournalsCache(clazz.getId());
                clearMapperCache(clazzJournal);
            }
            response.setInfo(response.isSuccess() ? "删除成功" : "删除失败");
            return response;
        } catch (Exception ex) {
            logger.error("Failed to delete headline, student={}, journalId={}", currentUserId(), journalId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 个人成就分享弹窗
     */
    @RequestMapping(value = "/achievement/popshare.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage popShareAchievement() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        Long currentUserId = currentUserId();
        User student = currentUser();
        try {

            //获取班级动态信息
            List<ClazzJournal> clazzJournals = mobileStudentClazzHelper.getClazzJournals(clazz.getId());
            if (CollectionUtils.isEmpty(clazzJournals)) {
                return MapMessage.successMessage();
            }
            Map<Long, Long> hiddenClazzJournalIds = mobileStudentClazzHelper.getHiddenAchievement(currentUserId);
            List<Map<String, Object>> bubbleAchievements = new LinkedList<>();
            List<Long> achievementGroup = new LinkedList<>();
            //学生成就弹窗分享处理 （个人成就未分享 关闭后都无法看到， 分享后班级同学可以看到）
            clazzJournals.stream()
                    .filter(clazzJournal -> clazzJournal.getJournalType() == ClazzJournalType.ACHIEVEMENT_HEADLINE)
                    // 本人的成就
                    .filter(clazzJournal -> currentUserId.equals(clazzJournal.getRelevantUserId()))
                    //1 关闭分享的成就 2 已经分享的成就 不在放入弹窗
                    .filter(clazzJournal -> MapUtils.isEmpty(hiddenClazzJournalIds) || !hiddenClazzJournalIds.containsKey(clazzJournal.getId()))
                    .forEach(clazzJournal -> {
                        //读取成就信息
                        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());
                        if (!extInfo.containsKey("achievementType")) return;
                        AchievementType achievementType = AchievementType.of(extInfo.get("achievementType").toString());
                        if (null == achievementType) return;
                        if (!extInfo.containsKey("level")) return;
                        //添加到冒泡成就
                        Map<String, Object> achievement = new HashMap<>();
                        achievement.put("CJId", clazzJournal.getId());
                        achievement.put("title", achievementType.getTitle());
                        achievement.put("name", achievementType.name());
                        achievement.put("level", extInfo.get("level"));
                        bubbleAchievements.add(achievement);
                        achievementGroup.add(clazzJournal.getId());
                    });

            if (CollectionUtils.isEmpty(bubbleAchievements)) {
                return MapMessage.successMessage();
            }

            // 新获取成就 满足弹窗
            StudentAchievementHeadlineV1Mapper achievementHeadlineMapper = new StudentAchievementHeadlineV1Mapper();
            achievementHeadlineMapper.setType(ClazzJournalType.ACHIEVEMENT_HEADLINE.name());
            achievementHeadlineMapper.setRelevantUserId(currentUserId);
            achievementHeadlineMapper.setHeadIcon(student.fetchImageUrl());
            achievementHeadlineMapper.setName(student.fetchRealname());
            achievementHeadlineMapper.setAchievements(bubbleAchievements);
            achievementHeadlineMapper.setVId(StudentAchievementHeadlineV1Mapper.genVid(achievementGroup));
            achievementHeadlineMapper.setTimestamp(System.currentTimeMillis());
            achievementHeadlineMapper.setDateTime(DateUtils.dateToString(new Date(), "MM-dd HH:mm"));
            achievementHeadlineMapper.setHeadWearImg(mobileStudentClazzHelper.getHeadWear(currentUserId)); // 头饰处理

            return MapMessage.successMessage().add("bubbleAchievement", achievementHeadlineMapper);
        } catch (Exception ex) {
            logger.error("Failed load user popshare achievement. user={}", currentUserId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 拒绝/关闭分享弹窗
     */
    @RequestMapping(value = "/achievement/refuseshare.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refuseShareAchievement() {
        try {
            if (studentUnLogin()) {
                return MapMessage.errorMessage("请重新登录");
            }
            String achievementJson = getRequestString("achievement");
            if (StringUtils.isBlank(achievementJson)) {
                return MapMessage.errorMessage("参数为空");
            }
            StudentAchievementHeadlineV1Mapper achievementHeadlineMapper = JsonUtils.fromJson(
                    achievementJson, StudentAchievementHeadlineV1Mapper.class
            );

            if (achievementHeadlineMapper == null) {
                return MapMessage.successMessage();
            }
            if (!mobileStudentClazzHelper.flushHiddenAchievement(achievementHeadlineMapper.getVId(), currentUserId())) {
                return MapMessage.errorMessage("操作异常");
            }

        } catch (CannotAcquireLockException cae) {
            return MapMessage.errorMessage("系统繁忙，请重试");
        } catch (Exception ex) {
            logger.error("Failed refuse share user  achievement. user={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
        return MapMessage.successMessage();
    }

    /**
     * 分享个人成就
     */
    @RequestMapping(value = "/achievement/share.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shareAchievement() {
        String achievementJson = getRequestString("achievement");
        if (StringUtils.isBlank(achievementJson)) {
            return MapMessage.errorMessage("参数为空");
        }
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }
        Long clazzId = clazz.getId();
        try {
            StudentAchievementHeadlineV1Mapper achievementHeadlineMapper = JsonUtils.fromJson(
                    achievementJson, StudentAchievementHeadlineV1Mapper.class
            );

            if (achievementHeadlineMapper == null) {
                return MapMessage.successMessage();
            }
            String vid = achievementHeadlineMapper.getVId();
            if (hasOp(vid)) {
                return MapMessage.successMessage();
            }
            try {
                if (!mobileStudentClazzHelper.flushHiddenAchievement(vid, currentUserId())) {
                    return MapMessage.errorMessage("操作异常");
                }
            } catch (CannotAcquireLockException cae) {
                return MapMessage.errorMessage("正在处理，不要着急~");
            }

            List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(currentUserId(), false);
            zoneQueueServiceClient.createClazzJournal(clazz.getId())
                    .withUser(currentUserId())
                    .withUser(currentUser().fetchUserType())
                    .withClazzJournalType(ClazzJournalType.ACHIEVEMENT_SHARE_HEADLINE)
                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION_STD)
                    .withJournalJson(JsonUtils.toJson(achievementHeadlineMapper))
                    .withGroup(groups.get(0).getId())
                    .commit();

            // 清空缓存
            mobileStudentClazzHelper.clearClazzJournalsCache(clazzId);

        } catch (Exception ex) {
            logger.error("Failed share user  achievement. user={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
        return MapMessage.successMessage();
    }

    /**
     * 送生日祝福
     */
    @RequestMapping(value = "/birthday/bless.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage happyBirthday() {
        Long relevantUserId = getRequestLong("relevantUserId");
        String vid = getRequestString("vid");

        if (StringUtils.isBlank(vid) || relevantUserId == 0L) {
            return MapMessage.errorMessage("参数为空");
        }
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        Clazz clazz = getCurrentClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        User student = currentUser();
        if (!ObjectUtils.notEqual(relevantUserId, currentUserId())) {
            return MapMessage.errorMessage("不能为自己祝福");
        }

        String cacheKey = HeadlineCacheKeyGenerator.birthdayBlessKey(vid);
        String lockKey = cacheKey + "_lock";

        try {
            atomicLockManager.acquireLock(lockKey, 3);
        } catch (CannotAcquireLockException cae) {
            return MapMessage.errorMessage("祝福该同学的人太多咯，再试下~");
        }

        try {
            RecordLikeInfo likeInfo = userLikeServiceClient.loadRecordLikeInfo(UserLikeType.BIRTHDAY_BLESS_HEADLINE, SafeConverter.toString(relevantUserId));
            if (likeInfo != null) {
                if (likeInfo.hasLiked(currentUserId())) {
                    return MapMessage.errorMessage("已祝福过该同学");
                }
            }

//            List<Map<String, Object>> blessedList = washingtonCacheSystem.CBS.flushable.load(cacheKey);
//
//            if (CollectionUtils.isEmpty(blessedList)) blessedList = new LinkedList<>();
//
//            boolean flag = blessedList.stream().anyMatch(bless -> Objects.equals(bless.get("id"), student.getId()));
//            if (flag) {
//                return MapMessage.errorMessage("已祝福过该同学");
//            }
//            //个人成就鼓励列表
//            Map<String, Object> blessed = new HashMap<>();
//            blessed.put("id", student.getId());
//            blessed.put("name", student.fetchRealname());
//            blessed.put("headIcon", student.fetchImageUrl());
//            blessed.put("headWearImg", mobileStudentClazzHelper.getHeadWear(currentUserId()));
//            blessed.put("time", System.currentTimeMillis());
//            blessedList.add(blessed);
//
//            ClazzJournal mock = new ClazzJournal();
//            mock.setId(SafeConverter.toLong(vid));
//            mock.setJournalType(ClazzJournalType.BIRTHDAY);
//            clearMapperCache(mock);
//            washingtonCacheSystem.CBS.flushable.set(cacheKey, HeadlineCacheKeyGenerator.CACHE_ONE_WEEK, blessedList);
//
            Map<String, Object> extInfo = MapUtils.m(
                    "icon", getRequestString("icon")
            );

            actionServiceClient.likeBirthday(clazz.getId(), currentUserId(), relevantUserId, extInfo);

        } catch (Exception ex) {
            logger.error("Failed bless student birthday, student={}, relevantUserId={}", student.getId(), relevantUserId, ex);
            return MapMessage.errorMessage("系统异常");
        } finally {
            atomicLockManager.releaseLock(lockKey);
        }
        return MapMessage.successMessage();
    }

    /**
     * 班级生日单
     */
    @RequestMapping(value = "/birthday/birthdayList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryBirthdayList() {
        try {
            if (studentUnLogin()) {
                return MapMessage.errorMessage("请重新登录");
            }
            Clazz clazz = getCurrentClazz();
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }
            User student = currentUser();

            //个人生日设置
            String myBirthday = "未填写";
            String vBirthday = DateUtils.dateToString(new Date(), "yyyy-MM-dd");
            if (StringUtils.isNotBlank(student.fetchBirthday())) {
                myBirthday = student.fetchBirthdayFormat("%s年%s月%s日");
                vBirthday = student.fetchBirthdayFormat("%s-%s-%s");
            }
            Map<String, Object> result = new HashMap<>();
            //个人生日 数据格式  【index：0 头像 index：1 姓名 index：2 生日（m月d日）】
            Map<String, Object> mine = new HashMap<>();
            mine.put("image", student.fetchImageUrl());
            mine.put("name", student.fetchRealname());
            mine.put("birthday", myBirthday);
            mine.put("value", vBirthday);
            mine.put("headWearImg", mobileStudentClazzHelper.getHeadWear(currentUserId()));
            result.put("myBirthday", mine);
            result.put("systemDate", DateUtils.dateToString(new Date(), "yyyy-MM-dd"));
            result.put("birthdayList", mobileStudentClazzHelper.queryClassmateBirthdayList(currentUserId(), clazz.getId()));
            return MapMessage.successMessage().add("result", result);
        } catch (Exception ex) {
            logger.error("Failed query user birthday, user={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 编辑生日
     */
    @RequestMapping(value = "/birthday/edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editBirthday() {
        try {
            if (studentUnLogin()) {
                return MapMessage.errorMessage("请重新登录");
            }
            // birthday yyyy-MM-dd
            String birthday = getRequestString("birthday");
            if (StringUtils.isBlank(birthday)) {
                return MapMessage.errorMessage("参数为空");
            }
            Pattern p = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
            Matcher m = p.matcher(birthday);
            if (!m.matches()) {
                return MapMessage.errorMessage("生日格式错误");
            }

            String[] dateFields = birthday.split("-");
            return userServiceClient.changeUserBirthday(currentUserId(), Integer.parseInt(dateFields[0]),
                    Integer.parseInt(dateFields[1]), Integer.parseInt(dateFields[2]));
        } catch (Exception ex) {
            logger.error("Failed edit student's birthday, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "comment_dictionary.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> headlineCommentDictionary() {
        try {
            String originDictionary = StringUtils.replaceAll(
                    getPageBlockContentGenerator().getPageBlockContentHtml("StudentClassCenter", "HeadlineComment"),
                    "\\s*", ""
            );

            if (StringUtils.isBlank(originDictionary)) {
                return Collections.emptyMap();
            }
            return JsonUtils.fromJson(originDictionary);
        } catch (Exception ex) {
            logger.error("Failed load headline comment dictionary.", ex);
            return Collections.emptyMap();
        }
    }


/* ============================ PRIVATE METHOD =================================*/

    private boolean calPage(Long minId, ClazzJournal clazzJournal) {
        return minId < 0 || (minId > 0 && clazzJournal.getId() < minId);
    }

    /**
     * 获取头条vo
     *
     * @param clazzJournal 动态信息
     */
    private StudentHeadlineMapper getMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        if (clazzJournal == null || clazzJournal.getJournalType() == null) {
            return null;
        }
        AbstractHeadlineExecutor headlineExecute = headlineExecutorFactory.getHeadlineExecute(clazzJournal.getJournalType());
        if (null == headlineExecute) {
            return null;
        }
        try {
            return headlineExecute.toMapper(clazzJournal, context);
        } catch (Exception ex) {
            logger.error("Failed parse student headline, clazzJournal id = {}, relevantUserId={}", clazzJournal.getId(), clazzJournal.getRelevantUserId(), ex);
            return null;
        }
    }

    private MapMessage clearMapperCache(ClazzJournal clazzJournal) {
        if (clazzJournal == null || clazzJournal.getJournalType() == null) {
            return MapMessage.errorMessage("参数错误");
        }
        AbstractHeadlineExecutor headlineExecute = headlineExecutorFactory.getHeadlineExecute(clazzJournal.getJournalType());
        if (null == headlineExecute) {
            return MapMessage.errorMessage("不支持的类型");
        }
        try {
            boolean flag = headlineExecute.clearMapper(clazzJournal);
            return new MapMessage().setSuccess(false).setInfo(flag ? "操作成功" : "操作失败");
        } catch (Exception ex) {
            logger.error("Failed clear student headline mapper cache, clazzJournal id = {}, relevantUserId={}", clazzJournal.getId(), clazzJournal.getRelevantUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 获取当前学生所在班级
     */
    private Clazz getCurrentClazz() {
        return currentStudentDetail() == null ? null : currentStudentDetail().getClazz();
    }

    /**
     * 是否已经分享 或者 关闭分享
     */
    private boolean hasOp(String vid) {
        Map<Long, Long> hiddenIds = mobileStudentClazzHelper.getHiddenAchievement(currentUserId());
        String[] ids = vid.split("_");
        for (String id : ids) {
            if (hiddenIds.containsKey(Long.parseLong(id))) {
                return true;
            }
        }
        return false;
    }

}

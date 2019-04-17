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

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolYearPhase;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.*;
import com.voxlearning.utopia.service.zone.client.AsyncClazzRecordServiceClient;
import com.voxlearning.utopia.service.zone.client.ClazzRecordServiceClient;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.student.headline.helper.MobileStudentClazzHelper;
import com.voxlearning.washington.mapper.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum.*;

/**
 * @author qianxiaozhi
 * @since 3/1/2017
 */
@Controller
@RequestMapping(value = "/studentMobile/clazz/record")
public class MobileStudentClazzRecordController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private AsyncClazzRecordServiceClient asyncClazzRecordServiceClient;
    @Inject private ClazzRecordServiceClient clazzRecordServiceClient;
    @Inject private MobileStudentClazzHelper mobileStudentClazzHelper;
    @Inject private UserLikeServiceClient userLikeServiceClient;

    /**
     * 判断学生是否在付费黑名单
     * 后续可以扩展其他检查
     * true -- 检查通过， false -- 检查不通过
     */
    @RequestMapping(value = "/validate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage validate() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        return MapMessage.successMessage().add("validate", !currentStudentDetail().isInPaymentBlackListRegion());
    }


    @RequestMapping(value = "/cards.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cards() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage("请加入班级");
        }

        Long studentId = currentUserId();
        Long clazzId = student.getClazzId();
        List<ClazzRecordCardMapper> cards = new LinkedList<>();

        // query all valid group homework
        Set<Long> groupIds = groupLoaderClient.loadStudentGroups(studentId, false)
                .stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Map<Long, List<NewHomework.Location>> allHws = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupIds);

        // find study master card homework and focus card homework
        List<NewHomework.Location> studyMasterHomework = new ArrayList<>();
        NewHomework.Location focusHomework = null;
        DateRange dateRange = SchoolYear.newInstance().currentTermDateRange();
        for (List<NewHomework.Location> groupHomework : allHws.values()) {
            NewHomework.Location hw = groupHomework.stream()
                    .filter(p -> p.getCreateTime() >= dateRange.getStartDate().getTime())
                    .sorted((o1, o2) -> (Long.compare(o2.getCreateTime(), o1.getCreateTime())))
                    .findFirst().orElse(null);
            if (hw == null) continue;
            studyMasterHomework.add(hw);
            if (focusHomework == null || focusHomework.getCreateTime() < hw.getCreateTime()) {
                focusHomework = hw;
            }
        }

        // generate study master cards
        AlpsFutureMap<NewHomework.Location, ClazzRecordCardMapper> smCards = new AlpsFutureMap<>();
        if (CollectionUtils.isNotEmpty(studyMasterHomework)) {
            smCards = AlpsFutureBuilder.<NewHomework.Location, ClazzRecordCardMapper>newBuilder()
                    .ids(studyMasterHomework)
                    .generator(homework -> asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadStudyMasterCard(studentId, homework))
                    .buildMap();
        }

        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(studentId, clazzId)
                .stream().map(User::getId).collect(Collectors.toList());

//        // 自学类的卡片有灰度控制
//        AlpsFuture<ClazzRecordCardMapper> staminaCard = new ValueWrapperFuture<>(null);
//        AlpsFuture<ClazzRecordCardMapper> crackerCard = new ValueWrapperFuture<>(null);
//        AlpsFuture<ClazzRecordCardMapper> competitionCard = new ValueWrapperFuture<>(null);

//        // 如果在灰度地区 而且 用户不是黑名单用户，才会加载
//        if (isLearningZoneGray() && !student.isInPaymentBlackListRegion()) {
//            // generate stamina card
//            staminaCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadStaminaCard(clazzId, classmates);
//
//            // generate cracker card
//            crackerCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadCrackerCard(clazzId, classmates);
//
//            //generate competition card
//            competitionCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadCompetitionCard(clazzId, classmates);
//        }

        // generate study focus card
        AlpsFuture<ClazzRecordCardMapper> focusCard = new ValueWrapperFuture<>(null);
        if (focusHomework != null) {
            focusCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadFocusCard(studentId, focusHomework);
        }

        // generate sharp card
//        AlpsFuture<ClazzRecordCardMapper> sharpCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadSharpCard(clazzId, classmates);

        // generate fashion card
        AlpsFuture<ClazzRecordCardMapper> fashionCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadFashionCard(clazzId, classmates);

        // generate fullMarks card
        AlpsFuture<ClazzRecordCardMapper> fullMarksCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadFullMarksCard(new ArrayList<>(groupIds), clazzId, classmates);

        // generate friendship card
        AlpsFuture<ClazzRecordCardMapper> friendshipCard = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadFriendshipCard(clazzId, classmates);

        cards.addAll(smCards.regularize().values());                                     // 学霸之星卡片
//        CollectionUtils.addNonNullElement(cards, staminaCard.getUninterruptibly());      // 毅力之星卡片
//        CollectionUtils.addNonNullElement(cards, crackerCard.getUninterruptibly());      // 闯关之星卡片
//        CollectionUtils.addNonNullElement(cards, competitionCard.getUninterruptibly());  // 竞技之星卡片
        CollectionUtils.addNonNullElement(cards, focusCard.getUninterruptibly());        // 专注之星卡片
//        CollectionUtils.addNonNullElement(cards, sharpCard.getUninterruptibly());        // 明察之星卡片
        CollectionUtils.addNonNullElement(cards, fashionCard.getUninterruptibly());      // 装扮之星卡片
        CollectionUtils.addNonNullElement(cards, fullMarksCard.getUninterruptibly());    // 满分之星卡片
        CollectionUtils.addNonNullElement(cards, friendshipCard.getUninterruptibly());   // 友爱之星卡片

        MapMessage result = MapMessage.successMessage().add("cards", cards);

        // find myself card
        cards.stream()
                .filter(p -> CollectionUtils.isNotEmpty(p.getHasGot()) && p.getHasGot().contains(student.getId()))
                .filter(p -> !isShared(p.getRecordTypeEnum().name(), p.getHomeworkId()))
                .findAny().ifPresent(myselfCard -> result.add("mineCard", myselfCard));
        return result.add("cdnDomain", CdnConfig.getAvatarDomain().getValue());
    }

    /**
     * 获取学生本学期的所有作业记录
     */
    @RequestMapping(value = "/smHomeworkRecords.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage smHomeworkRecords() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        //分页size
        final int pageSize = getRequestInt("pageSize", 4);
        final int page = getRequestInt("page", 1);
        Long groupId = getRequestLong("groupId");

        //定义结果集
        try {
            if (groupId <= 0L) {
                return MapMessage.errorMessage("班级信息错误");
            }

            List<NewHomework.Location> homework = clazzRecordServiceClient.loadHomeworkList(groupId);
            if (CollectionUtils.isEmpty(homework)) {
                return MapMessage.successMessage();
            }

            int total = homework.size();
            Pageable Page = Pageable.<NewHomework.Location, ClazzRecordHwMapper>newBuilder()
                    .build(page, pageSize, total, clazzRecordServiceClient.loadUserHomeworkRecords(currentUserId(), homework, page, pageSize));
            return MapMessage.successMessage()
                    .add("result", Page);
        } catch (Exception ex) {
            logger.error("Failed fetch student sm homework records, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 获取学生本季度的所有专注作业记录
     */
    @RequestMapping(value = "/focusHomeworkRecords.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage focusHomeworkRecords() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        // 分页size
        final int pageSize = getRequestInt("pageSize", 4);
        final int page = getRequestInt("page", 1);

        // 定义结果集
        try {
            // query all valid group homework
            Set<Long> groupIds = groupLoaderClient.loadStudentGroups(currentUserId(), false)
                    .stream()
                    .map(GroupMapper::getId).collect(Collectors.toSet());
            List<NewHomework.Location> homework = clazzRecordServiceClient.loadHomeworkList(groupIds);
            if (CollectionUtils.isEmpty(homework)) {
                return MapMessage.successMessage();
            }

            int total = homework.size();
            Pageable Page = Pageable.<NewHomework.Location, ClazzRecordHwMapper>newBuilder()
                    .build(page, pageSize, total, clazzRecordServiceClient.loadUserHomeworkRecords(currentUserId(), homework, page, pageSize));
            return MapMessage.successMessage()
                    .add("result", Page);
        } catch (Exception ex) {
            logger.error("Failed fetch student focus homework records, student={}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 记录语音分享
     */
    @RequestMapping(value = "/shareSoundRecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage shareSoundRecord() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        //分页
        String uri = getRequestString("uri");
        String recordName = getRequestString("recordName");
        String hwId = getRequestString("homeworkId");
        Long time = getRequestLong("time");

        if (StringUtils.isBlank(uri)) {
            return MapMessage.errorMessage("录音信息有误");
        }
        if (StringUtils.isBlank(recordName)) {
            return MapMessage.errorMessage("分享信息有误");
        }
        if (time <= 0) {
            return MapMessage.errorMessage("录音时长不足");
        }
        if (isShared(recordName, hwId)) {
            return MapMessage.errorMessage("已经分享过");
        }

        String lockKey = String.format("clazz:sound:share:clazzId:%s", currentStudentDetail().getClazzId());

        try {
            atomicLockManager.acquireLock(lockKey, 3);
        } catch (CannotAcquireLockException cae) {
            return MapMessage.errorMessage("分享失败，再试下~");
        }

        //定义结果集
        try {
            RecordSoundShareMapper soundShareMapper = new RecordSoundShareMapper();
            soundShareMapper.setUserId(currentUserId());
            soundShareMapper.setUri(uri);
            soundShareMapper.setRecordTypeEnumName(recordName);
            soundShareMapper.setClazzId(currentStudentDetail().getClazzId());
            soundShareMapper.setTime(time);
            soundShareMapper.setHwId(hwId);
            clazzRecordServiceClient.recordSoundShare(soundShareMapper);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed share student sound record, student={}, uri={}", currentUserId(), uri, ex);
            return MapMessage.errorMessage("系统异常");
        } finally {
            atomicLockManager.releaseLock(lockKey);
        }
    }

    /**
     * 获取语音分享记录
     */
    @RequestMapping(value = "/queryShareSoundRecord.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryShareSoundRecord() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage("请先加入班级");
        }

        // 定义结果集
        try {
            List<RecordSoundShareMapper> recordSoundShareMappers =
                    clazzRecordServiceClient.queryRecordSoundShare(student.getClazzId(), currentUserId());

            if (CollectionUtils.isEmpty(recordSoundShareMappers)) {
                return MapMessage.successMessage().add("records", Collections.emptyList());
            }

            List<Map> vos = new LinkedList<>();
            List<Long> userIds = recordSoundShareMappers.stream()
                    .map(RecordSoundShareMapper::getUserId)
                    .collect(Collectors.toList());

            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
            for (RecordSoundShareMapper mapper : recordSoundShareMappers) {
                if (mapper == null) continue;
                User user = userMap.get(mapper.getUserId());
                if (user == null) continue;
                Map<String, Object> vo = new HashMap<>();
                vo.put("image", user.fetchImageUrl());
                vo.put("headWear", mobileStudentClazzHelper.getHeadWear(mapper.getUserId()));
                vo.put("studentName", user.fetchRealname());
                vo.put("uri", mapper.getUri());
                vo.put("time", mapper.getTime());
                vo.put("self", Objects.equals(mapper.getUserId(), currentUserId()));
                ClazzRecordTypeEnum recordTypeEnum = ClazzRecordTypeEnum.safeParse(mapper.getRecordTypeEnumName());
                vo.put("recordName", recordTypeEnum == null ? "未知记录" : recordTypeEnum.getDesc());
                vos.add(vo);
            }
            return MapMessage.successMessage().add("records", vos);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 鼓励
     */
    @RequestMapping(value = "/likeRecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage likeRecord() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        String text = getRequestString("text");
        String recordName = getRequestString("recordName");
        String homeworkId = getRequestString("homeworkId");
        String recordId = getRequestString("recordId");

        if (StringUtils.isBlank(text)) {
            return MapMessage.errorMessage("鼓励语未获取");
        }
        if (StringUtils.isBlank(recordName)) {
            return MapMessage.errorMessage("鼓励信息有误");
        }

        if (ClazzRecordTypeEnum.safeParse(recordName) == null) {
            return MapMessage.errorMessage("鼓励记录未获取到");
        }
        boolean isHomeworkRecord = ClazzRecordTypeEnum.safeParse(recordName).isHomeworkRecord();
        String lockKey = isHomeworkRecord
                ? String.format("clazz:record:like:homeworkId:%s", homeworkId)
                : String.format("clazz:record:like:recordName:%s:clazzId:%s", recordName, currentStudentDetail().getClazzId());

        try {
            atomicLockManager.acquireLock(lockKey, 3);
        } catch (CannotAcquireLockException cae) {
            return MapMessage.errorMessage("鼓励失败，再试下~");
        }
        //定义结果集
        try {
            RecordLikeMapper recordLikeMapper = new RecordLikeMapper();
            recordLikeMapper.setUserId(currentUserId());
            recordLikeMapper.setText(text);
            recordLikeMapper.setHomeworkId(homeworkId);
            recordLikeMapper.setRecordTypeEnumName(recordName);
            recordLikeMapper.setClazzId(currentStudentDetail().getClazzId());
            clazzRecordServiceClient.like(recordLikeMapper);

            //生成点赞记录
            UserRecordSnapshot urss = new UserRecordSnapshot();
            urss.setUserId(currentUser().getId());
            urss.setUserName(currentUser().fetchRealname());
            urss.setComment(text);
            urss.setCreateTime(new Date());

            userLikeServiceClient.commentClazzRecord(recordId, urss);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed like student record, student={}, record={}, homework={} ", currentUserId(), recordName, homeworkId, ex);
        } finally {
            atomicLockManager.releaseLock(lockKey);
        }
        return MapMessage.errorMessage("系统异常");
    }

    /**
     * 获取鼓励记录
     */
    @RequestMapping(value = "/queryLikeRecord.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryLikeRecord() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        String recordName = getRequestString("recordName");
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(recordName)) {
            return MapMessage.errorMessage("鼓励信息有误");
        }

        //定义结果集
        try {
            RecordLikeMapper recordLikeMapper = new RecordLikeMapper();
            recordLikeMapper.setUserId(currentUserId());
            recordLikeMapper.setHomeworkId(homeworkId);
            recordLikeMapper.setRecordTypeEnumName(recordName);
            recordLikeMapper.setClazzId(currentStudentDetail().getClazzId());
            List<RecordLikeMapper> likeRecordMappers = clazzRecordServiceClient.queryRecordLike(recordLikeMapper);

            if (CollectionUtils.isEmpty(likeRecordMappers)) {
                return MapMessage.successMessage().add("records", Collections.emptyList());
            }

            List<Map<String, Object>> vos = new LinkedList<>();
            for (RecordLikeMapper mapper : likeRecordMappers) {
                if (mapper == null) continue;
                User user = raikouSystem.loadUser(mapper.getUserId());
                if (user == null) continue;
                Map<String, Object> vo = new HashMap<>();
                vo.put("image", user.fetchImageUrl());
                vo.put("headWear", mobileStudentClazzHelper.getHeadWear(mapper.getUserId()));
                vo.put("studentName", user.fetchRealname());
                vo.put("text", mapper.getText());
                vo.put("self", Objects.equals(mapper.getUserId(), currentUserId()));
                vo.put("time", DateUtils.dateToString(new Date(mapper.getCreateTime()), "MM-dd HH:mm"));
                vos.add(vo);
            }
            return MapMessage.successMessage().add("records", vos).add("liked", clazzRecordServiceClient.recordLiked(recordLikeMapper));
        } catch (Exception ex) {
            logger.error("Failed query student like record, student={}, record={}, homework={} ", currentUserId(), recordName, homeworkId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 获取学生信息
     */
    @RequestMapping(value = "/studentInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentInfo() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage("请先加入班级");
        }

        String ids = getRequestString("ids");
        if (StringUtils.isBlank(ids)) {
            return MapMessage.errorMessage("暂无同学信息");
        }
        try {
            List<User> classmates = mobileStudentClazzHelper.getCacheClassmates(student.getId(), student.getClazzId());
            if (CollectionUtils.isEmpty(classmates)) {
                return MapMessage.errorMessage("暂无同学信息");
            }
            List<ClazzRecordUserInfoMapper> mappers = new LinkedList<>();
            Map<Long, User> classmatesMap = classmates.stream().collect(Collectors.toMap(User::getId, Function.identity(),
                    (u, v) -> u, LinkedHashMap::new));
            String[] studentIds = ids.split(",");
            for (String studentId : studentIds) {
                if (StringUtils.isBlank(studentId) || !studentId.matches("\\d+")) continue;
                User user = classmatesMap.get(Long.parseLong(studentId));
                if (user == null) continue;

                ClazzRecordUserInfoMapper userInfoMapper = new ClazzRecordUserInfoMapper();
                userInfoMapper.setImage(user.fetchImageUrl());
                userInfoMapper.setHeadWear(mobileStudentClazzHelper.getHeadWear(user.getId()));
                userInfoMapper.setName(user.fetchRealname());
                mappers.add(userInfoMapper);
            }
            return MapMessage.successMessage().add("students", mappers);
        } catch (Exception ex) {
            logger.error("Failed query students' info, students={}", ids, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 学霸top3
     */
    @RequestMapping(value = "/smTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage smTop3() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        String hwId = getRequestString("homeworkId");
        Long groupId = getRequestLong("groupId", -1);

        if (StringUtils.isBlank(hwId)) {
            return MapMessage.errorMessage("暂无作业信息");
        }

        NewHomework newHomework = newHomeworkLoaderClient.load(hwId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业信息无效");
        }

        if (groupId <= 0) {
            return MapMessage.errorMessage("分组信息获取失败");
        }

        List<NewHomework.Location> hws = clazzRecordServiceClient.loadHomeworkList(groupId);

        ClazzRecordTypeEnum recordTypeEnum = ClazzRecordTypeEnum.getFromSubject(newHomework.getSubject());
        String recordId = StringUtils.join(recordTypeEnum.name(), "_", hwId);

        return MapMessage.successMessage()
                .add("result", clazzRecordServiceClient.queryTop3StudyMasterMapper(hwId))
                .add("homeworkInfos", queryHomeworkInfos(hws, hwId)).add("avgScore", clazzRecordServiceClient.getAvgScore(hwId))
                .add("recordId", recordId);
    }

    /**
     * 专注top3
     */
    @RequestMapping(value = "/focusTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage focusTop3() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        String hwId = getRequestString("homeworkId");
        if (StringUtils.isBlank(hwId)) {
            return MapMessage.errorMessage("暂无作业信息");
        }

        List<NewHomework.Location> hws = queryHwsByGroups();

        String recordId = StringUtils.join(FOCUS_STAR.name(), "_", hwId);

        return MapMessage.successMessage()
                .add("result", clazzRecordServiceClient.queryTop3FocusMapper(hwId))
                .add("homeworkInfos", queryHomeworkInfos(hws, hwId)).add("avgScore", clazzRecordServiceClient.getAvgScore(hwId))
                .add("recordId", recordId);
    }

    /**
     * 明察top3
     */
    @RequestMapping(value = "/sharpTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sharpTop3() {
//        if (studentUnLogin()) {
//            return MapMessage.errorMessage("请重新登录");
//        }
//        StudentDetail studentDetail = currentStudentDetail();
//        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(currentUserId(), studentDetail.getClazzId())
//                .stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//        Map<String, Object> sharpMapper = clazzRecordServiceClient.queryTop3AndWeekSharpMapper(studentDetail.getClazzId(), classmates);
//        return MapMessage.successMessage().add("result", sharpMapper.get("top3"))
//                .add("weekTop", sharpMapper.get("weekTop"))
//                .add("weekDate", parseWeek());
        return MapMessage.errorMessage("大爆料已经下线");
    }

    /**
     * 装扮top3
     */
    @RequestMapping(value = "/fashionTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fashionTop3() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        StudentDetail studentDetail = currentStudentDetail();
        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(currentUserId(), studentDetail.getClazzId())
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
        AlpsFuture<List<ClazzRecordCardMapper>> top3List = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadTop3FashionList(studentDetail.getClazzId(), classmates);
        AlpsFuture<ClazzRecordCardMapper> weekTop = asyncClazzRecordServiceClient.getAsyncClazzRecordService().loadWeekTopFashionCard(studentDetail.getClazzId(), classmates);

        // 装扮之星是以学期为单位的，按照学期做key
        SchoolYearPhase syp = SchoolYear.newInstance().currentPhase();
        if (syp == SchoolYearPhase.WINTER_VACATION) {
            syp = SchoolYearPhase.LAST_TERM;
        } else if (syp == SchoolYearPhase.SUMMER_VACATION) {
            syp = SchoolYearPhase.NEXT_TERM;
        }

        String recordId = StringUtils.join(FASHION_STAR.name(), "_", studentDetail.getClazzId(), "_", syp.name());

        return MapMessage.successMessage()
                .add("weekDate", parseWeek())
                .add("result", top3List.getUninterruptibly())
                .add("weekTop", weekTop.getUninterruptibly())
                .add("recordId", recordId);
    }

    /**
     * 满分top3
     */
    @RequestMapping(value = "/fullMarksTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fullMarksTop3() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        StudentDetail studentDetail = currentStudentDetail();
        List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(currentUserId(), false);
        List<Long> groupIds = groups
                .stream()
                .map(GroupMapper::getId).collect(Collectors.toList());

        // 满分之星是以学期为单位的，按照学期做key
        SchoolYearPhase syp = SchoolYear.newInstance().currentPhase();
        if (syp == SchoolYearPhase.WINTER_VACATION) {
            syp = SchoolYearPhase.LAST_TERM;
        } else if (syp == SchoolYearPhase.SUMMER_VACATION) {
            syp = SchoolYearPhase.NEXT_TERM;
        }

        String recordId = StringUtils.join(FULLMARKS_STAR.name(), "_", studentDetail.getClazzId(), "_", syp.name());

        return MapMessage.successMessage()
                .add("result", clazzRecordServiceClient.queryFullMarksTop3(groupIds, studentDetail.getClazzId()))
                .add("weekTop", clazzRecordServiceClient.queryWeekTopFullMarks(groupIds, studentDetail.getClazzId()))
                .add("weekDate", parseWeek())
                .add("recordId", recordId);
    }

    /**
     * 友爱top3
     */
    @RequestMapping(value = "/friendshipTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage friendshipTop3() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        StudentDetail studentDetail = currentStudentDetail();
        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(currentUserId(), studentDetail.getClazzId())
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 友爱之星是以学期为单位的，按照学期做key
        SchoolYearPhase syp = SchoolYear.newInstance().currentPhase();
        if (syp == SchoolYearPhase.WINTER_VACATION) {
            syp = SchoolYearPhase.LAST_TERM;
        } else if (syp == SchoolYearPhase.SUMMER_VACATION) {
            syp = SchoolYearPhase.NEXT_TERM;
        }

        String recordId = StringUtils.join(FRIENDSHIP_STAR.name(), "_", studentDetail.getClazzId(), "_", syp.name());

        return MapMessage.successMessage()
                .add("result", clazzRecordServiceClient.queryFriendTop3(studentDetail.getClazzId(), classmates))
                .add("weekTop", clazzRecordServiceClient.queryWeekTopFriendShip(studentDetail.getClazzId(), classmates))
                .add("weekDate", parseWeek())
                .add("recordId", recordId);
    }

    /**
     * 毅力top3
     */
    @RequestMapping(value = "/staminaTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage staminaTop3() {
        return MapMessage.errorMessage("该卡片已经下线");
//        if (studentUnLogin()) {
//            return MapMessage.errorMessage("请重新登录");
//        }
//        StudentDetail studentDetail = currentStudentDetail();
//        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(currentUserId(), studentDetail.getClazzId())
//                .stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        String weekStartDate = DateUtils.dateToString(WeekRange.current().getStartDate(), "yyyyMMdd");
//        String recordId = StringUtils.join(STAMINA_STAR.name(), "_", studentDetail.getClazzId(), "_", weekStartDate);
//
//        return MapMessage.successMessage()
//                .add("result", clazzRecordServiceClient.queryStaminaTop3(studentDetail.getClazzId(), classmates))
//                .add("weekTop", clazzRecordServiceClient.queryStaminaWeekTop(studentDetail.getClazzId(), classmates))
//                .add("weekDate", parseWeek())
//                .add("recordId", recordId);
    }

    /**
     * 闯关top3
     */
    @RequestMapping(value = "/crackerTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage crackerTop3() {
        return MapMessage.errorMessage("该卡片已经下线");
//        if (studentUnLogin()) {
//            return MapMessage.errorMessage("请重新登录");
//        }
//        StudentDetail studentDetail = currentStudentDetail();
//        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(currentUserId(), studentDetail.getClazzId())
//                .stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        String weekStartDate = DateUtils.dateToString(WeekRange.current().getStartDate(), "yyyyMMdd");
//        String recordId = StringUtils.join(CRACKER_STAR.name(), "_", studentDetail.getClazzId(), "_", weekStartDate);
//
//        return MapMessage.successMessage()
//                .add("result", clazzRecordServiceClient.queryCrackerTop3(studentDetail.getClazzId(), classmates))
//                .add("weekTop", clazzRecordServiceClient.queryCrackerWeekTop(studentDetail.getClazzId(), classmates))
//                .add("weekDate", parseWeek())
//                .add("recordId", recordId);
    }

    /**
     * 竞技top3
     */
    @RequestMapping(value = "/competitionTop3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage competitionTop3() {
        return MapMessage.errorMessage("该卡片已经下线");
//        if (studentUnLogin()) {
//            return MapMessage.errorMessage("请重新登录");
//        }
//        StudentDetail studentDetail = currentStudentDetail();
//        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(currentUserId(), studentDetail.getClazzId())
//                .stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        String weekStartDate = DateUtils.dateToString(WeekRange.current().getStartDate(), "yyyyMMdd");
//        String recordId = StringUtils.join(COMPETE_STAR.name(), "_", studentDetail.getClazzId(), "_", weekStartDate);
//
//        return MapMessage.successMessage()
//                .add("result", clazzRecordServiceClient.queryCompetitionTop3(studentDetail.getClazzId(), classmates))
//                .add("weekTop", clazzRecordServiceClient.queryCompetitionWeekTop(studentDetail.getClazzId(), classmates))
//                .add("weekDate", parseWeek())
//                .add("recordId", recordId);
    }

    /**
     * 自学产品-我的努力页面
     */
    @RequestMapping(value = "/leaningzoneEffort.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage leaningzoneEffort() {
        return MapMessage.errorMessage("该卡片已经下线");
//        if (studentUnLogin()) {
//            return MapMessage.errorMessage("请重新登录");
//        }
//        StudentDetail student = currentStudentDetail();
//        if (student.getClazz() == null) {
//            return MapMessage.errorMessage("请先加入班级");
//        }
//        String type = getRequestString("recordType");
//        Long studentId = currentUserId();
//        Long clazzId = student.getClazzId();
//        try {
//            List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(studentId, clazzId)
//                    .stream()
//                    .map(User::getId)
//                    .collect(Collectors.toList());
//            ClazzRecordTypeEnum recordType = ClazzRecordTypeEnum.safeParse(type);
//            AlpsFuture<ClazzRecordCardMapper> futureChampion = new ValueWrapperFuture<>(null);  // 第一名
//            AlpsFuture<Map<String, Integer>> myStatistic = asyncClazzRecordServiceClient.getAsyncClazzRecordService()
//                    .loadStudentStatistic(studentId);
//            int myScore = 0;
//            switch (recordType) {
//                case STAMINA_STAR:
//                    // 本周毅力之星
//                    futureChampion = asyncClazzRecordServiceClient.getAsyncClazzRecordService()
//                            .loadStaminaCard(clazzId, classmates);
//                    myScore = myStatistic.getUninterruptibly().getOrDefault("stamina", 0);
//                    break;
//                case CRACKER_STAR:
//                    // 本周闯关之星
//                    futureChampion = asyncClazzRecordServiceClient.getAsyncClazzRecordService()
//                            .loadCrackerCard(clazzId, classmates);
//                    myScore = myStatistic.getUninterruptibly().getOrDefault("cracker", 0);
//                    break;
//                case COMPETE_STAR:
//                    // 本周竞技之星
//                    futureChampion = asyncClazzRecordServiceClient.getAsyncClazzRecordService()
//                            .loadCompetitionCard(clazzId, classmates);
//                    myScore = myStatistic.getUninterruptibly().getOrDefault("compete", 0);
//                    break;
//                default:
//                    break;
//            }
//            ClazzRecordCardMapper champion = futureChampion.getUninterruptibly();
//            int championScore = champion == null ? 0 : SafeConverter.toInt(champion.getCount());
//            return MapMessage.successMessage()
//                    .add("isChampion", myScore >= championScore)
//                    .add("championScore", championScore)
//                    .add("myScore", myScore);
//        } catch (Exception ex) {
//            logger.error("Failed load learningzone effort page, student={}, recordType={}", studentId, type, ex);
//            return MapMessage.errorMessage("网络异常");
//        }
    }

    /**
     * 封装历史作业记录信息
     */
    private Map<String, Map<String, String>> queryHomeworkInfos(List<NewHomework.Location> hws, String currentHwId) {
        if (CollectionUtils.isEmpty(hws)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, String>> mapMap = new HashMap<>();
        for (int i = 0; i < hws.size(); i++) {
            NewHomework.Location location = hws.get(i);
            if (location == null || !StringUtils.equals(currentHwId, location.getId())) continue;

            Map<String, String> currentMap = new HashMap<>();
            currentMap.put(location.getId(), DateUtils.dateToString(new Date(location.getCreateTime()), "MM月dd日"));
            mapMap.put("current", currentMap);
            if (i > 0) {
                int j = i - 1;
                NewHomework.Location preLocation = hws.get(j);
                if (preLocation != null) {
                    Map<String, String> hwIdMap = new HashMap<>();
                    hwIdMap.put(preLocation.getId(), DateUtils.dateToString(new Date(preLocation.getCreateTime()), "MM月dd日"));
                    mapMap.put("pre", hwIdMap);
                } else {
                    mapMap.put("pre", null);
                }
            } else {
                mapMap.put("pre", null);
            }

            if (i >= hws.size() - 1) {
                mapMap.put("next", null);
            } else {
                int j = i + 1;
                NewHomework.Location nextLocation = hws.get(j);
                if (nextLocation != null) {
                    Map<String, String> hwIdMap = new HashMap<>();
                    hwIdMap.put(nextLocation.getId(), DateUtils.dateToString(new Date(nextLocation.getCreateTime()), "MM月dd日"));
                    mapMap.put("next", hwIdMap);
                } else {
                    mapMap.put("next", null);
                }
            }
        }

        return mapMap;
    }

    /**
     * 查看本季度所有的家庭作业
     */
    private List<NewHomework.Location> queryHwsByGroups() {
        // query all valid group homework
        List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(currentUserId(), false);
        Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());

        return clazzRecordServiceClient.loadHomeworkList(groupIds);
    }

    private String parseWeek() {
        WeekRange range = WeekRange.current();
        String format = "MM月dd日";
        return StringUtils.formatMessage(
                "{}-{}",
                DateUtils.dateToString(range.getStartDate(), format),
                DateUtils.dateToString(range.getEndDate(), format)
        );
    }

    /**
     * 是否分享过记录
     */
    private boolean isShared(String recordType, String hwId) {
        List<RecordSoundShareMapper> recordSoundShareMappers = clazzRecordServiceClient
                .queryRecordSoundShare(currentStudentDetail().getClazzId(), currentUserId())
                .stream()
                .filter(r -> Objects.equals(currentUserId(), r.getUserId()))
                .filter(r -> StringUtils.equals(recordType, r.getRecordTypeEnumName()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(recordSoundShareMappers)) {
            return false;
        }
        // 如果是作业类 同一个作业记录不准重复分享
        if (StringUtils.isNotBlank(hwId)) {
            return recordSoundShareMappers.stream()
                    .anyMatch(r -> StringUtils.equals(hwId, r.getHwId()));
        }
        // 如果是非作业类 一天只准分享一次
        return recordSoundShareMappers.stream()
                .anyMatch(r -> StringUtils.equals(
                        DateUtils.dateToString(new Date(), "dd"),
                        DateUtils.dateToString(new Date(r.getCreateTime()), "dd")
                        )
                );
    }

    // 自学灰度判断
    private boolean isLearningZoneGray() {
        // 测试环境不灰度了
        if (RuntimeMode.le(Mode.TEST)) {
            return true;
        }
        return grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(
                        currentStudentDetail(), "ClassSpace", "LearningZone"
                );
    }

}

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

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.IterableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.support.CreditHistoryBuilderFactory;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkReportService;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardBusinessType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardItemType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardStatus;
import com.voxlearning.utopia.service.parentreward.api.entity.*;
import com.voxlearning.utopia.service.parentreward.api.mapper.*;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCache;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCacheManager;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardHelper;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardScoreLevelCalculator;
import com.voxlearning.utopia.service.piclisten.api.GrindEarService;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.wonderland.api.WonderlandService;
import com.voxlearning.utopia.temp.GrindEarActivity;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2017/5/4
 */
@Controller
@RequestMapping(value = "studentMobile/parent/reward")
public class MobileStudentParentRewardController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = NewHomeworkReportService.class)
    private NewHomeworkReportService newHomeworkReportService;
    @ImportService(interfaceClass = NewHomeworkLoader.class)
    private NewHomeworkLoader newHomeworkLoader;
    @ImportService(interfaceClass = GrindEarService.class)
    private GrindEarService grindEarService;
    @ImportService(interfaceClass = WonderlandService.class)
    private WonderlandService wonderlandService;
    @Inject
    private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;

    @Inject
    private ParentRewardLoaderClient parentRewardLoaderClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;

    private static List<String> rewardContentList;
    private static List<String> rewardLetterList;
    private static List<String> thanksLetterList;
    private static List<ParentRewardBusinessType> businessTypeList;

    static {
        rewardContentList = new ArrayList<>();
        rewardContentList.add("你真棒，");
        rewardContentList.add("表现不错！");
        rewardContentList.add("做的很好！");
        rewardContentList.add("给你点赞，");
        rewardLetterList = new ArrayList<>();
        rewardLetterList.add("给我发奖励吧");
        rewardLetterList.add("请给我支持与鼓励");
        rewardLetterList.add("发个家长奖励呀～");
        thanksLetterList = new ArrayList<>();
        thanksLetterList.add("callName，谢谢你！爱你呦 <br>(ㅅ^v^)♡");
        thanksLetterList.add("callName，让奖励来的更猛烈吧！么么哒 <br>(。•ω•。)ノ♡");
        thanksLetterList.add("callName，辛苦了！我会令你骄傲的！");
        thanksLetterList.add("callName，我想对你说：谢谢！我会好好学习的");
        thanksLetterList.add("谢谢callName给我发奖励，我会继续努力哒 <br>(￣︶￣)↗");
        thanksLetterList.add("谢谢callName对我的关心，奖励不要停～ <br>( #^.^# )");
        thanksLetterList.add("谢谢callName我会继续努力向前冲~ ");
        thanksLetterList.add("谢谢callName陪伴我长大 <br>╭(●｀∀´●)╯╰(●’◡’●)╮");

        businessTypeList = new ArrayList<>();
        businessTypeList.add(ParentRewardBusinessType.GW_ENGLISH_ISLAND);
        businessTypeList.add(ParentRewardBusinessType.GW_MATH_ISLAND);
        businessTypeList.add(ParentRewardBusinessType.GW_CHINESE_ISLAND);
        businessTypeList.add(ParentRewardBusinessType.GW_WISDOM_ISLAND);
        businessTypeList.add(ParentRewardBusinessType.GW_COMPETITION_ISLAND);
        businessTypeList.add(ParentRewardBusinessType.TEAMWORK);
    }

    /**
     * 成长世界入口
     */
    @RequestMapping(value = "gw/reward.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage growthWorldReward() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null)
            return MapMessage.errorMessage("请重新登录");
        Long studentId = studentDetail.getId();
        try {
            List<ParentRewardLog> parentRewardLogs = parentRewardLoader.getSevenDaysRewardList(studentId);
            List<ParentRewardLog> initLogs = parentRewardLogs.stream()
                    .filter(ParentRewardLog::sendNotExpired)
                    .filter(rewardLog -> {
                        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                        if (item != null) {
                            ParentRewardBusinessType businessType = ParentRewardBusinessType.of(item.getBusiness());
                            return businessTypeList.contains(businessType);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            int rewardCount = initLogs.size();
            return MapMessage.successMessage().add("has_reward", rewardCount > 0);
        } catch (Exception ex) {
            logger.error("{}, sid:{}", ex.getMessage(), studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 排行榜
     */
    @RequestMapping(value = "range.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage range() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null)
            return MapMessage.errorMessage("请重新登录");
        Long studentId = studentDetail.getId();

        try {
            Integer currentPage = getRequestInt("currentPage", 1);
            Pageable page = new PageRequest(currentPage - 1, 10);
            AlpsFuture<Integer> parentRewardStudentRank = parentRewardLoader.getParentRewardStudentRank(studentId);
            Page<ParentRewardStudentCountWrapper> parentRewardStudentRangeListPage = parentRewardLoader.getParentRewardStudentRangeList(studentId, page);
            parentRewardStudentRangeListPage.forEach(t -> t.setAvatar(getUserAvatarImgUrl(t.getAvatar())));

            return MapMessage.successMessage()
                    .add("range_list", parentRewardStudentRangeListPage.getContent())
                    .add("total_count", parentRewardStudentRangeListPage.getTotalElements())
                    .add("currentPage", currentPage)
                    .add("month", LocalDateTime.now().getMonth().getValue())
                    .add("totalPage", parentRewardStudentRangeListPage.getTotalPages())
                    .add("range", null == parentRewardStudentRank ? 0 : parentRewardStudentRank.getUninterruptibly());
        } catch (Exception ex) {
            logger.error("{}, sid:{}", ex.getMessage(), studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 新学生注册家长奖励
     */
    @RequestMapping(value = "register.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage registerReward() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        CacheObject cacheObject = ParentRewardCache.getPersistenceCache().get("NEW_STUDENT_REGISTER_PARENT_REWARD_" + studentDetail.getId());
        if (cacheObject == null || cacheObject.getValue() == null) {
            parentRewardService.generateParentReward(studentDetail.getId(), "NEW_STUDENT_REGISTER_PARENT", Collections.emptyMap());
            ParentRewardCache.getPersistenceCache().incr("NEW_STUDENT_REGISTER_PARENT_REWARD_" + studentDetail.getId(), 1, 1, 0);
        }
        return MapMessage.successMessage();
    }

    /**
     * 首次绑定家长
     */
    @RequestMapping(value = "bindparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindParent() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null)
            return MapMessage.errorMessage("请重新登录");
        Long studentId = studentDetail.getId();
        String dateString;
        String expireDateString;
        String dateCacheKey = "FIRST_TIME_TO_BIND_PARENT_DATE_" + studentId;
        String expireDateCacheKey = "FIRST_TIME_TO_BIND_PARENT_EXPIRE_DATE_" + studentId;
        String itemKey = "FIRST_TIME_TO_BIND_PARENT";
        CacheObject<Object> cacheObject = ParentRewardCache.getPersistenceCache().get(dateCacheKey);
        if (cacheObject == null || cacheObject.getValue() == null) {
            parentRewardService.generateParentReward(studentDetail.getId(), itemKey, Collections.emptyMap());
            dateString = DateUtils.dateToString(new Date(), "MM月dd日 HH:mm");
            ParentRewardCache.getPersistenceCache().set(dateCacheKey, 0, dateString);
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(itemKey);
            expireDateString = DateUtils.dateToString(DateUtils.addDays(DayRange.current().getEndDate(), item.getSendExpireDays()), "MM月dd日 HH:mm");
            ParentRewardCache.getPersistenceCache().set(expireDateCacheKey, 0, expireDateString);
        } else {
            dateString = SafeConverter.toString(cacheObject.getValue());
            String expireDate = ParentRewardCache.getPersistenceCache().load(expireDateCacheKey);
            expireDateString = SafeConverter.toString(expireDate);
        }
        return MapMessage.successMessage()
                .add("hasReceived", cacheObject != null && cacheObject.getValue() != null)
                .add("date", dateString)
                .add("expireDate", expireDateString);
    }

    /**
     * 学生领取奖励首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        MapMessage resultMap = MapMessage.successMessage();
        User student = currentUser();
        if (null == student) {
            return MapMessage.errorMessage("请重新登录");
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = studentDetail.getId();
        try {
            //家庭关爱值
            Set<String> ids = new HashSet<>();
            String currentTermId = StudentRewardSendCount.generateId(studentId, ParentRewardHelper.currentTermDateRange().getStartDate());
            String lastTermId = StudentRewardSendCount.generateId(studentId, ParentRewardHelper.lastTermDateRange().getStartDate());
            ids.add(currentTermId);
            ids.add(lastTermId);
            Map<String, StudentRewardSendCount> studentRewardSendCountMap = parentRewardLoader.getStudentRewardSendCount(ids);
            long itemCount = 0;
            if (studentRewardSendCountMap.get(currentTermId) != null) {
                itemCount = studentRewardSendCountMap.get(currentTermId).getItemCount();
            }
            resultMap.put("item_count", itemCount);
            int level = ParentRewardScoreLevelCalculator.getLevel(itemCount);
            resultMap.put("level", level);
            CacheObject<Object> objectCacheObject = ParentRewardCache.getPersistenceCache().get("STUDENT_REWARD_LEVEL_" + studentId);
            if (null == objectCacheObject || null == objectCacheObject.getValue()) {
                ParentRewardCache.getPersistenceCache().set("STUDENT_REWARD_LEVEL_" + studentId, (int) ((ParentRewardHelper.currentTermDateRange().getEndTime() - Instant.now().toEpochMilli()) / 1000), level);
                resultMap.put("newLevel", false);
            } else {
                int oldLevel = SafeConverter.toInt(objectCacheObject.getValue());
                if (level > oldLevel) {
                    ParentRewardCache.getPersistenceCache().set("STUDENT_REWARD_LEVEL_" + studentId, (int) ((ParentRewardHelper.currentTermDateRange().getEndTime() - Instant.now().toEpochMilli()) / 1000), level);
                    resultMap.put("newLevel", true);
                } else {
                    resultMap.put("newLevel", false);
                }
            }

            //本学期奖励信息
            ParentRewardStudentStatistics studentStatistics = parentRewardLoader.getCurrentTermStudentStatistics(studentId);
            Map<String, Object> termMap = getTermRewardInfo(studentStatistics);
            resultMap.add("term_reward_count", termMap);

            //规则弹窗
            boolean showRuleTip = false;
            boolean flag = SafeConverter.toInt(CacheSystem.CBS.getCache("persistence").load("SHOW_RULE_TIP_" + studentId)) == 0;
            Date now = new Date();
            if (flag && now.before(DateUtils.stringToDate("2019-03-15 25:59:59"))) {
                showRuleTip = true;
            }
            resultMap.add("show_rule_tip", showRuleTip);
            CacheSystem.CBS.getCache("persistence").incr("SHOW_RULE_TIP_" + studentId, 1, 1, 0);

            //上学期奖励信息弹窗（家庭互动值大于0 & 没弹过 & 弹出时间范围内）
            long lastItemCount = 0;
            if (studentRewardSendCountMap.get(lastTermId) != null) {
                lastItemCount = studentRewardSendCountMap.get(lastTermId).getItemCount();
            }
            if (lastItemCount > 0 && ParentRewardHelper.showLastTermInfo() && !ParentRewardCacheManager.INSTANCE.studentHasShowLastTermInfo(studentId)) {
                resultMap.put("last_item_count", lastItemCount);
                resultMap.put("student_name", studentDetail.fetchRealname());
                ParentRewardStudentStatistics lastTermStatistics = parentRewardLoader.getLastTermStudentStatistics(studentId);
                if (lastTermStatistics != null) {
                    resultMap.put("receive_count", SafeConverter.toInt(lastTermStatistics.getItemCount()));
                }
                ParentRewardCache.getPersistenceCache().incr(ParentRewardHelper.lastTermStudentStatisticsFlag(studentId), 1, 1, ParentRewardHelper.termCacheExpireTime());
            }

            //是否绑定了家长
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            boolean bindParent = CollectionUtils.isNotEmpty(studentParentRefs);
            resultMap.add("bind_parent", bindParent);

            //主形象奖励项--未发放今天过期|未发放未过期|待领取|今天已领取
            List<ParentRewardLog> parentRewardLogs = parentRewardLoader.getSummaryRewardList(studentId);

            //本次待领取的奖励项数量(过滤条件需要兼容新老数据)
            long currentRewardCount = parentRewardLogs.stream()
                    .filter(ParentRewardLog::sendNotExpired)
                    .count();
            //是否有待发的奖励
            boolean hasReward = currentRewardCount > 0;
            resultMap.add("has_reward", hasReward);
            resultMap.add("current_reward_count", currentRewardCount);

            //学豆类型的奖励的详细列表
            //成长世界自学一关需要单独处理，奖励记录列表里面也是。产品认为这是一个让用户觉得产品更简单的方案，可能她对简单这个词有什么误解吧
            List<ParentRewardLog> gwRewardList = parentRewardLogs.stream()
                    .filter(rewardLog -> "FINISH_WONDERLAND_MISSION".equals(rewardLog.getKey()))
                    .collect(Collectors.toList());
            List<ParentRewardSummary> gwSummaryList = generaSummaryList(gwRewardList, studentDetail);

            //除了成长世界自学一关的
            List<ParentRewardLog> leftRewardList = parentRewardLogs.stream()
                    .filter(rewardLog -> !"FINISH_WONDERLAND_MISSION".equals(rewardLog.getKey()))
                    .collect(Collectors.toList());
            Map<String, List<ParentRewardSummary>> summaryMap = generateRewardSummary(leftRewardList, studentDetail);
            summaryMap.put(ParentRewardItemType.EVOLUTION_STONE.name(), gwSummaryList);

            String rewardContent = "";
            boolean isInBlacklist = isStudentInActivityBlacklist();
            resultMap.add("isInBlacklist", isInBlacklist);
            //没有待发奖励并且没有绑定家长
            if (!hasReward) {
                //没有绑定家长不会给成长世界导流，默认为true
                resultMap.put("finish_task", Boolean.TRUE);
                if (bindParent) {
                    List<String> randomTexts = new ArrayList<>();
                    randomTexts.add("Hi，" + studentDetail.fetchRealname() + "，表现好就能得更多奖励哦");
                    randomTexts.add("Hi，" + studentDetail.fetchRealname() + "，凡努力必有回报，加油吧");
                    randomTexts.add("Hi，" + studentDetail.fetchRealname() + "，努力就会进步，还有奖励哦");
                    randomTexts.add("Hi，" + studentDetail.fetchRealname() + "，做好准备，让奖励来的更猛烈些！");
                    randomTexts.add("Hi，" + studentDetail.fetchRealname() + "，良好表现可获得家长额外奖励哦");
                    Collections.shuffle(randomTexts);
                    rewardContent = randomTexts.get(0);
                    //没有待发奖励，绑定了家长
                    //query homework need to finish
                    Map<String, Object> homeworkMap = getUnfinishHomework(studentDetail);
                    resultMap.add("homework_info", homeworkMap);
                    //如果没有作业并且在活动期限内，则看看今天是否有未完成的点读机磨耳朵活动
                    if (MapUtils.isEmpty(homeworkMap)) {
                        //在磨耳朵活动期限内并且没有完成活动
                        boolean grindEar = GrindEarActivity.isInActivityPeriod() && !grindEarService.studentIsFinishTodayTask(studentId);
                        resultMap.put("grind_ear", grindEar);
                        boolean finishTask = parentRewardLogs.stream().anyMatch(log -> ParentRewardItemType.EVOLUTION_STONE.name().equals(log.getType())) && !isInBlacklist;
                        resultMap.put("finish_task", finishTask);
                    }
                }
            } else {
                Collections.shuffle(rewardContentList);
                rewardContent += "Hi，" + studentDetail.fetchRealname() + "，" + rewardContentList.get(0);

                //待发放奖励的奖励（学豆|学分）数量信息
                Map<String, Object> rewardMap = new HashMap<>();
                summaryMap.forEach((key, value) -> {
                    //待领取的奖励总和
                    ParentRewardSummary summary = value.stream().filter(e -> e.getStatus() == ParentRewardStatus.INIT.getType() && !e.getExpire()).findFirst().orElse(null);
                    int rewardCount = summary == null ? 0 : SafeConverter.toInt(summary.getCount());
                    rewardMap.put(key, rewardCount);
                });
                resultMap.add("reward_count_info", rewardMap);
                //是否发送过亲子信
                boolean hasSendLetter = !ParentRewardCacheManager.INSTANCE.isStudentNeverSendLetterToday(currentUserId());
                resultMap.put("has_send_letter", hasSendLetter);

            }

            //主形象中各类型奖励的总结
            List<ParentRewardSummary> rewardList = summaryMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            //主形象中要显示的所有奖励信息
            resultMap.add("reward_list", rewardList);
            resultMap.add("reward_content", rewardContent);

            AlpsFuture<Integer> parentRewardStudentRank = parentRewardLoader.getParentRewardStudentRank(studentId);
            resultMap.add("rank", null == parentRewardStudentRank ? 0 : SafeConverter.toInt(parentRewardStudentRank.getUninterruptibly()));

            boolean showReceiveRemind = ParentRewardCacheManager.INSTANCE.showReceiveRemind(studentId);
            resultMap.add("show_receive_remind", showReceiveRemind);
            if (showReceiveRemind) {
                ParentRewardCache.getPersistenceCache().incr(ParentRewardHelper.remindCacheKey(studentId), 1, 1, 0);
            }

            return resultMap;
        } catch (Exception ex) {
            logger.error("{}, sid:{}", ex.getMessage(), studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }

    }

    /**
     * 领取奖励
     */
    @RequestMapping(value = "getReward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getReward() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        String rewards = getRequestString("rewards");
        if (StringUtils.isEmpty(rewards)) {
            return MapMessage.errorMessage("没有待领取的奖励");
        }
        Long studentId = currentUserId();
        try {
            List<ParentRewardLog> rewardLogs = JsonUtils.fromJsonToList(rewards, ParentRewardLog.class);
            if (CollectionUtils.isEmpty(rewardLogs)) {
                return MapMessage.errorMessage("没有待领取的奖励");
            }
            rewardLogs = rewardLogs.stream().filter(rewardLog -> Objects.equals(studentId, rewardLog.getStudentId())).collect(Collectors.toList());
            ParentRewardReceiveResult result = parentRewardService.receiveParentReward(studentId, rewardLogs);
            if (result.getIntegral() > 0) {
                IntegralHistory integralHistory = new IntegralHistory();
                integralHistory.setIntegral(result.getIntegral());
                integralHistory.setComment("家长奖励获得学豆");
                integralHistory.setIntegralType(IntegralType.STUDENT_HOMEWORK_FROM_PARENT_REWARD.getType());
                integralHistory.setUserId(studentId);
                try {
                    MapMessage mapMessage = AtomicLockManager.instance()
                            .wrapAtomic(userIntegralService)
                            .keyPrefix("receiveParentReward")
                            .keys(studentId)
                            .proxy()
                            .changeIntegral(integralHistory);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage("领取学豆奖励失败");
                    }
                } catch (DuplicatedOperationException e) {
                    return MapMessage.errorMessage("重复领取");
                }
            }
            if (result.getPoints() > 0) {
                CreditType creditType = CreditType.student_receive_parent_reward;
                CreditHistory creditHistory = CreditHistoryBuilderFactory.newBuilder(studentId, creditType)
                        .withAmount(result.getPoints())
                        .withComment("领取家长奖励自学积分")
                        .build();
                if (!userIntegralService.changeCredit(creditHistory).isSuccess()) {
                    return MapMessage.errorMessage("领取自学积分奖励失败");
                }
            }

            return MapMessage.successMessage().add("reward_info", result);
        } catch (Exception ex) {
            logger.error("{}, sid:{}", ex.getMessage(), studentId, ex);
            return MapMessage.errorMessage("领取失败");
        }
    }

    /**
     * 家长奖励记录
     */
    @RequestMapping(value = "getRewardList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRewardList() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = studentDetail.getId();
        boolean expire = getRequestBool("expire");
        Integer currentPage = getRequestInt("currentPage", 1);
        try {
            MapMessage resultMap = MapMessage.successMessage();
            Pageable page = new PageRequest(currentPage - 1, 10);
            Page<ParentRewardLog> rewardLogPage = parentRewardLoaderClient.loadParentRewardLogPage(studentId, page, expire);
            List<ParentRewardRecordWrapper> rewardList = new ArrayList<>();
            int totalPages = 0;
            long expireTotalCount;
            long totalCount = 0;
            if (rewardLogPage != null && CollectionUtils.isNotEmpty(rewardLogPage.getContent())) {
                List<ParentRewardLog> rewardLogList = rewardLogPage.getContent();
                boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                rewardLogList.forEach(log -> rewardList.add(convert(log, studentParentRefs, showScoreLevel)));
                totalPages = rewardLogPage.getTotalPages();
                totalCount = rewardLogPage.getTotalElements();
            }

            if (expire) {
                expireTotalCount = totalCount;
                totalCount = parentRewardLoaderClient.getTotalCount(studentId, Boolean.FALSE);
            } else {
                expireTotalCount = parentRewardLoaderClient.getTotalCount(studentId, Boolean.TRUE);
            }

            //家庭关爱值
            Set<String> ids = new HashSet<>();
            ids.add(StudentRewardSendCount.generateId(studentId, ParentRewardHelper.lastTermDateRange().getStartDate()));
            Map<String, StudentRewardSendCount> studentRewardSendCountMap = parentRewardLoader.getStudentRewardSendCount(ids);
            long itemCount = studentRewardSendCountMap.values().stream().mapToLong(StudentRewardSendCount::getItemCount).sum();
            if (itemCount > 0) {
                resultMap.add("student_name", studentDetail.fetchRealname());
                ParentRewardStudentStatistics studentStatistics = parentRewardLoader.getLastTermStudentStatistics(studentId);
                if (studentStatistics != null) {
                    resultMap.add("receive_count", SafeConverter.toInt(studentStatistics.getItemCount()));
                }
                resultMap.add("last_item_count", itemCount);
            }

            return resultMap.add("totalPage", totalPages)
                    .add("currentPage", currentPage)
                    .add("totalCount", totalCount)
                    .add("expireTotalCount", expireTotalCount)
                    .add("reward_list", rewardList);
        } catch (Exception e) {
            logger.error("{}, sid:{}", e.getMessage(), studentId, e);
            return MapMessage.errorMessage("获取家长奖励列表异常");
        }
    }

    @RequestMapping(value = "letterList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage geLetterList() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = studentDetail.getId();
        String letterType = getRequestString("type");
        try {
            List<String> letters = new ArrayList<>();
            if ("reward".equals(letterType)) {
                Set<String> rewardLetters = new HashSet<>();
                boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
                List<ParentRewardLog> parentRewardLogs = parentRewardLoader.getParentRewardList(studentId, ParentRewardStatus.INIT.getType());
                Set<String> itemTitleSet = new HashSet<>();
                parentRewardLogs.forEach(e -> {
                    ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(e.getKey());
                    if (item != null) {
                        ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                        if (category != null) {
                            String itemTitle = getParentRewardDescription(e, item, category, showScoreLevel);
                            itemTitleSet.add(itemTitle);
                        }
                    }
                });
                itemTitleSet.forEach(e -> {
                    Collections.shuffle(rewardLetterList);
                    rewardLetters.add("我" + e + "，" + rewardLetterList.get(0));
                });
                rewardLetters.add("不发内容，发个抖一抖");
                letters.addAll(rewardLetters);
            } else {
                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                Set<String> callNameList = studentParentRefs.stream()
                        .filter(e -> StringUtils.isNotBlank(e.getCallName()))
                        .map(StudentParentRef::getCallName)
                        .collect(Collectors.toSet());

                if (callNameList.size() == 1) {
                    String callName = IterableUtils.get(callNameList, 0);
                    if (CallName.其它监护人.name().equals(callName)) {
                        callName = "";
                    }
                    Collections.shuffle(thanksLetterList);
                    //如果家长callName是空字符串或者其他监护人的时候，亲子信有可能出现开头是逗号的情况，需要处理一下
                    String letter = thanksLetterList.get(0).replace("callName", callName);
                    letter = letter.startsWith("，") ? letter.replace("，", "") : letter;
                    letters.add(letter);
                    letter = thanksLetterList.get(1).replace("callName", callName);
                    letter = letter.startsWith("，") ? letter.replace("，", "") : letter;
                    letters.add(letter);
                } else {
                    StringBuilder callNameContent = new StringBuilder();
                    //排除""或者其他监护人
                    String letter;
                    for (String callName : callNameList) {
                        if (CallName.其它监护人.name().equals(callName)) {
                            callName = "";
                        }
                        Collections.shuffle(thanksLetterList);
                        letter = thanksLetterList.get(0).replace("callName", callName);
                        letter = letter.startsWith("，") ? letter.replace("，", "") : letter;
                        letters.add(letter);
                        if (StringUtils.isBlank(callName)) {
                            continue;
                        }
                        callNameContent.append(callName).append("、");
                    }
                    if (callNameContent.length() > 0) {
                        callNameContent = new StringBuilder(callNameContent.substring(0, callNameContent.length() - 1));
                    }
                    List<String> randomTexts = new ArrayList<>();
                    randomTexts.add("感谢" + callNameContent + "对我的鼓励～ <br>(^ ^)∠※");
                    randomTexts.add(callNameContent + "，爱你～ <br>づ￣ 3￣)づ");
                    randomTexts.add("谢谢" + callNameContent + "的奖励，我希望我们全家天天开心~");
                    randomTexts.add("Thank you！I love you！");
                    Collections.shuffle(randomTexts);
                    letters.add(0, randomTexts.get(0));
                }
            }
            return MapMessage.successMessage().add("letters", letters);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "sendLetter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendLetter() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = studentDetail.getId();
        String content = getRequestString("content");
        //亲子信类型（reward-要奖励|thanks-表感谢）
        String type = getRequestString("type");
        try {
            boolean available = ParentRewardCacheManager.INSTANCE.isStudentNeverSendLetterToday(currentUserId());
            if (!available) {
                return MapMessage.errorMessage("每天只能发送一封亲子信，你今天已经发过了");
            }

            Map<Long, String> receiverMap = studentLoaderClient.loadStudentParentRefs(studentId)
                    .stream()
                    .filter(e -> StringUtils.isNotBlank(e.getCallName()))
                    .collect(Collectors.toMap(StudentParentRef::getParentId, StudentParentRef::getCallName));
            if (MapUtils.isNotEmpty(receiverMap)) {
                String tip;
                if (content.contains("抖一抖")) {
                    content = "发了一个抖一抖";
                    tip = studentDetail.fetchRealname() + "抖了你一下，提醒你给Ta发奖励哦";
                    for (Long receiverId : receiverMap.keySet()) {
                        parentRewardService.sendLetter(studentId, receiverId, content, tip, DateUtils.addDays(new Date(), 2).getTime());
                    }
                } else if ("reward".equals(type)) {
                    boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");

                    List<ParentRewardLog> rewardLogList = parentRewardLoader.getParentRewardList(studentId, ParentRewardStatus.INIT.getType())
                            .stream()
                            .sorted(Comparator.comparing(ParentRewardLog::getCreateTime))
                            .collect(Collectors.toList());
                    for (Map.Entry entry : receiverMap.entrySet()) {
                        Long receiverId = SafeConverter.toLong(entry.getKey());
                        String callName = SafeConverter.toString(entry.getValue());
                        if (!Objects.equals(callName, CallName.其它监护人.name())) {
                            tip = callName + "，" + content;
                        } else {
                            tip = content;
                        }
                        Date expireDate = DateUtils.addDays(new Date(), 2);
                        //要奖励的亲子信过期时间和对应的奖励项一致，如果有多个相同的奖励项，取最早的那个
                        for (ParentRewardLog rewardLog : rewardLogList) {
                            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                            if (item != null) {
                                ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                                if (category != null) {
                                    String itemTitle = getParentRewardDescription(rewardLog, item, category, showScoreLevel);
                                    if (content.contains(itemTitle)) {
                                        expireDate = DateUtils.addDays(rewardLog.getCreateTime(), 2);
                                        break;
                                    }
                                }
                            }
                        }
                        parentRewardService.sendLetter(studentId, receiverId, content, tip, expireDate.getTime());
                    }
                } else {
                    //文案中包含的家长个数
                    int parentCount = 0;
                    for (Map.Entry entry : receiverMap.entrySet()) {
                        String callName = SafeConverter.toString(entry.getValue());
                        Long receiverId = SafeConverter.toLong(entry.getKey());
                        if (content.contains(callName) || "Thank you！I love you！<(￣3￣)>".equals(content)) {
                            parentCount++;
                            parentRewardService.sendLetter(studentId, receiverId, content, content);
                        }
                    }
                    //当文案中没有家长称呼时，说明是发给其它监护人的
                    if (parentCount == 0) {
                        //其他监护人;
                        Set<Long> receiverIds = new HashSet<>();
                        receiverMap.forEach((key, value) -> {
                            String callName = SafeConverter.toString(value, "");
                            if (CallName.其它监护人.name().equals(callName)) {
                                receiverIds.add(key);
                            }
                        });
                        for (Long receiverId : receiverIds) {
                            parentRewardService.sendLetter(studentId, receiverId, content, content);
                        }
                    }
                }
            }

            //清理亲子信箱缓存
            parentRewardService.evictLetterPageCache(studentId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "letterHistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLetterHistory() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = studentDetail.getId();
        int pageIndex = getRequestInt("page", 1);
        try {
            List<ParentRewardLetter> letters = parentRewardLoader.getLettersByStudentId(studentId, pageIndex);
            parentRewardService.clearUnreadLetters(studentId);
            //是否发送过亲子信
            boolean hasSendLetter = !ParentRewardCacheManager.INSTANCE.isStudentNeverSendLetterToday(currentUserId());
            //是否绑定了家长
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            //是否有待发放的奖励
            List<ParentRewardLog> parentRewardLogs = parentRewardLoader.getParentRewardList(studentId, ParentRewardStatus.INIT.getType());
            return MapMessage.successMessage()
                    .add("has_send_letter", hasSendLetter)
                    .add("bind_parent", CollectionUtils.isNotEmpty(studentParentRefs))
                    .add("has_reward", CollectionUtils.isNotEmpty(parentRewardLogs))
                    .add("letters", generateLetterList(letters, studentDetail));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 获取最新的未读亲子信
     */
    @RequestMapping(value = "tip.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTips() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long studentId = studentDetail.getId();
        try {
            RedisLetterWrapper wrapper = parentRewardLoader.getUnreadLetterInfo(studentId);
            if (wrapper != null) {
                User parent = raikouSystem.loadUser(wrapper.getUserId());
                String parent_default_icon = "/public/skin/parentMobile/images/new_icon/avatar_parent_default.png";
                if (parent != null) {
                    wrapper.setImg(getUserAvatarImgUrl(parent));
                } else {
                    wrapper.setImg(getCdnBaseUrlStaticSharedWithSep() + parent_default_icon);
                }
            }
            return MapMessage.successMessage()
                    .add("tip", wrapper);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private List<Map<String, Object>> generateRewardDateList(StudentDetail studentDetail, List<ParentRewardLog> rewardLogs) {
        boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, List<ParentRewardLog>> rewardDateMap = new HashMap<>();
        for (ParentRewardLog rewardLog : rewardLogs) {
            String date = DateUtils.dateToString(rewardLog.getCreateTime(), "yyyy-MM-dd");
            List<ParentRewardLog> rewardLogList = rewardDateMap.get(date);
            if (CollectionUtils.isEmpty(rewardLogList)) {
                rewardLogList = new ArrayList<>();
            }
            rewardLogList.add(rewardLog);
            rewardDateMap.put(date, rewardLogList);
        }

        rewardDateMap.entrySet().stream()
                .sorted((o1, o2) -> {
                    Date date1 = DateUtils.stringToDate(o1.getKey(), "yyyy-MM-dd");
                    Date date2 = DateUtils.stringToDate(o2.getKey(), "yyyy-MM-dd");
                    return date1.compareTo(date2);
                })
                .forEach(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", StringUtils.substring(e.getKey(), 5));
                    List<Map<String, Object>> rewardList = new ArrayList<>();
                    e.getValue().forEach(rewardLog -> {
                        Map<String, Object> rewardMap = new HashMap<>();
                        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                        if (item != null) {
                            ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                            if (category != null) {
                                String title = getParentRewardTitle(rewardLog, item, category, showScoreLevel);
                                String type = item.getType();
                                Integer count = item.getCount();
                                rewardMap.put("title", title);
                                rewardMap.put("type", type);
                                rewardMap.put("count", count);
                                rewardList.add(rewardMap);
                            }
                        }
                    });
                    map.put("rewards", rewardList);
                    list.add(map);
                });
        return list;
    }

    private List<ParentRewardLetterWrapper> generateLetterList(List<ParentRewardLetter> letters, StudentDetail studentDetail) {
        Set<Long> parentIds = letters.stream().filter(e -> Objects.equals(e.getReceiverId(), studentDetail.getId())).map(ParentRewardLetter::getSenderId).collect(Collectors.toSet());
        Map<Long, User> parentMap = userLoaderClient.loadUsers(parentIds);
        List<StudentParentRef> refs = studentLoaderClient.loadStudentParentRefs(studentDetail.getId());
        Map<Long, String> callNameMap = refs.stream().collect(Collectors.toMap(StudentParentRef::getParentId, StudentParentRef::getCallName, (u, v) -> u));
        List<ParentRewardLetterWrapper> wrappers = new ArrayList<>();
        letters.stream().sorted(Comparator.comparing(ParentRewardLetter::getCreateTime)).forEach(e -> {
            ParentRewardLetterWrapper wrapper = new ParentRewardLetterWrapper();
            if (Objects.equals(e.getSenderId(), studentDetail.getId())) {
                wrapper.setImg(getUserAvatarImgUrl(studentDetail));
            } else {
                wrapper.setCallName(callNameMap.get(e.getSenderId()));
                wrapper.setImg(getUserAvatarImgUrl(parentMap.get(e.getSenderId())));
            }
            wrapper.setContent(e.getContent());
            wrapper.setCreateTime(DateUtils.dateToString(e.getCreateTime(), "yyyy-MM-dd HH:mm"));
            wrapper.setSendByStudent(Objects.equals(e.getSenderId(), studentDetail.getId()));
            wrappers.add(wrapper);
        });
        return wrappers;
    }

    /**
     * 获取本学期领取的奖励信息
     */
    private Map<String, Object> getTermRewardInfo(ParentRewardStudentStatistics studentStatistics) {
        Map<String, Object> termMap = new HashMap<>();
        //本学期总共领取的学豆数
        int integral = 0;
        int luckyBag = 0;
        if (studentStatistics != null) {
            Map<String, Integer> data = studentStatistics.getData();
            if (MapUtils.isNotEmpty(data)) {
                for (Map.Entry entry : data.entrySet()) {
                    if ("integral".equals(entry.getKey())) {
                        integral += SafeConverter.toInt(entry.getValue());
                    } else if ("points".equals(entry.getKey())) {
                        //现在正式数据没有points类型。测试数据先排除下
                    } else {
                        luckyBag += SafeConverter.toInt(entry.getValue());
                    }
                }
            }
        }
        termMap.put("integral", integral);
        termMap.put("luckyBag", luckyBag);
        return termMap;
    }

    /**
     * 主形象中按奖励类型分类总结
     * {
     * "INTEGRAL":[
     * "未发放过期",
     * "待发放",
     * "已发放未领取"
     * ],
     * "POINTS":[
     * ]
     * }
     */
    private Map<String, List<ParentRewardSummary>> generateRewardSummary(List<ParentRewardLog> parentRewardLogs, StudentDetail studentDetail) {
        Map<String, List<ParentRewardSummary>> summaryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(parentRewardLogs)) {
            //奖品分类（学豆、学分、宝箱（成长世界的））
            Set<String> itemCategorySet = new HashSet<>();
            for (ParentRewardItemType itemType : ParentRewardItemType.values()) {
                itemCategorySet.add(itemType.getCategory());
            }
            for (String itemCategory : itemCategorySet) {
                List<ParentRewardLog> typeLogs = parentRewardLogs.stream()
                        .filter(rewardLog -> {
                            ParentRewardItemType itemType = ParentRewardItemType.of(rewardLog.getType());
                            return itemType != null && itemType.getCategory().equals(itemCategory);
                        })
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(typeLogs)) {
                    summaryMap.put(itemCategory, generaSummaryList(typeLogs, studentDetail));
                }
            }
        }
        return summaryMap;
    }


    private List<ParentRewardSummary> generaSummaryList(List<ParentRewardLog> parentRewardLogs, StudentDetail studentDetail) {
        List<ParentRewardSummary> summaryList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parentRewardLogs)) {
            //未发放已过期
            List<ParentRewardLog> initExpireList = parentRewardLogs.stream().filter(ParentRewardLog::sendExpired).collect(Collectors.toList());
            generateSummary(summaryList, initExpireList, studentDetail);
            //未发放未过期
            List<ParentRewardLog> initList = parentRewardLogs.stream().filter(ParentRewardLog::sendNotExpired).collect(Collectors.toList());
            generateSummary(summaryList, initList, studentDetail);
            //已发放未过期
            List<ParentRewardLog> sendList = parentRewardLogs.stream().filter(ParentRewardLog::receiveNotExpired).collect(Collectors.toList());
            generateSummary(summaryList, sendList, studentDetail);
            //已发放已过期
            List<ParentRewardLog> sendExpireList = parentRewardLogs.stream().filter(ParentRewardLog::receiveExpired).collect(Collectors.toList());
            generateSummary(summaryList, sendExpireList, studentDetail);

        }
        return summaryList;
    }

    private void generateSummary(List<ParentRewardSummary> summaryList, List<ParentRewardLog> parentRewardLogList, StudentDetail studentDetail) {
        ParentRewardSummary summary = null;
        if (CollectionUtils.isNotEmpty(parentRewardLogList)) {
            summary = new ParentRewardSummary();
            ParentRewardLog rewardLog = parentRewardLogList.stream().findFirst().orElse(null);
            int count = 0;
            String type = rewardLog.getType();

            ParentRewardItemType itemType = ParentRewardItemType.of(type);
            if (itemType != null) {
                summary.setType(itemType.getCategory());
                if ("LUCKY_BAG".equals(itemType.getCategory())) {
                    Set<String> taskIds = new HashSet<>();
                    for (ParentRewardLog log : parentRewardLogList) {
                        Map<String, Object> ext = log.getExt();
                        if (ext != null && ext.get("taskId") != null) {
                            taskIds.add(SafeConverter.toString(ext.get("taskId")));
                        }
                    }
                    summary.setTaskIds(taskIds);
                    count = parentRewardLogList.size();
                } else {
                    for (ParentRewardLog log : parentRewardLogList) {
                        count += log.getCount();
                    }
                }
                if ("FINISH_WONDERLAND_MISSION".equals(rewardLog.getKey())) {
                    summary.setType(itemType.name());
                }
            }

            //如果是待领取未过期，就把奖励记录详情返回给前段，在领取奖励的时候再由前段传给后端
            if (rewardLog.receiveNotExpired()) {
                List<Map<String, Object>> detailList = new ArrayList<>();
                for (ParentRewardLog log : parentRewardLogList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", log.getId());
                    map.put("studentId", log.getStudentId());
                    map.put("key", log.getKey());
                    map.put("type", log.getType());
                    map.put("count", log.getCount());
                    detailList.add(map);
                }
                summary.setRewards(detailList);
                summary.setRewardDateList(generateRewardDateList(studentDetail, parentRewardLogList));
            }

            boolean expire = rewardLog.sendExpired() || rewardLog.receiveExpired();
            summary.setStatus(rewardLog.getStatus());
            summary.setExpire(expire);
            summary.setCount(count);
            //只有未发放未过期的需要这个列表
            if (rewardLog.sendNotExpired()) {
                summary.setItemList(generateRewardItemList(studentDetail, parentRewardLogList));
            }
        }
        if (summary != null) {
            summaryList.add(summary);
        }
    }

    private List<Map<String, Object>> generateRewardItemList(StudentDetail studentDetail, List<ParentRewardLog> rewardLogList) {
        List<Map<String, Object>> itemList = new ArrayList<>();
        List<ParentRewardLog> rewardList = rewardLogList.stream()
                .filter(e -> parentRewardBufferLoaderClient.getParentRewardItem(e.getKey()) != null)
                .sorted(Comparator.comparing(l -> parentRewardBufferLoaderClient.getParentRewardItem(l.getKey()).getRank())) //正序排序
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(rewardList)) {
            boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            //用title来替代key，实现按title分类
            rewardList.forEach(log -> {
                ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(log.getKey());
                if (item != null) {
                    ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                    if (category != null) {
                        String title = getParentRewardTitle(log, item, category, showScoreLevel);
                        log.setTitle(title);
                    }
                }
            });
            Map<String, List<ParentRewardLog>> rewardLogMap = rewardList.stream().collect(Collectors.groupingBy(ParentRewardLog::getTitle));
            rewardLogMap.forEach((key, value) -> {
                Map<String, Object> map = new HashMap<>();
                map.put("title", key);
                map.put("type", IterableUtils.get(value, 0).getType());
                map.put("count", SafeConverter.toInt(IterableUtils.get(value, 0).getCount()));
                map.put("item_count", value.size());
                itemList.add(map);
            });

        }
        return itemList;
    }

    private Map<String, Object> getUnfinishHomework(StudentDetail studentDetail) {
        Map<String, Object> homeworkMap = new HashMap<>();
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false).stream().map(GroupMapper::getId).collect(Collectors.toSet());
        String homeworkId = newHomeworkReportService.fetchStudentNewestUnfinishedHomework(studentDetail.getId(), groupIds);
        if (StringUtils.isNotBlank(homeworkId)) {
            NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
            Date date = DateUtils.addDays(new Date(), -7);
            //只需要七天以内的作业
            if (newHomework != null && newHomework.getCreateAt().after(date)) {
                homeworkMap.put("homework_id", homeworkId);
                homeworkMap.put("homework_type", newHomework.getSubject());
                String ver = getRequestString("app_version");
                homeworkMap.put("version", generateBigVersion(ver, studentDetail));
            }
        }
        return homeworkMap;
    }

    private String generateBigVersion(String ver, StudentDetail studentDetail) {
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "AppH5Release", "testlist")) {
            return "V0_0_0";
        }
        return super.generateBigVersion(ver);
    }
}

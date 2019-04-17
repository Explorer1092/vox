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

package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.IterableUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardBusinessType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardStatus;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardCategory;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.parentreward.api.entity.StudentRewardSendCount;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardLogView;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardSendResult;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCache;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCacheManager;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardHelper;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardScoreLevelCalculator;
import com.voxlearning.utopia.service.user.api.UserBlacklistService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author xinxin
 * @since 5/8/17.
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/reward")
public class ParentRewardApiV2Controller extends AbstractParentApiController {
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = UserBlacklistService.class)
    private UserBlacklistService userBlacklistService;
    @Inject
    private ParentRewardLoaderClient parentRewardLoaderClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;
    private String growthWorldReportConfigKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Mode current = RuntimeMode.current();
        if (current == Mode.DEVELOPMENT) {
            current = Mode.TEST;
        }
        growthWorldReportConfigKey = "growthWorldReportConfig_" + current.name();
    }

    /**
     * 查询学生可发放奖励数据
     */
    @RequestMapping(value = "/query.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage summary() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
        } catch (Exception ex) {
            return failMessage(ex);
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || Objects.equals(getCurrentParentId(), 20001L)) {
            return successMessage().add("show", false);
        }
        try {
            String rewardUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/rewards/detail.vpage?ref=ball&t=1";
            MapMessage result = successMessage()
                    .add("rewardUrl", rewardUrl)
                    .add("couponUrl", ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17my_shell/coupon.vpage?ref=ball");

            List<ParentRewardLog> parentRewardList = parentRewardLoader.getParentRewardList(studentId, ParentRewardStatus.INIT.getType());
            if (CollectionUtils.isEmpty(parentRewardList)) {
                result.add("show", true);
                return result;
            }

            boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            int total = 0;
            List<String> rewardTypes = new ArrayList<>();
            for (ParentRewardLog log : parentRewardList) {
                ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(log.getKey());
                if (null == item) {
                    continue;
                }

                total += log.getCount();
                if (showScoreLevel && StringUtils.isNotBlank(item.getLevelTitle())) {
                    rewardTypes.add(log.realTitle(item.getLevelTitle()));
                } else {
                    rewardTypes.add(log.realTitle(item.getTitle()));
                }
            }
            //多于2条的只传第一条，小于等于2条的都传
            List<String> descriptions = new ArrayList<>();
            if (rewardTypes.size() > 2) {
                descriptions.add(IterableUtils.get(rewardTypes, 0));
            } else {
                descriptions.addAll(rewardTypes);
            }

            return result
                    .add("total", total)
                    .add("count", rewardTypes.size())
                    .add("desc", descriptions)
                    .add("timeAvailable", parentRewardService.rewardSendAvailable(getCurrentParentId(), studentId, isParentRewardNewVersionForFaceDetect(getClientVersion())))
                    .add("show", true);
        } catch (Exception ex) {
            log.error("{},sid:{}", ex.getMessage(), studentId, ex);
            return failMessage("系统异常");
        }
    }

    /**
     * 发放家长奖励给学生
     */
    @RequestMapping(value = "/send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage send() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
        } catch (Exception ex) {
            return failMessage(ex);
        }

        Long studentId = getRequestLong(ApiConstants.REQ_STUDENT_ID);
        if (0 == studentId) {
            return failMessage("无效的学生ID");
        }

        try {
            if (!parentRewardService.rewardSendAvailable(getCurrentParentId(), studentId,isParentRewardNewVersionForFaceDetect(getClientVersion()))) {
                return failMessage("为鼓励家长发放奖励，发奖时间将在每天08:00-16:00开启");
            }

            ParentRewardSendResult rewardSendResult;
            try {
                rewardSendResult = atomicLockManager.wrapAtomic(parentRewardService)
                        .keyPrefix("SEND_STUDENT_REWARD")
                        .keys(studentId)
                        .proxy()
                        .sendParentReward(getCurrentParentId(), studentId, Collections.emptyList());
            } catch (DuplicatedOperationException ex) {
                return failMessage("当前奖励正在被发放,请稍候刷新页面重试");
            }

            if (null == rewardSendResult) {
                return successMessage();
            }

            return successMessage().add("data", rewardSendResult);
        } catch (Exception ex) {
            log.error("{},sid:{},pid:{}", ex.getMessage(), studentId, getCurrentParentId(), ex);
            return failMessage("系统异常");
        }
    }

    /**
     * 学生奖励详情列表 家长奖励-2.5
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getRewardDetail() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
        } catch (Exception ex) {
            return failMessage(ex);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (0 == studentId) {
            return failMessage("无效的学生ID");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("获取学生信息错误");
        }
        try {
            MapMessage message = successMessage();
            String detailUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/rewards/detail.vpage?ref=ball&t=1";
            message.add("detail_url", detailUrl);
            String left_text;
            String right_text = "";
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            long sendCount;
            if (clazz != null) {
                List<Long> studentIds = ParentRewardCache.getPersistenceCache().load(ParentRewardHelper.clazzSentStudentList(clazz.getId()));
                sendCount = CollectionUtils.isEmpty(studentIds) ? 0 : studentIds.size();
                if (sendCount > 5) {
                    right_text = "同班" + sendCount + "位家长已发奖励";
                } else if (sendCount > 0) {
                    Collections.shuffle(studentIds);
                    Long randomStudentId = studentIds.get(0);
                    StudentDetail randomStudent = studentLoaderClient.loadStudentDetail(randomStudentId);
                    right_text = randomStudent.fetchRealname() + "家长已发奖励";
                }

            }
            message.add("right_text", right_text);
            List<ParentRewardLog> parentRewardLogs = parentRewardLoader.getSevenDaysRewardList(studentId);
            //可发放的奖励
            List<ParentRewardLog> initLogs = parentRewardLogs.stream()
                    .filter(ParentRewardLog::sendNotExpired)
                    .collect(Collectors.toList());
            //今天产生的所有奖励
            List<ParentRewardLog> todayLogs = parentRewardLogs.stream().filter(rewardLog -> rewardLog.getCreateTime().after(DayRange.current().getStartDate())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(initLogs)) {
                boolean sendAvailable = parentRewardService.rewardSendAvailable(getCurrentParentId(), studentId,isParentRewardNewVersionForFaceDetect(getClientVersion()));
                if (sendAvailable) {
                    left_text = "*" + studentDetail.fetchRealname() + "的努力值得肯定，赶快发奖给Ta吧~";
                } else {
                    left_text = "*16:00-22:00作业时间不可发放，互动等级达LV3则不限时哦";
                }
                message.add("send_available", sendAvailable);
                List<ParentRewardLogView> rewardList = generateRewardList(studentDetail, initLogs);
                message.add("rewards", rewardList);
            } else {
                boolean sentToday = !ParentRewardCacheManager.INSTANCE.isStudentNeverBeenSentParentReward(studentId);
                left_text = sentToday ? "*今日奖励已发放完毕" : "*今天还没有奖励哦";
            }
            AlpsFuture<Integer> parentRewardStudentRank = parentRewardLoader.getParentRewardStudentRank(studentId);
            int rank = null == parentRewardStudentRank ? 0 : parentRewardStudentRank.getUninterruptibly();
            message.add("rank", rank);
            message.add("left_text", left_text);
            message.add("subject_list", generateSubjectRewardList(todayLogs));
            Set<String> ids = new HashSet<>();
            ids.add(StudentRewardSendCount.generateId(studentId, ParentRewardHelper.currentTermDateRange().getStartDate()));
            Map<String, StudentRewardSendCount> studentRewardSendCountMap = parentRewardLoader.getStudentRewardSendCount(ids);
            long itemCount = studentRewardSendCountMap.values().stream().mapToLong(StudentRewardSendCount::getItemCount).sum();
            //第一次进入首页时给每个家长记录下奖励等级，因为升到3级的时候，每个家长都要有弹窗
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            for (StudentParentRef studentParentRef : studentParentRefs) {
                Long parentId = studentParentRef.getParentId();
                CacheObject<Object> cacheObject = ParentRewardCache.getPersistenceCache().get("PARENT_REWARD_LEVEL_REMIND_" + studentId + "_" + parentId);
                if (cacheObject == null || cacheObject.getValue() == null) {
                    int level = ParentRewardScoreLevelCalculator.getLevel(itemCount);
                    ParentRewardCache.getPersistenceCache().set("PARENT_REWARD_LEVEL_REMIND_" + studentId + "_" + parentId, (int) ((ParentRewardHelper.currentTermDateRange().getEndTime() - Instant.now().toEpochMilli()) / 1000), level);
                }
            }
            message.add("item_count", itemCount);
            Map<String, Object> challenge = parentRewardLoader.getGrowthReportConfigMap(studentId);
            if (MapUtils.isNotEmpty(challenge)) {
                String img = SafeConverter.toString(challenge.get("img"));
                if (StringUtils.isNotBlank(img)) {
                    challenge.put("img", getCdnBaseUrlStaticSharedWithSep() + img);
                }
                String url = SafeConverter.toString(challenge.get("url"));
                if (StringUtils.isNotBlank(url)) {
                    challenge.put("url", ProductConfig.getMainSiteBaseUrl() + url);
                }
                message.add("challenge", challenge);
            }
            return message;
        } catch (Exception ex) {
            log.error("{}, sid:{}, pid:{}", ex.getMessage(), studentId, getCurrentParentId(), ex);
            return failMessage("获取奖励列表失败");
        }
    }

    @RequestMapping(value = "sendreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReward() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_PARENT_REWARD_ID, "奖励ID");
            validateRequired(REQ_PARENT_REWARD_KEY, "奖励KEY");
            validateRequired(REQ_PARENT_REWARD_TYPE, "奖励类型");
            validateRequired(REQ_PARENT_REWARD_COUNT, "奖励数量");
        } catch (Exception ex) {
            return failMessage(ex);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String id = getRequestString(REQ_PARENT_REWARD_ID);
        String key = getRequestString(REQ_PARENT_REWARD_KEY);
        String type = getRequestString(REQ_PARENT_REWARD_TYPE);
        Integer count = getRequestInt(REQ_PARENT_REWARD_COUNT);
        if (getCurrentParentId() == null) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        try {
            if (!parentRewardService.rewardSendAvailable(currentUserId(), studentId,isParentRewardNewVersionForFaceDetect(getClientVersion())) && !"FINISH_WONDERLAND_MISSION".equals(key)) {
                return MapMessage.errorMessage("为鼓励家长发放奖励，发奖时间将在每天08:00-16:00开启");
            }
            ParentRewardLog log = new ParentRewardLog();
            log.setId(id);
            log.setKey(key);
            log.setStudentId(studentId);
            log.setType(type);
            log.setCount(count);
            ParentRewardSendResult sendResult;
            try {
                sendResult = atomicLockManager.wrapAtomic(parentRewardService)
                        .keyPrefix("SEND_STUDENT_REWARD")
                        .keys(studentId)
                        .proxy()
                        .sendParentRewards(getCurrentParentId(), studentId, Collections.singletonList(log));
            } catch (DuplicatedOperationException ex) {
                return failMessage("当前奖励正在被发放，请稍候刷新页面重试");
            }
            if (null == sendResult) {
                return failMessage("奖励发放失败");
            }
            MapMessage message = successMessage().add("sent_by_others", sendResult.getCount() == 0);

            if (sendResult.getCount() > 0) {
                Map<String, Object> popInfo = getActivationInfoForPopup(currentUserId(), studentId);
                message.add("popInfo", popInfo);
            }
            return message;
        } catch (Exception ex) {
            log.error("{}, sid:{}, pid:{}", ex.getMessage(), studentId, getCurrentParentId(), ex);
            return failMessage("发放奖励失败");
        }
    }

    private Map<String, Object> getActivationInfoForPopup(Long parentId, Long studentId) {
        Map<String, Object> popInfo = new HashMap<>();

        Long sendCountToday = washingtonCacheSystem.CBS.persistence.incr("PARENT_SEND_REWARD_COUNT_" + currentUserId(), 1, 1, DateUtils.getCurrentToDayEndSecond());
        if (null != sendCountToday && sendCountToday <= 3) {
            popInfo.put("pop", true);
            popInfo.put("title", "孩子的点滴进步都值得鼓励");
            popInfo.put("count", sendCountToday);
            popInfo.put("action", "发放家长奖励");
            UserActivationLevel parentLevel = userLevelLoader.getParentLevel(parentId);
            if (null != parentLevel) {
                popInfo.put("level", parentLevel.getLevel());
                popInfo.put("levelName", parentLevel.getName());
                popInfo.put("activation", parentLevel.getValue());
                popInfo.put("maxActivation", parentLevel.getLevelEndValue() + 1);
                popInfo.put("minActivation", parentLevel.getLevelStartValue());
                popInfo.put("parentLevelUrl", "/view/mobile/parent/grade/detail.vpage");
                if (parentLevel.getLevel() > 1 && LocalDateTime.now().getHour() >= 8 && LocalDateTime.now().getHour() < 16) {
                    popInfo.put("value", 2L);
                    popInfo.put("desc", "*每日8:00-16:00发奖励可得双倍活跃值");
                } else {
                    popInfo.put("value", 1L);
                }
            }
        } else {
            popInfo.put("pop", false);
        }
        return popInfo;
    }

    private List<Map<String, Object>> generateSubjectRewardList(List<ParentRewardLog> rewardLogs) {
        List<Map<String, Object>> list = new ArrayList<>();
        //奖励分类
        List<String> subjectList = Arrays.asList(Subject.ENGLISH.name(), Subject.MATH.name(), Subject.CHINESE.name(), "EXTRA");
        for (String subject : subjectList) {
            List<ParentRewardLog> subjectLogs = rewardLogs.stream()
                    .filter(rewardLog -> {
                        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                        return item != null && (subject.equals(item.getSubject()) || "EXTRA".equals(subject) && !subjectList.contains(item.getSubject()));
                    }).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("subject", subject);
            map.put("count", subjectLogs.size());
            list.add(map);
        }
        return list;
    }

    private List<ParentRewardLogView> generateRewardList(StudentDetail studentDetail, List<ParentRewardLog> logs) {
        boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        boolean sendAvailable = parentRewardService.rewardSendAvailable(getCurrentParentId(), studentDetail.getId(),isParentRewardNewVersionForFaceDetect(getClientVersion()));
        List<ParentRewardLogView> viewList = new ArrayList<>();
        logs.forEach(log -> {
            ParentRewardLogView view = new ParentRewardLogView();
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(log.getKey());
            if (item != null) {
                ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                if (category != null) {
                    view.setRewardId(log.getId());
                    view.setType(log.getType());
                    view.setCount(log.getCount());
                    view.setItemKey(log.getKey());
                    view.setCategoryRank(category.getRank());
                    view.setCreateTime(log.getCreateTime());
                    view.setSendUrl(item.getSecondaryPageUrl());
                    if ("HOMEWORK".equals(category.getKey())) {
                        if (showScoreLevel && StringUtils.isNotBlank(item.getLevelTitle())) {
                            view.setBusiness(log.realTitle(item.getLevelTitle()));
                        } else {
                            view.setBusiness(log.realTitle(item.getTitle()));
                        }
                    } else {
                        ParentRewardBusinessType businessType = ParentRewardBusinessType.of(item.getBusiness());
                        if (businessType != null) {
                            view.setBusiness(businessType.getValue());
                        }
                    }
                    String icon = item.getIcon();
                    if (showScoreLevel && StringUtils.isNotEmpty(item.getLevelIcon())) {
                        icon = item.getLevelIcon();
                    }
                    view.setIcon(icon);
                    view.setColor(item.getColor());
                    if (!sendAvailable && item.getSendExpireDays() != 0) {
                        view.setSendStartTime(parentRewardService.timeToSendReward());
                    }
                }
                viewList.add(view);
            }
        });
        Comparator<ParentRewardLogView> comparator = Comparator.comparingInt(ParentRewardLogView::getCategoryRank);
        comparator = comparator.thenComparing((o1, o2) -> {
            Date date1 = o1.getCreateTime();
            Date date2 = o2.getCreateTime();
            return date2.compareTo(date1);
        });
        viewList.stream()
                .sorted(comparator)
                .forEach(view -> view.setCreateTimeString(DateUtils.dateToString(view.getCreateTime(), "MM-dd")));
        return viewList;
    }
}

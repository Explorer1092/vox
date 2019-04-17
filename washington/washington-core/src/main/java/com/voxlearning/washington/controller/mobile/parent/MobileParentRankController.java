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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.StudentVipType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.entity.AppParentSignRecord;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 榜单
 * Created by yanling.lan on 2016/01/29.
 */
@Controller
@RequestMapping(value = "/parentMobile/rank")
@Slf4j
public class MobileParentRankController extends AbstractMobileParentController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;

    /**
     * 班级榜单
     * sid String current selected student's id  TODO 微信端可不传
     */
    @RequestMapping(value = "classes.vpage", method = RequestMethod.GET)
    public String classRank(Model model) {

        setRouteParameter(model);

        Long studentId = getRequestLong("sid");
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        model.addAttribute("isGraduate", clazz != null && clazz.isTerminalClazz());

        String pageAddr = "parentmobile/list/classes";

        User user = currentUser();
        if (user == null) {
            model.addAttribute(RES_RESULT, MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE));
            return pageAddr;
        }

        return pageAddr;
    }

    /**
     * 班级榜单-家长动态榜
     */
    @RequestMapping(value = "dynamic.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentDynamicRank() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId <= 0) {
            return MapMessage.errorMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        User student = raikouSystem.loadUser(studentId);
        if (student == null) {
            return MapMessage.errorMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }

        if (!studentIsParentChildren(currentParent().getId(), studentId)) {
            return MapMessage.errorMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT).setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        //送花列表
        List<Map<String, Object>> flowerRank = flowerRank(studentId);
        if (CollectionUtils.isEmpty(flowerRank)) {
            return MapMessage.successMessage()
                    .add("rankList", new ArrayList<>())
                    .add("lastMonthLoginIntegral", 0)
                    .add("hasReceivedLoginReward", true);
        }
        List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        List<Long> studentIdList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toSet()))
                .stream()
                .sorted(Comparator.comparing(GroupStudentTuple::getCreateTime))
                .map(GroupStudentTuple::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        Comparator<Map<String, Object>> c = (a, b) -> ((Long) SafeConverter.toLong(b.get("flowerCount"))).compareTo(SafeConverter.toLong(a.get("flowerCount")));
        c = c.thenComparing((a, b) -> ((Long) SafeConverter.toLong(a.get("lastTime"))).compareTo(SafeConverter.toLong(b.get("lastTime"))));
        c = c.thenComparing((a, b) -> studentIdList.indexOf(SafeConverter.toLong(a.get("studentId"))) - studentIdList.indexOf(SafeConverter.toLong(b.get("studentId"))));
        flowerRank = flowerRank
                .stream()
                .sorted(c)
                .collect(Collectors.toList());

        Set<Long> studentIds = new HashSet<>();
        flowerRank.stream().forEach(p -> studentIds.add(SafeConverter.toLong(p.get(ApiConstants.COMPLETE_STUDENT_ID))));
        //家长孩子关系map
        Map<Long, List<StudentParentRef>> studentParentRef = studentLoaderClient.loadStudentParentRefs(studentIds);

        //榜单里的所有家长id
        Set<Long> parentIds = new HashSet<>();
        //当前学生的所有家长
        Set<Long> currentStudentParents = new HashSet<>();
        //有称呼的
        Map<Long, Set<Long>> hasCallNameParentIdMap = new HashMap<>();
        //无称呼
        Map<Long, Set<Long>> noCallNameParentIdMap = new HashMap<>();
        //获取家长id相关数据
        studentParentRef.forEach((k, v) -> {
            Set<Long> hasCallNameParentSet = v.stream().filter(p -> CallName.of(p.getCallName()) != null).map(StudentParentRef::getParentId).collect(Collectors.toSet());
            Set<Long> notCallNameParentSet = v.stream().filter(p -> CallName.of(p.getCallName()) == null).map(StudentParentRef::getParentId).collect(Collectors.toSet());
            Set<Long> parentSet = v.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
            parentIds.addAll(parentSet);
            hasCallNameParentIdMap.put(k, hasCallNameParentSet);
            noCallNameParentIdMap.put(k, notCallNameParentSet);
            if (k.equals(studentId)) {
                currentStudentParents.addAll(parentSet);
            }
        });

        //学生vip
        //临时去掉VIP
//        Map<Long, StudentVipType> studentVipTypeMap = studentLoaderClient.loadStudentsVipInfo(studentIds);
        //榜单家长app登录次数
        Map<Long, Integer> thisMonthLoginCountMap = new HashMap<>();

        //本月家长签到统计
        List<AppParentSignRecord> appParentSignRecords = vendorLoaderClient.loadAppParentSignRecordByUserIds(parentIds);
        appParentSignRecords.stream()
                .filter(p -> p.hasSigned(MonthRange.current()))
                .forEach(appParentSignRecord -> thisMonthLoginCountMap.put(SafeConverter.toLong(appParentSignRecord.getId()), 1));


        //补充“双倍关注”数据
        for (Map<String, Object> map : flowerRank) {
            Long sid = SafeConverter.toLong(map.get(ApiConstants.COMPLETE_STUDENT_ID));
            Set<Long> studentParentHadCallName = hasCallNameParentIdMap.get(sid);
            Set<Long> studentParentNoCallName = noCallNameParentIdMap.get(sid);
            //当月登录家长的个数
            int currentMonthLoginCount = getStudentLoginParentCount(thisMonthLoginCountMap, studentParentHadCallName, studentParentNoCallName);
            int currentMonthLoginReward = getIntegralByLoginParentCount(currentMonthLoginCount);
            map.put("loginReward", currentMonthLoginReward);
            map.put("isDoubleAttention", currentMonthLoginCount >= 2);
            //危机公关。全部返回NONE
            map.put("isVip", StudentVipType.NONE);
        }
        int lastMonthLoginIntegral = getLastMonthLoginIntegral(currentStudentParents, hasCallNameParentIdMap.get(studentId), noCallNameParentIdMap.get(studentId));


        //家长本月是否签到
        Long parentId = currentParent().getId();
        AppParentSignRecord appParentSignRecord = vendorLoaderClient.loadAppParentRecordByUserId(parentId);
        String ua = getRequest().getHeader("User-Agent");
        Boolean isAPP = StringUtils.isNotBlank(ua) && ua.contains("17Parent");
        return MapMessage.successMessage()
                .add("currentStudentId", studentId)
                .add("currentStudentName", student.fetchRealname())
                .add("rankList", flowerRank)
                .add("lastMonthLoginIntegral", lastMonthLoginIntegral)
                .add("hasReceivedLoginReward", hasReceivedLoginReward(studentId))
                .add("showFlag", true)
                .add("signFlag", thisMonthLoginCountMap.get(parentId) != null)
                .add("lastSignFlag", appParentSignRecord != null && appParentSignRecord.hasSigned(MonthRange.current().previous()))
                .add("isHitDownLoad", !isAPP);
    }

    //动态榜，领取登录奖励
    @RequestMapping(value = "receiveloginreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveLoginReward() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        //当月是否已领取过奖励
        boolean hasReceivedLoginReward = hasReceivedLoginReward(studentId);

        //未领取，则领取上月登录次数对应的奖励学豆
        if (hasReceivedLoginReward) {
            return MapMessage.successMessage("已领取奖励");
        }
        //家长孩子关系map
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        Set<Long> parents = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
        Set<Long> hadCallNameParents = studentParentRefs.stream()
                .filter(p -> CallName.of(p.getCallName()) != null).map(StudentParentRef::getParentId).collect(Collectors.toSet());
        Set<Long> noCallNameParents = studentParentRefs.stream()
                .filter(p -> CallName.of(p.getCallName()) == null).map(StudentParentRef::getParentId).collect(Collectors.toSet());
        int lastMonthLoginIntegral = getLastMonthLoginIntegral(parents, hadCallNameParents, noCallNameParents);

        if (lastMonthLoginIntegral == 0) {
            return MapMessage.errorMessage("没有可领取的奖励").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        IntegralHistory integralHistory = new IntegralHistory();
        integralHistory.setIntegral(lastMonthLoginIntegral);
        integralHistory.setComment("家长登录奖励");
        integralHistory.setIntegralType(IntegralType.家长登录奖励_产品平台.getType());
        integralHistory.setUserId(studentId);
        integralHistory.setUniqueKey(IntegralType.家长登录奖励_产品平台.getDescription() + MonthRange.current());
        MapMessage receiveRewardResult = AtomicLockManager.instance()
                .wrapAtomic(userIntegralService)
                .keyPrefix("receiveLoginReward")
                .keys(studentId, IntegralType.家长登录奖励_产品平台.getType())
                .proxy()
                .changeIntegral(integralHistory);
        if (!receiveRewardResult.isSuccess()) {
            return MapMessage.errorMessage("领取奖励失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        } else {
            return MapMessage.successMessage("领取奖励成功");
        }
    }


    private int getLastMonthLoginIntegral(Set<Long> parentSet, Set<Long> hasCallNameParentSet, Set<Long> noHasCallNameParentSet) {
        //上月登录情况
        Map<Long, Integer> lastMonthLoginCountMap = new HashMap<>();
        MonthRange month = MonthRange.current().previous();
        String lastMonth = month.toString();
        List<AppParentSignRecord> appParentSignRecords = vendorLoaderClient.loadAppParentSignRecordByUserIds(parentSet);
        for (AppParentSignRecord appParentSignRecord : appParentSignRecords) {
            Set<String> keySet = appParentSignRecord.getAppParentSignRecordMap().keySet().stream().map(e -> e.substring(0, 6)).collect(Collectors.toSet());
            if (keySet.contains(lastMonth)) {
                lastMonthLoginCountMap.put(SafeConverter.toLong(appParentSignRecord.getId()), 1);
            }
        }
        int lastMonthLoginParentCount = getStudentLoginParentCount(lastMonthLoginCountMap, hasCallNameParentSet, noHasCallNameParentSet);
        return getIntegralByLoginParentCount(lastMonthLoginParentCount);
    }

    private int getStudentLoginParentCount(Map<Long, Integer> monthLoginCountMap, Set<Long> hasCallNameParentSet, Set<Long> noHasCallNameParentSet) {
        int loginParentCount = 0;
        //有身份的家长登录次数全部统计
        if (CollectionUtils.isNotEmpty(hasCallNameParentSet)) {
            for (Long parentId : hasCallNameParentSet) {
                if (monthLoginCountMap.containsKey(parentId) && monthLoginCountMap.get(parentId) > 0) {
                    loginParentCount += 1;
                }
            }
        }
        //无身份的所有家长全部只算1个
        if (CollectionUtils.isNotEmpty(noHasCallNameParentSet)) {
            for (Long parentId : noHasCallNameParentSet) {
                if (monthLoginCountMap.containsKey(parentId) && monthLoginCountMap.get(parentId) > 0) {
                    loginParentCount += 1;
                    break;
                }
            }
        }
        return loginParentCount;
    }

    private int getIntegralByLoginParentCount(int loginParentCount) {
        if (loginParentCount >= 2) {
            return 20;
        } else if (loginParentCount == 1) {
            return 5;
        } else {
            return 0;
        }
    }

    //是否已领取了当月的登录奖励
    private boolean hasReceivedLoginReward(Long studentId) {

        List<IntegralHistory> integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(studentId);
        //当前月，该类奖励
        IntegralHistory received = integralHistories.stream()
                .filter(p -> p.getIntegralType().equals(IntegralType.家长登录奖励_产品平台.getType()))
                .filter(p -> MonthRange.current().contains(p.getCreatetime()))
                .findFirst().orElse(null);
        //领取过
        return received != null;
    }
}

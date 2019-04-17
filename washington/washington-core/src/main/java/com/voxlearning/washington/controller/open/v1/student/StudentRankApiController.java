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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Student rank list
 * Created by Shuai Huan on 2015/9/17.
 */
@Controller
@RequestMapping(value = "/v1/student")
@Slf4j
public class StudentRankApiController extends AbstractStudentApiController {

    @Inject private RaikouSDK raikouSDK;

    // 土豪榜
    @RequestMapping(value = "silverrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage silverRank() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail student = getCurrentStudentDetail();
        Clazz clazz = student.getClazz();
        if (clazz == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }
        List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                .wrapCache(zoneLoaderClient.getZoneLoader())
                .expiration(1800)
                .keyPrefix("CLAZZ_WEALTHIEST_RANK")
                .keys(clazz.getId(), student.getId())
                .proxy()
                .silverRank(clazz, student.getId());

        for (Map<String, Object> rankMap : rankList) {
            String studentImg = (String) rankMap.get("studentImg");
            rankMap.put("studentImg", getUserAvatarImgUrl(studentImg));
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SILVER_RANK, rankList);
        return resultMap;
    }

    // 学霸榜
    @RequestMapping(value = "smcountrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage smCountRank() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail student = getCurrentStudentDetail();
        Clazz clazz = student.getClazz();
        if (clazz == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }
        List<Map<String, Object>> rankList = washingtonCacheSystem.CBS.flushable
                .wrapCache(zoneLoaderClient.getZoneLoader())
                .expiration(1800)
                .keyPrefix("CLAZZ_SMCOUNT_RANK")
                .keys(clazz.getId(), student.getId())
                .proxy()
                .studyMasterCountRank(clazz, student.getId());

        for (Map<String, Object> rankMap : rankList) {
            String studentImg = (String) rankMap.get("studentImg");
            rankMap.put("studentImg", getUserAvatarImgUrl(studentImg));
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SM_RANK, rankList);
        return resultMap;
    }

    //奖励榜
    @RequestMapping(value = "rewardrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage rewardRank() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail == null || studentDetail.getClazz() == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }

//        List<User> studentList = userAggregationLoaderClient.loadLinkedStudentsByClazzId(studentDetail.getClazzId(), studentDetail.getId());
//        Set<Long> studentIds = new HashSet<>();
//        Map<Long, User> studentMap = new HashMap<>();
//        studentList.stream().forEach(p -> {
//            studentMap.put(p.getId(), p);
//            studentIds.add(p.getId());
//        });
        List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        if (CollectionUtils.isEmpty(studentGroups)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }
        List<Long> studentIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toSet()))
                .stream()
                .sorted(Comparator.comparing(GroupStudentTuple::getCreateTime))
                .map(GroupStudentTuple::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> studentMap = userLoaderClient.loadUsers(studentIds);
        //重新处理一下。把没有查出来的userId丢弃掉。
        studentIds = new ArrayList<>(studentMap.keySet());

        //我的名次
        int myRank = 1;
        int rankIndex = 1;
        List<Map<String, Object>> rewardIntegralRankList = new ArrayList<>();

        //此时的studentIds是学豆为0的学生
        Iterator<Long> it = studentIds.iterator();
        while (it.hasNext()) {
            Long userId = it.next();
            //有历史脏数据。User被disable了。但是跟group的关系还在
            if (studentMap.get(userId) == null) {
                continue;
            }
            if (studentDetail.getId().equals(userId)) {
                myRank = rankIndex;
            }
            Map<String, Object> map = buildRewardRankUser(studentMap.get(userId), rankIndex++);
            rewardIntegralRankList.add(map);
        }

        Map<String, Integer> rewardCountInParentApp = newHomeworkPartLoaderClient.getRewardCountInParentApp(currentUserId());

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("integralRank", rewardIntegralRankList).add("myRank", myRank)
                .add("hasReward", MapUtils.isNotEmpty(rewardCountInParentApp))
                .add("buttonText", MapUtils.isNotEmpty(rewardCountInParentApp) ? "有未领奖励" : "奖励已全部领取");
        return resultMap;
    }

    private Map<String, Object> buildRewardRankUser(User user, int rank) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        map.put("userName", getStudentName(user));
        map.put("userUrl", getUserAvatarImgUrl(user.fetchImageUrl()));
        map.put("integral", 0);
        map.put("rank", rank);
        return map;
    }

    // 星星月份榜
    @RequestMapping(value = "starrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage starRank() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_STAR_RANK_CURRENT_MONTH, rank(studentDetail.getClazzId(), studentDetail.getId()));
        resultMap.add(RES_STAR_RANK_LAST_MONTH, rank(studentDetail.getClazzId(), studentDetail.getId()));
        return resultMap;
    }

    // 星星学期榜
    @RequestMapping(value = "startermrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage starTermRank() {
        MapMessage resultMap = new MapMessage();
        String sys = getRequestString(REQ_SYS);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null) {
            if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_STAR_RANK_CURRENT_TERM, rank(studentDetail.getClazzId(), studentDetail.getId()));
        resultMap.add(RES_STAR_RANK_LAST_TERM, rank(studentDetail.getClazzId(), studentDetail.getId()));
        return resultMap;
    }

    private MapMessage rank(Long clazzId, Long studentId) {

        try {

            List<User> studentList = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazzId, studentId);
            List<Map<String, Object>> studentRankList = new ArrayList<>();
            Map<String, Object> myRank = new HashMap<>();

            for (User user : studentList) {
                Long userId = user.getId();
                Map<String, Object> obj = new HashMap<>();
                obj.put("userId", userId);
                obj.put("userName", getStudentName(user));
                obj.put("userUrl", getUserAvatarImgUrl(user.fetchImageUrl()));
                obj.put("star", 0);
                obj.put("haveReward", false);
                obj.put("homeworkScore", 0);
                obj.put("homeworkDuration", 0);
                obj.put("bindWechatParentCount", 0);
                studentRankList.add(obj);
            }
            int i = 0;
            for (Map<String, Object> obj : studentRankList) {
                Long userId = conversionService.convert(obj.get("userId"), Long.class);
                ++i;
                obj.put("integral", 0);
                obj.put("rank", i);
                if (userId.equals(studentId)) {
                    myRank = obj;
                }
            }
            return MapMessage.successMessage().add("starRank", studentRankList).add("myRank", myRank).add("receivedStarRankReward", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MapMessage.errorMessage("查询班级星星上月排行榜失败").add("clazzId", clazzId).add("studentId", studentId);
    }

}

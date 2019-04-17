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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.SmartClazzRewardItem;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.mapper.SmartClazzRank;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * smart clazz api
 * Created by Shuai Huan on 2015/1/14.
 */
@Controller
@RequestMapping(value = "/v1/teacher/smartclazz")
@Slf4j
public class TeacherSmartClazzApiController extends AbstractTeacherApiController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

    @RequestMapping(value = "/clazz_detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzDetail() {
        MapMessage resultMap = new MapMessage();
        Subject currentSubject = getCurrentSubject();
        try {
            validateRequiredNumber(REQ_CLAZZ_ID, "班级id");
            if (currentSubject == null)
                validateRequest(REQ_CLAZZ_ID);
            else
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher mainTeacher = getCurrentTeacher();
        if (mainTeacher == null || CollectionUtils.isEmpty(mainTeacher.getSubjects()))
            return failMessage(RES_TEACHER_NO_SUBJECT_MSG);

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        List<Subject> teacherAllSubjectInClazz = teacherLoaderClient.findTeacherAllSubjectInClazz(clazzId, mainTeacher.getId());
        resultMap.add(RES_SUBJECT_LIST, toSubjectList(teacherAllSubjectInClazz, true));

        if (currentSubject == null)
            currentSubject = teacherAllSubjectInClazz.stream().sorted(Comparator.comparingInt(Subject::getKey)).findFirst().orElse(null);
        else if (!teacherAllSubjectInClazz.contains(currentSubject))
            return failMessage(RES_TEACHER_CLAZZ_NO_SUBJECT_MSG);

        if (currentSubject == null)
            return failMessage(RES_TEACHER_CLAZZ_NO_SUBJECT_MSG);

        Teacher teacher = getCurrentTeacherBySubject(currentSubject);
        if (teacher == null)
            return failMessage(RES_TEACHER_CLAZZ_NO_SUBJECT_MSG);

        GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);


        if (group == null) {
            return failMessage(RES_CLAZZ_NOT_EXIST_MSG);
        }

        SmartClazzIntegralPool pool = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralPool(group.getId())
                .getUninterruptibly();

        if (pool == null) {
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }

        //查询当月奖励给该班级学生的学豆
        MonthRange monthRange = MonthRange.current();
        List<SmartClazzRank> clazzStudentIntegralList = businessTeacherServiceClient
                .findSmartClazzIntegralHistory(group.getId(), monthRange.getStartDate());

        //剔除无名字的学生
        clazzStudentIntegralList = clazzStudentIntegralList.stream().filter(source ->
                StringUtils.isNotBlank(source.getStudentName())
        ).collect(Collectors.toList());

        resultMap.add(RES_CLAZZ_INTEGRAL, pool.fetchTotalIntegral());
        clazzStudentIntegralList = clazzStudentIntegralList.stream()
                .sorted((o1, o2) -> {
                    String n1 = o1.getStudentName();
                    String n2 = o2.getStudentName();
                    if (StringUtils.equals(n1, n2)) {// 名字相同，按id排序
                        return (o1.getStudentId().compareTo(o2.getStudentId()));
                    }
                    return Collator.getInstance(Locale.CHINESE).compare(n1, n2);// 按拼音排序
                })
                .collect(Collectors.toList());
        List<Map<String, Object>> studentList = new LinkedList<>();
        for (SmartClazzRank rank : clazzStudentIntegralList) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put(RES_USER_ID, rank.getStudentId());
            studentMap.put(RES_REAL_NAME, rank.getStudentName());
            studentMap.put(RES_AVATAR_URL, getUserAvatarImgUrl(rank.getStudentImg()));
            studentMap.put(RES_STUDENT_INTEGRAL, rank.getIntegral());
            studentList.add(studentMap);
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        if (teacherDetail == null)
            return failMessage(RES_TEACHER_NUMBER_ERROR_MSG);
        resultMap.add(RES_TEACHER_INTEGRAL_COUNT, teacherDetail.getUserIntegral().getUsable());
        resultMap.add(RES_STUDENT_LIST, studentList);
        resultMap.add(RES_TEACHER_AUTH_STATE, mainTeacher.fetchCertificationState() == AuthenticationState.SUCCESS);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_CURRENT_SUBJECT, currentSubject.name());
        return resultMap;
    }

    @RequestMapping(value = "/exchange_integral.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage exchangeIntegral() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_CLAZZ_ID, "班级 id");
            validateRequiredNumber(REQ_INTEGRAL_COUNT, "学豆数量");
            Subject currentSubject = getCurrentSubject();
            if (currentSubject == null)
                validateRequest(REQ_CLAZZ_ID, REQ_INTEGRAL_COUNT);
            else
                validateRequest(REQ_CLAZZ_ID, REQ_INTEGRAL_COUNT, REQ_SUBJECT);

        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Integer integralCount = getRequestInt(REQ_INTEGRAL_COUNT);

        if (getCurrentTeacher().fetchCertificationState() != AuthenticationState.SUCCESS) {
            return failMessage(RES_UNAUTHENTICATION_TEACHER_MSG);
        }

        if (integralCount <= 0 || integralCount % 5 != 0) {
            return failMessage(RES_RESULT_CLAZZ_INTEGRAL_COUNT_ERROR_MSG);
        }

        try {
            MapMessage mapMessage = atomicLockManager.wrapAtomic(clazzIntegralService)
                    .keyPrefix("SMARTCLAZZ_EXCHANGE:")
                    .keys(teacher.getSubject(), clazzId)
                    .proxy()
                    .saveSmartClazzExchangeIntegral(teacher, clazzId, integralCount);
            if (mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, convert2OtherMsg(mapMessage.getInfo()));
            }
        } catch (DuplicatedOperationException de) {
            resultMap.add(RES_RESULT, RES_RESULT_DUPLICATE_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (Exception ex) {
            log.error("exchange integral error! clazzId:{},teacherId:{},integral:{}", clazzId, teacher.getId(), integralCount, ex);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
        }

        return resultMap;
    }

    @RequestMapping(value = "/reward_integral.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rewardIntegral() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID_LIST, "学生id");
            validateRequired(REQ_CLAZZ_ID, "班级id");
            validateRequired(REQ_INTEGRAL_COUNT, "学豆数量");
            Subject currentSubject = getCurrentSubject();
            if (currentSubject == null)
                validateRequest(REQ_STUDENT_ID_LIST, REQ_CLAZZ_ID, REQ_INTEGRAL_COUNT);
            else
                validateRequest(REQ_STUDENT_ID_LIST, REQ_CLAZZ_ID, REQ_INTEGRAL_COUNT, REQ_SUBJECT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);

        if (getCurrentTeacher().fetchCertificationState() != AuthenticationState.SUCCESS) {
            return failMessage(RES_UNAUTHENTICATION_TEACHER_MSG);
        }

        if (!teacherLoaderClient.isTeachingClazz(teacher.getId(), clazzId)) {
            return failMessage(RES_RESULT_NOT_BELONG_CLAZZ_MSG);
        }
        int integralCount = getRequestInt(REQ_INTEGRAL_COUNT);
        if (integralCount <= 0)
            return failMessage(RES_RESULT_INTEGRAL_ZERO_MSG);
        String userIdsStr = getRequestString(REQ_STUDENT_ID_LIST);
        if (StringUtils.isBlank(userIdsStr) || userIdsStr.split(",").length == 0)
            return failMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("userIds", userIdsStr);
        jsonMap.put("clazzId", clazzId);
        jsonMap.put("integralCnt", integralCount);
        jsonMap.put("rewardItem", SmartClazzRewardItem.ANSWER_BEST.name());
        jsonMap.put("customContent", SmartClazzRewardItem.ANSWER_BEST.getValue());

        try {
            MapMessage mapMessage = atomicLockManager.wrapAtomic(businessClazzIntegralServiceClient)
                    .keyPrefix("NEWSMARTCLAZZ_REWARD:")
                    .keys(teacher.getSubject(), teacher.getId(), clazzId)
                    .proxy()
                    .rewardSmartClazzStudent(teacher.getId(), raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId), jsonMap);
            if (mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, convert2OtherMsg(mapMessage.getInfo()));
            }
        } catch (DuplicatedOperationException de) {
            resultMap.add(RES_RESULT, RES_RESULT_DUPLICATE_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (Exception ex) {
            log.error("reward integral error!", ex);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
        }

        return resultMap;
    }

    private String convert2OtherMsg(String info) {
        switch (info) {
            case "学豆数量不足，您可以用园丁豆兑换成学豆继续给学生奖励哦":
                return "奖励失败，班级学豆不足！";
            case "园丁豆数量不足":
                return "兑换失败，园丁豆数量不足！";
            default:
                return "系统错误！";
        }
    }


}

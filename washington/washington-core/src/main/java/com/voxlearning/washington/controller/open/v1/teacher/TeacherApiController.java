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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.mapper.SchoolEsQuery;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.school.client.SchoolEsInfoServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Teacher API for App
 * Created by Shuai Huan on 2014/12/26.
 */
@Controller
@RequestMapping(value = "/v1/teacher")
@Slf4j
public class TeacherApiController extends AbstractTeacherApiController {

    @Inject
    private SchoolEsInfoServiceClient schoolEsInfoServiceClient;

    /**
     * 很老很老的老师app版本会调用,直接返回错误
     */
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage oldVersionIndex() {
        return  failMessage(RES_RESULT_VERSION_OLD_ERROR_MSG);
    }


    @RequestMapping(value = "/integral/change.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeUserIntegral() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_INTEGRAL, "学豆数");
            validateRequired(REQ_INTEGRAL_REASON, "原因");
            validateRequired(REQ_INTEGRAL_UNIQUE_KEY, "唯一Key");
            validateRequest(REQ_INTEGRAL, REQ_INTEGRAL_REASON, REQ_INTEGRAL_UNIQUE_KEY);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 验证APP是否允许添加银币
        VendorApps vendorApps = getApiRequestApp();
        if (!"QuestionBank".equals(vendorApps.getAppKey())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APP_NO_ADD_INTEGRAL);
            return resultMap;
        }

        // 判断一下对方的IP是否在IP白名单里面
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            String appClientIp = getWebRequestContext().getRealRemoteAddr();
            if (vendorApps.getServerIps() == null || !vendorApps.getServerIps().contains(appClientIp)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_APP_NO_ADD_INTEGRAL);
                return resultMap;
            }
        }

        // add integral
        Integer integral = getRequestInt(REQ_INTEGRAL);
        Teacher teacher = getCurrentTeacher();
        String integralReason = getRequestString(REQ_INTEGRAL_REASON);
        String uniqueKey = getRequestString(REQ_INTEGRAL_UNIQUE_KEY);
        if (integral < 0) {
            // 判断余额
            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            UserIntegral useable = detail.getUserIntegral();
            if (useable == null || useable.getUsable() + integral < 0) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_NO_USEABLE_MSG);
                return resultMap;
            }
        }

        IntegralHistory integralHistory = new IntegralHistory();
        integralHistory.setUserId(teacher.getId());
        integralHistory.setIntegralType(IntegralType.老师题库改变园丁豆_产品平台.getType());
        integralHistory.setIntegral(integral * 10);
        integralHistory.setUniqueKey(uniqueKey);
        integralHistory.setComment(integralReason);
        integralHistory.setAddIntegralUserId(teacher.getId());
        MapMessage message = userIntegralService.changeIntegral(integralHistory);
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/schoollevels.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getSelectableSchoolLevels() {
        MapMessage resultMap = new MapMessage();

        List<Map<String, Object>> selectableSchoolLevels = new ArrayList<>();
        VendorApps apps = getApiRequestApp();

        List<Map<String, Object>> subjectList = new ArrayList<>();

        // 兼容处理，老版本依然返回学前
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        if (VersionUtil.compareVersion(appVersion, "1.8.0.0") < 0) {

            // 学前 只有英语
            Map<String, Object> infant = getSchoolLevelInfo(SchoolLevel.INFANT);
            subjectList.add(getSubjectInfo(Subject.ENGLISH));
            infant.put("subjects", subjectList);
            selectableSchoolLevels.add(infant);

            // 小学 英语数学语文
            Map<String, Object> primary = getSchoolLevelInfo(SchoolLevel.JUNIOR);
            subjectList = new ArrayList<>();
            subjectList.add(getSubjectInfo(Subject.ENGLISH));
            subjectList.add(getSubjectInfo(Subject.MATH));
            subjectList.add(getSubjectInfo(Subject.CHINESE));
            primary.put("subjects", subjectList);
            selectableSchoolLevels.add(primary);

            // 中学 英语 语文
            Map<String, Object> middle = getSchoolLevelInfo(SchoolLevel.MIDDLE);
            subjectList = new ArrayList<>();
            subjectList.add(getSubjectInfo(Subject.ENGLISH));
//            subjectList.add(getSubjectInfo(Subject.CHINESE));
            middle.put("subjects", subjectList);
            selectableSchoolLevels.add(middle);

        } else {

            // 小学老师APP
            if (juniorTeacherAppOnlineTime() && "17Teacher".equals(apps.getAppKey())) {
                Map<String, Object> primary = getSchoolLevelInfo(SchoolLevel.JUNIOR);
                subjectList.add(getSubjectInfo(Subject.ENGLISH));
                subjectList.add(getSubjectInfo(Subject.MATH));
                subjectList.add(getSubjectInfo(Subject.CHINESE));
                primary.put("subjects", subjectList);
                selectableSchoolLevels.add(primary);
            } else if ("17JuniorTea".equals(apps.getAppKey())) {
                // 中学 英语 数学
                Map<String, Object> middle = getSchoolLevelInfo(SchoolLevel.MIDDLE);
                subjectList = new ArrayList<>();
                subjectList.add(getSubjectInfo(Subject.ENGLISH));
                subjectList.add(getSubjectInfo(Subject.MATH));
                middle.put("subjects", subjectList);
                selectableSchoolLevels.add(middle);
            } else {
                Map<String, Object> primary = getSchoolLevelInfo(SchoolLevel.JUNIOR);
                subjectList.add(getSubjectInfo(Subject.ENGLISH));
                subjectList.add(getSubjectInfo(Subject.MATH));
                subjectList.add(getSubjectInfo(Subject.CHINESE));
                primary.put("subjects", subjectList);
                selectableSchoolLevels.add(primary);
                Map<String, Object> middle = getSchoolLevelInfo(SchoolLevel.MIDDLE);
                subjectList = new ArrayList<>();
                subjectList.add(getSubjectInfo(Subject.ENGLISH));
                middle.put("subjects", subjectList);
                selectableSchoolLevels.add(middle);
            }
        }

        resultMap.add(RES_SCHOOL_LEVEL, selectableSchoolLevels);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/nearschools.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getNearSchools() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_LAT, "纬度");
            validateRequired(REQ_LON, "经度");
            validateRequiredNumber(REQ_APP_MSG_SCHOOL_LEVEL, "学校级别");
            if (hasSessionKey())
                validateRequest(REQ_LAT, REQ_LON, REQ_APP_MSG_SCHOOL_LEVEL);
            validateRequestNoSessionKey(REQ_LAT, REQ_LON, REQ_APP_MSG_SCHOOL_LEVEL);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Double lat = getRequestDouble(REQ_LAT);
        Double lon = getRequestDouble(REQ_LON);
        Integer level = getRequestInt(REQ_APP_MSG_SCHOOL_LEVEL);
        if (lat <= 0d || lon <= 0d ) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "参数错误");
            return resultMap;
        }
        SchoolEsQuery query = new SchoolEsQuery();
        query.setLimit(8);
        query.setPage(1);
        query.setAuthenticationStates(Collections.singleton(1));
        query.setCoordinates(String.format("%s,%s", lat, lon));
        query.setLevels(Collections.singleton(level));
        Page<SchoolEsInfo> schoolEsInfoPage = schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(query);
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("schools", schoolEsInfoPage.getContent());
    }

    /**
     * 根据手机号返回用户ID
     * @return
     */
    @RequestMapping(value = "/getstudentmobile.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudentMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(RES_STUDENT_MOBILE, "学生手机号");
            validateRequest(RES_STUDENT_MOBILE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(getRequestString(RES_STUDENT_MOBILE), UserType.STUDENT);
        if (userAuthentication == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "没有相关用户信息, 请核对");
            return resultMap;
        }
        // 校验学生是否在老师名下
        List<User> students = studentLoaderClient.loadTeacherStudents(currentUserId());
        if (students.isEmpty() || students.stream().filter(s -> Objects.equals(s.getId(), userAuthentication.getId())).count() == 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "老师名下没有该学生, 请核对");
        }
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_STUDENT_ID, userAuthentication.getId());
    }

    /**
     * 中学老师可在APP端查看学生手机号
     * @return
     */
    @RequestMapping(value = "/showStudentMobile.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage showStudentMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_ID, "学生ID");
            validateRequest(REQ_USER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(REQ_USER_ID, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        // 校验学生是否在老师名下
        Teacher teacher = getCurrentTeacher();
        List<User> students = studentLoaderClient.loadTeacherStudents(teacher.getId());
        if (CollectionUtils.isEmpty(students) || students.stream().filter(s -> Objects.equals(s.getId(), getRequestLong(REQ_USER_ID))).findAny() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "找不到该学生的手机号码");
        }

        String mobile = sensitiveUserDataServiceClient.showUserMobile(getRequestLong(REQ_USER_ID), "open api", SafeConverter.toString(teacher.getId()));

        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(REQ_MOBILE, mobile);
    }

    private Map<String, Object> getSchoolLevelInfo(SchoolLevel sl) {
        return MapUtils.m("school_level_id", sl.getLevel(),
                "school_level_name", sl.name(),
                "school_level_desc", sl.getDescription());
    }

    private Map<String, Object> getSubjectInfo(Subject subject) {
        return MapUtils.m("subject_name", subject.name(), "subject_desc", subject.getValue());
    }


}

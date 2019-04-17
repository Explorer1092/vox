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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * User related API controller class.
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
@Controller
@RequestMapping(value = "/v1/clazz")
public class ClazzApiController extends AbstractApiController {

    private static final String CLAZZ_SHARE_KEY = "clazz_share";
    private static final String SYSTEM_CLAZZ_SHARE_KEY = "sys_clazz_share";
    private static final int SHARE_LIVE_TIME = 24 * 60 * 60;

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @RequestMapping(value = "/share.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage userShare() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredAny(REQ_SHARE_TEXT, REQ_SHARE_IMG, "分享的内容或者图片");
            validateRequest(REQ_SHARE_TEXT, REQ_SHARE_TEXT_LINK, REQ_SHARE_IMG, REQ_SHARE_IMG_LINK);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();

        // 判断是否为学生类型
        if (UserType.STUDENT.getType() != curUser.getUserType()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_USER_TYPE_ERROR);
            return resultMap;
        }

        // 判断学生是否有班级
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(curUser.getId());
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_CLASS_ERROR);
            return resultMap;
        }

        // 判断当日是否分享过
        String curDate = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        String memKey = CacheKeyGenerator.generateCacheKey(ClazzApiController.class, CLAZZ_SHARE_KEY, curUser.getId());
        String shareDate = washingtonCacheSystem.CBS.flushable.load(memKey);
        if (shareDate != null && shareDate.equals(curDate)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SHARE_LIMIT_MSG);
            return resultMap;
        }

        VendorApps vendorApps = getApiRequestApp();

        if (!clazz.isSystemClazz()) {// 非系统自建班级
            zoneQueueServiceClient.createClazzJournal(clazz.getId())
                    .withUser(curUser.getId())
                    .withUser(curUser.fetchUserType())
                    .withClazzJournalType(ClazzJournalType.APP_SHARE)
                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                    .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                            "app_name", vendorApps.getCname(),
                            "app_url", "/student/apps/index.vpage?app_key=" + vendorApps.getAppKey(),
                            "share_text", getRequestString(REQ_SHARE_TEXT),
                            "share_text_link", getRequestString(REQ_SHARE_TEXT_LINK),
                            "share_img", getRequestString(REQ_SHARE_IMG),
                            "share_img_link", getRequestString(REQ_SHARE_IMG_LINK))))
                    .withPolicy(JournalDuplicationPolicy.DAILY)
                    .commit();
        } else {// 系统自建班级
            List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(curUser.getId(), false);
            for (GroupMapper group : groups) {
                zoneQueueServiceClient.createClazzJournal(clazz.getId())
                        .withUser(curUser.getId())
                        .withUser(curUser.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.APP_SHARE)
                        .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                                "app_name", vendorApps.getCname(),
                                "app_url", "/student/apps/index.vpage?app_key=" + vendorApps.getAppKey(),
                                "share_text", getRequestString(REQ_SHARE_TEXT),
                                "share_text_link", getRequestString(REQ_SHARE_TEXT_LINK),
                                "share_img", getRequestString(REQ_SHARE_IMG),
                                "share_img_link", getRequestString(REQ_SHARE_IMG_LINK))))
                        .withPolicy(JournalDuplicationPolicy.DAILY)
                        .withGroup(group.getId())
                        .commit();
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        // 保存分享数据，控制分享次数
        washingtonCacheSystem.CBS.flushable.add(memKey, SHARE_LIVE_TIME, curDate);

        return resultMap;
    }

    @RequestMapping(value = "/sysshare.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage systemShare() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequiredAny(REQ_SHARE_TEXT, REQ_SHARE_IMG, "分享的内容或者图片");
            validateRequestNoSessionKey(REQ_CLAZZ_ID, REQ_SHARE_TEXT, REQ_SHARE_TEXT_LINK, REQ_SHARE_IMG, REQ_SHARE_IMG_LINK);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 判断班级ID是否存在
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_CLAZZID_MSG);
            return resultMap;
        }

        // 判断当日是否分享过
        String curDate = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        String memKey = CacheKeyGenerator.generateCacheKey(ClazzApiController.class, SYSTEM_CLAZZ_SHARE_KEY, clazzId);
        String shareDate = washingtonCacheSystem.CBS.flushable.load(memKey);
        if (shareDate != null && shareDate.equals(curDate)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SHARE_LIMIT_MSG);
            return resultMap;
        }

        VendorApps vendorApps = getApiRequestApp();

        // 判断该班级的用户是否加载过该游戏
        // FIXME 暂时注掉吧，对系统性能有比较大的损耗
        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        Map<Long, VendorAppsUserRef> userRefs = vendorLoaderClient.loadVendorAppUserRefs(vendorApps.getAppKey(), studentIds);
        if (userRefs == null || userRefs.size() == 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_CLAZZID_MSG);
            return resultMap;
        }

        zoneQueueServiceClient.createClazzJournal(clazz.getId())
                .withUser(SystemRobot.getInstance().getId())
                .withUser(SystemRobot.getInstance().fetchUserType())
                .withClazzJournalType(ClazzJournalType.APP_SHARE)
                .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                        "app_name", vendorApps.getCname(),
                        "app_url", "/student/apps/index.vpage?app_key=" + vendorApps.getAppKey(),
                        "share_text", getRequestString(REQ_SHARE_TEXT),
                        "share_text_link", getRequestString(REQ_SHARE_TEXT_LINK),
                        "share_img", getRequestString(REQ_SHARE_IMG),
                        "share_img_link", getRequestString(REQ_SHARE_IMG_LINK))))
                .withPolicy(JournalDuplicationPolicy.DAILY)
                .commit();

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        // 保存分享数据，控制分享次数
        washingtonCacheSystem.CBS.flushable.add(memKey, SHARE_LIVE_TIME, curDate);

        return resultMap;
    }

    // 学习圈发送动态
    @RequestMapping(value = "/sharelearningcyclejournal.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shareLearningCycleJournal() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_GROUP_ID, "组ID");
            validateRequiredNumber(REQ_STUDENT_ID, "学生ID");
            validateRequiredNumber(REQ_JOURNAL_TYPE, "动态类型");
            validateRequired(REQ_JOURNAL_AVATAR, REQ_JOURNAL_SHOW_NAME, REQ_JOURNAL_CONTENT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 判断组ID是否存在
        Long groupId = getRequestLong(REQ_GROUP_ID);
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_GROUPID_MSG);
            return resultMap;
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }

        Integer journalTypeInt = getRequestInt(REQ_JOURNAL_TYPE);
        ClazzJournalType journalType = ClazzJournalType.safeParse(journalTypeInt);
        if (journalType == ClazzJournalType.UNKOWN) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_JOURNAL_TYPE_ERROR_MSG);
            return resultMap;
        }
        Map<String, Object> jsonMap = new HashMap<>();
        // 普通文本动态
        jsonMap.put("avatar", getRequestString(REQ_JOURNAL_AVATAR));
        jsonMap.put("show_name", getRequestString(REQ_JOURNAL_SHOW_NAME));
        jsonMap.put("content", getRequestString(REQ_JOURNAL_CONTENT));
        jsonMap.put("link_url", getRequestString(REQ_JOURNAL_LINK_URL));
        // 图文动态
        if (journalType == ClazzJournalType.LEARNING_CYCLE_T2) {
            jsonMap.put("img_urls", getRequestString(REQ_JOURNAL_IMG_URLS));
        }
        // 图片模板动态
        if (journalType == ClazzJournalType.LEARNING_CYCLE_T3) {
            jsonMap.put("img_url", getRequestString(REQ_JOURNAL_IMG_URL));
            jsonMap.put("img_content", getRequestString(REQ_JOURNAL_IMG_CONTENT));
        }

        zoneQueueServiceClient.createClazzJournal(groupMapper.getClazzId())
                .withUser(student.getId())
                .withUser(student.fetchUserType())
                .withClazzJournalType(journalType)
                .withClazzJournalCategory(ClazzJournalCategory.LEARNING_CYCLE)
                .withJournalJson(JsonUtils.toJson(jsonMap))
                .withGroup(groupId)
                .commit();

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/students.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzStudents() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequestNoSessionKey(REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 限制只允许聊天室调用
        VendorApps apps = getApiRequestApp();

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_CLAZZ_ID_MSG);
            return resultMap;
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_CLAZZ_ID_MSG);
            return resultMap;
        }

        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        List<Map<String, Object>> students = new ArrayList<>();
        userLoaderClient.loadUsers(studentIds)
                .values()
                .forEach(student -> {
                    Map<String, Object> studentItem = new LinkedHashMap<>();
                    studentItem.put(RES_USER_ID, student.getId());
                    if ("17Teacher".equals(apps.getAppKey())) {
                        studentItem.put(RES_PASSWORD, "");
                    }
                    studentItem.put(RES_USER_TYPE, student.getUserType());
                    studentItem.put(RES_REAL_NAME, student.getProfile().getRealname());
                    studentItem.put(RES_NICK_NAME, student.getProfile().getNickName());
                    studentItem.put(RES_USER_GENDER, student.getProfile().getGender());
                    studentItem.put(RES_AVATAR_URL, getUserAvatarImgUrl(student));
                    studentItem.put(RES_CLAZZ_ID, clazz.getId());
                    studentItem.put(RES_CLAZZ_NAME, clazz.getClassName());
                    studentItem.put(RES_CLAZZ_LEVEL, clazz.getClassLevel());
                    studentItem.put(RES_SCHOOL_ID, school.getId());
                    studentItem.put(RES_SCHOOL_NAME, school.getCname());

                    students.add(studentItem);
                });
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_CLAZZ_STUDENTS, students);
        return resultMap;
    }

    @RequestMapping(value = "/teachers.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzTeachers() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_CLAZZ_ID_MSG);
            return resultMap;
        }

        List<ClazzTeacher> clazzTeacherList = teacherLoaderClient.loadClazzTeachers(clazzId);
        List<Map<String, Object>> teachers = new ArrayList<>();
        for (ClazzTeacher clazzTeacher : clazzTeacherList) {
            if (clazzTeacher.getTeacher() == null) {
                continue;
            }

            // 返回学科老师ID和姓名
            Map<String, Object> teacherItem = new LinkedHashMap<>();
            teacherItem.put(RES_USER_ID, clazzTeacher.getTeacher().getId());
            teacherItem.put(RES_SUBJECT, clazzTeacher.getTeacher().getSubject().name());
            teacherItem.put(RES_REAL_NAME, clazzTeacher.getTeacher().getProfile().getRealname());

            teachers.add(teacherItem);
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_CLAZZ_TEACHERS, teachers);
        return resultMap;
    }

    @Deprecated
    @RequestMapping(value = "/join.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage joinClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_ID, "班级ID");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequest(REQ_CLAZZ_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 20150818 由于班级体系变更，不再支持这种加入班级的方式，需要升级到新版本
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_CLAZZ_ERROR_MSG);
        return resultMap;

//        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
//        Clazz clazz = clazzLoaderClient.loadClazz(clazzId);
//        if (clazz == null) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_CLAZZ_ID_MSG);
//            return resultMap;
//        }
//
//        User curUser = getApiRequestUser();
//        MapMessage result = clazzServiceClient.joinClazz(curUser, clazzId);
//        if (result.isSuccess()) {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_JOIN_CLAZZ_ERROR_MSG);
//        }
//
//        return resultMap;
    }

}
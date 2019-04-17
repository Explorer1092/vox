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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * 新体系老师布置作业
 *
 * @author xuesong.zhang
 * @since 2016-04-08
 */
@Controller
@RequestMapping(value = "/v1/teacher/new/homework")
@Slf4j
public class TeacherNewHomeworkApiController extends AbstractTeacherApiController {

    @Inject private FeedbackServiceClient feedbackServiceClient;

    /**
     * 老师首页推荐作业内容接口
     */
    @RequestMapping(value = "index/content.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage indexContent() {
        MapMessage resultMap = new MapMessage();

        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        Subject subject = teacher.getSubject();
        switch (subject) {
            case CHINESE:
            case MATH:
            case ENGLISH:
                return successMessage()
                        .add(RES_DESCRIPTION, "更多功能即将开放")
                        .add(RES_IMG_URL, getCdnBaseUrlStaticSharedWithSep() + "resources/app/17teacher/res/firstpage/first_page_default_icon.png");
            default:
                return failMessage("不支持的学科");
        }
    }

    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadClazzList() {
        MapMessage resultMap = new MapMessage();

        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.Normal), true);
            if (message.isSuccess()) {
                SchoolYear schoolYear = SchoolYear.newInstance();
                resultMap.add(RES_TERM, schoolYear.currentTerm().getKey());
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 作业推荐班级列表
     */
    @RequestMapping(value = "recommend/clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadClazzListForRecommend() {
        MapMessage resultMap = new MapMessage();

        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.loadTeacherClazzListForRecommend(teacherDetail, Collections.singleton(NewHomeworkType.Normal), true);
            if (message.isSuccess()) {
                SchoolYear schoolYear = SchoolYear.newInstance();
                resultMap.add(RES_TERM, schoolYear.currentTerm().getKey());
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 获取默认教材
     */
    @RequestMapping(value = "clazzbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadClazzBook() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_CLAZZID_GROUPID_LIST, "班级id与组id列表");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZID_GROUPID_LIST);
            } else {
                validateRequest(REQ_CLAZZID_GROUPID_LIST);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String clazzGroupIds = getRequestString(REQ_CLAZZID_GROUPID_LIST);
        List<String> clazzIdGroupIdList = Arrays.asList(clazzGroupIds.trim().split(","));
        Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
        clazzIdGroupIdList.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                long clazzId = SafeConverter.toLong(strs[0]);
                long groupId = SafeConverter.toLong(strs[1]);
                if (clazzId > 0 && groupId > 0) {
                    clazzIdGroupIdMap.put(clazzId, groupId);
                }
            }
        });

        if (MapUtils.isEmpty(clazzIdGroupIdMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "错误班级id与组id列表");
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadClazzBook(teacher, clazzIdGroupIdMap, false);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                Map<String, Object> clazzBook = (Map<String, Object>) message.get("clazzBook");
                if (MapUtils.isNotEmpty(clazzBook)) {
                    clazzBook.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
                }
                resultMap.add(RES_CLAZZ_BOOK, clazzBook);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 获取教材列表
     */
    @RequestMapping(value = "booklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBookList() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_CLAZZ_LEVEL, "年级");
            validateRequired(REQ_BOOK_TERM, "上下册");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL, REQ_BOOK_TERM);
            } else {
                validateRequest(REQ_CLAZZ_LEVEL, REQ_BOOK_TERM);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        Subject subject = teacher.getSubject();
        if (subject == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "您还没有设置学科及班级，请完成设置后再登录！");
            return resultMap;
        }

        Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);
        Integer term = getRequestInt(REQ_BOOK_TERM);

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        List<NewBookProfile> newBookProfiles = newHomeworkContentServiceClient.loadBooks(teacher, clazzLevel, term);
        if (CollectionUtils.isNotEmpty(newBookProfiles)) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_BOOK_LIST, newBookProfiles);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, "未找到合适的教材");
        }
        return resultMap;
    }

    /**
     * 获取教材单元列表
     */
    @RequestMapping(value = "unitlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadUnitList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材id");
            if (StringUtils.isNotEmpty(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_BOOK_ID, REQ_SUBJECT);
            } else {
                validateRequest(REQ_BOOK_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        Subject subject = teacher.getSubject();
        if (subject == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "您还没有设置学科及班级，请完成设置后再登录！");
            return resultMap;
        }

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        String bookId = getRequestString(REQ_BOOK_ID);

        try {
            MapMessage message = newHomeworkContentServiceClient.loadBookUnitList(bookId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_BOOK, message.get("book"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }

        return resultMap;
    }

    /**
     * 更换教材
     */
    @RequestMapping(value = "changebook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changeBook() {
        MapMessage resultMap = new MapMessage();


        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_CLAZZ_IDS, "班级ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_BOOK_ID, REQ_CLAZZ_IDS);
            } else {
                validateRequest(REQ_BOOK_ID, REQ_CLAZZ_IDS);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String clazzIds = getRequestString(REQ_CLAZZ_IDS);
        String bookId = getRequestString(REQ_BOOK_ID);

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        ChangeBookMapper command = new ChangeBookMapper();
        command.setBooks(bookId);
        command.setClazzs(clazzIds);
        try {
            MapMessage message = newContentServiceClient.getRemoteReference().setClazzBook(teacher, command);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, "更换成功");
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    @RequestMapping(value = "typelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadContentTypeList() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_UNIT_ID, "单元ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_BOOK_ID, REQ_UNIT_ID, REQ_SECTION_IDS);
            } else {
                validateRequest(REQ_BOOK_ID, REQ_UNIT_ID, REQ_SECTION_IDS);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, "请安装新版本App后再使用");
        return resultMap;
    }

    /**
     * NEW HOMEWORK-- 布置作业
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveHomework() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkData = getRequestString(REQ_HOMEWORK_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(homeworkData);
        //添加布置作业ip
        String ip = getWebRequestContext().getRealRemoteAddress();
        jsonMap.put("ip", ip);
        String subject = SafeConverter.toString(jsonMap.get("subject"));
        /**
         * 兼容app安卓老师布置作业清空内容再布置提交数据报15006学科错误
         * 1、非包班制老师，如果提交数据subject为空,重新付值老师的subject。
         * 2、提示更新为登录超时请重新登录。
         */
        Teacher teacher = StringUtils.isBlank(subject) ? getCurrentTeacher() : getCurrentTeacherBySubject(Subject.safeParse(subject));

        if (StringUtils.isBlank(subject)) {
            Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            if (teacherIds.size() == 1) {
                jsonMap.put("subject", teacher.getSubject().name());
            }
        }
        HomeworkSource source = HomeworkSource.newInstance(jsonMap);
        MapMessage message = new MapMessage();
        NewHomeworkType newHomeworkType = NewHomeworkType.of(SafeConverter.toString(source.get("homeworkType")));
        if (NewHomeworkType.Unknown.equals(newHomeworkType)) {
            return message.setSuccess(false).setInfo("没有homeworkType参数。");
        }

        HomeworkTag homeworkTag = HomeworkTag.of(SafeConverter.toString(source.get("homeworkTag")));
        message = newHomeworkServiceClient.assignHomework(teacher, source, HomeworkSourceType.App, newHomeworkType, homeworkTag);
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo() + "(" + message.getErrorCode() + ")");
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_HOMEWORK_ID_List, message.get("homeworkIds"));
            resultMap.add(RES_OFFLINEHOMEWORK_URL, RES_OFFLINEHOMEWORK_URL_VALUE);
            String domain = getWebRequestContext().getWebAppBaseUrl();
            resultMap.add(RES_DOMAIN, domain);
        }
        return resultMap;

    }

    /**
     * NEW HOMEWORK-- 布置作业
     */
    @RequestMapping(value = "examfeedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage examFeedback() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_FEEDBACK_CONTENT, "反馈内容");
            validateRequired(REQ_FEEDBACK_TYPE, "反馈类型");
            validateRequired(REQ_QUESTION_ID, "试题ID");
            validateRequest(REQ_FEEDBACK_CONTENT, REQ_FEEDBACK_TYPE, REQ_QUESTION_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        MapMessage message = new MapMessage();
        try {
            String content = getRequestString(REQ_FEEDBACK_CONTENT);
            String questionId = getRequestString(REQ_QUESTION_ID);
            String type = getRequestString(REQ_FEEDBACK_TYPE);

            ExamFeedback examFeedback = new ExamFeedback();
            examFeedback.setUserId(teacher.getId());
            examFeedback.setQuestionId(questionId);
            examFeedback.setContent(type);
            if (StringUtils.isNotBlank(content)) {
                examFeedback.setContent(type + ":" + content);
            }
            examFeedback.setType(ExamFeedback.fetchExamFeedbackType(4));
            message = feedbackServiceClient.getFeedbackService().persistExamFeedback(examFeedback);
        } catch (Exception ex) {
            logger.error("failed to question feedback, teacher id {}, questionId {}", teacher.getId(), getRequestString(REQ_QUESTION_ID), ex);
            message.setSuccess(false).setInfo(RES_RESULT_SYSTEM_ERROR);
        }
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        }
        return resultMap;

    }

    // 根据班级Ids获取可用学豆最大值
    @RequestMapping(value = "maxic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage calculateMaxIntegralCount() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_IDS, "班级ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_IDS);
            } else {
                validateRequest(REQ_CLAZZ_IDS);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String clazzIds = getRequestString(REQ_CLAZZ_IDS);
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        if (StringUtils.isNotBlank(clazzIds)) {
            Set<Long> cids = Arrays.stream(StringUtils.split(clazzIds, ","))
                    .map(e -> ConversionUtils.toLong(StringUtils.split(e, "_")[0])).collect(Collectors.toSet());
            try {
                Map<String, Object> map = businessTeacherServiceClient
                        .calculateHomeworkMaxIntegralCount(teacherDetail, cids);
                resultMap.add(RES_DEFAULT_INTEGRAL_COUNT, SafeConverter.toInt(map.get("dc")));
                resultMap.add(RES_MAX_INTEGRAL_COUNT, SafeConverter.toInt(map.get("mc")));
                String version = getRequestString(REQ_APP_NATIVE_VERSION);
                if (StringUtils.isBlank(version) || VersionUtil.compareVersion(version, "1.7.5") < 0) {
                    resultMap.add(RES_OVEW_TIME_GIDS, Collections.emptyList());
                } else {
                    resultMap.add(RES_OVEW_TIME_GIDS, map.get("overTimeGids"));
                }
                resultMap.add(RES_LIMIT_TIME, map.get("limitTime"));

                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MAX_DURATION_MINUTES, NewHomeworkConstants.ASSIGN_HOMEWORK_MAX_DURATION_MINUTES);
            } catch (Exception ex) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, ex.getMessage());
            }
        }
        return resultMap;
    }

    // 布置2.0 单元进度
    @RequestMapping(value = "unitprogress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadUnitProgress() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZID_GROUPID_LIST, "班级id与组id列表");
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_UNIT_ID, "单元ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZID_GROUPID_LIST, REQ_BOOK_ID, REQ_UNIT_ID);
            } else {
                validateRequest(REQ_CLAZZID_GROUPID_LIST, REQ_BOOK_ID, REQ_UNIT_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String clazzGroupIds = getRequestString(REQ_CLAZZID_GROUPID_LIST);
        Map<Long, Long> groupIdClazzIdMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(clazzGroupIds)) {
            List<String> clazzIdGroupIdList = Arrays.asList(clazzGroupIds.trim().split(","));
            clazzIdGroupIdList.forEach(str -> {
                String[] strs = str.split("_");
                if (strs.length == 2) {
                    groupIdClazzIdMap.put(SafeConverter.toLong(strs[1]), SafeConverter.toLong(strs[0]));
                }
            });
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String bookId = getRequestString(REQ_BOOK_ID);
        String unitId = getRequestString(REQ_UNIT_ID);
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        // 版本低于1.5.3英语学科不显示单元进度
        // 学前老师不显示单元进度
        if ((Subject.ENGLISH == teacher.getSubject() && VersionUtil.compareVersion(appVersion, "1.5.3") < 0)
                || teacher.isInfantTeacher()) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_RESULTS, Collections.emptyList());
            resultMap.add(RES_KNOWLEDGE_DETAIL_URL, RES_KNOWLEDGE_DETAIL_URL_VALUE);
            String domain = getWebRequestContext().getWebAppBaseUrl();
            resultMap.add(RES_DOMAIN, domain);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadUnitProgress(teacher, groupIdClazzIdMap, unitId, bookId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_RESULTS, message.get("results"));
                resultMap.add(RES_KNOWLEDGE_DETAIL_URL, RES_KNOWLEDGE_DETAIL_URL_VALUE);
                String domain = getWebRequestContext().getWebAppBaseUrl();
                resultMap.add(RES_DOMAIN, domain);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    // 布置2.0 同步习题智能组题
    @RequestMapping(value = "intelligence/question.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadIntelligenceExamQuestion() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_INTELLIGENCE_DATA, "组题条件");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_INTELLIGENCE_DATA);
            } else {
                validateRequest(REQ_INTELLIGENCE_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String intelligenceData = getRequestString(REQ_INTELLIGENCE_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(intelligenceData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "组题条件错误");
            return resultMap;
        }
        String clazzIdGroupIds = SafeConverter.toString(jsonMap.get("clazzid_groupid_list"), "");
        String bookId = SafeConverter.toString(jsonMap.get("book_id"), "");
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"), "");
        String sectionIds = SafeConverter.toString(jsonMap.get("section_ids"), "");
        String algoType = SafeConverter.toString(jsonMap.get("algo_type"), "");
        int difficulty = SafeConverter.toInt(jsonMap.get("difficulty"), 1);
        int questionCount = SafeConverter.toInt(jsonMap.get("question_count"));
        String kpIds = SafeConverter.toString(jsonMap.get("kp_ids"), "");
        String contentTypeIds = SafeConverter.toString(jsonMap.get("content_type_ids"));
        String objectiveConfigId = SafeConverter.toString(jsonMap.get("objective_config_id"));
        String type = SafeConverter.toString(jsonMap.get("type"));

//        if (teacher.getSubject() == Subject.MATH && (difficulty <= 0 || difficulty > 5)) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, "难度错误");
//            return resultMap;
//        }
        if (questionCount < 3 || questionCount > 50) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "题量错误");
            return resultMap;
        }
        if (StringUtils.isBlank(bookId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "教材信息错误");
            return resultMap;
        }

        List<String> sectionIdList = StringUtils.toList(sectionIds, String.class);
        List<String> kpIdList = StringUtils.toList(kpIds, String.class);
        List<Integer> contentTypeIdList = StringUtils.toList(contentTypeIds, Integer.class);

        Set<Long> groupIds = new LinkedHashSet<>();
        List<String> clazzIdGroupIdList = Arrays.asList(clazzIdGroupIds.trim().split(","));
        clazzIdGroupIdList.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                groupIds.add(SafeConverter.toLong(strs[1]));
            }
        });

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient
                    .loadIntelligenceQuestion(teacherDetail, groupIds, sectionIdList, bookId, unitId, algoType, difficulty, questionCount, kpIdList, contentTypeIdList, objectiveConfigId, type);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_ID, message.get("id"));
                resultMap.add(RES_QUESTIONS, message.get("questions"));
                resultMap.add(RES_SECONDS, message.get("seconds"));
                resultMap.add(RES_OBJECTIVE_CONFIG_ID, message.get("objectiveConfigId"));
                resultMap.add(RES_NEWHOMEWORK_OBJECTIVETYPE, message.get("type"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 根据教材，单元，课时(小数或小语)获取教学目标子目标列表
     */
    @RequestMapping(value = "objective/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadObjectiveList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_UNIT_ID, "单元ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_BOOK_ID, REQ_UNIT_ID, REQ_SECTION_IDS);
            } else {
                validateRequest(REQ_BOOK_ID, REQ_UNIT_ID, REQ_SECTION_IDS);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String bookId = getRequestString(REQ_BOOK_ID);
        String unitId = getRequestString(REQ_UNIT_ID);
        String sectionIds = getRequestString(REQ_SECTION_IDS);
        List<String> sectionIdList = Arrays.asList(sectionIds.trim().split(","));
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null || teacher.getSubject() == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        Subject subject = teacher.getSubject();
        if ((subject == Subject.MATH || subject == Subject.CHINESE) && CollectionUtils.isEmpty(sectionIdList)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SECTION_ERROR_MSG);
            return resultMap;
        }

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        try {
            MapMessage message = newHomeworkContentServiceClient.loadObjectiveList(teacherDetail, sectionIdList, unitId, bookId, HomeworkSourceType.App, appVersion);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_OBJECTIVE_LIST, message.get("objectiveList"));
                String domain = getWebRequestContext().getWebAppBaseUrl();
                resultMap.add(RES_DOMAIN, domain);
                resultMap.add(RES_HOMEWORK_URL, RES_HOMEWORK_URL_VALUE);
                resultMap.add(RES_PREVIEW_URL, RES_PREVIEW_URL_VALUE);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 根据子目标和作业形式获取内容
     */
    @RequestMapping(value = "objective/content.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadObjectiveContent() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CONTENT_DATA, "内容数据");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CONTENT_DATA);
            } else {
                validateRequest(REQ_CONTENT_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String contentData = getRequestString(REQ_CONTENT_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(contentData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "内容数据错误");
            return resultMap;
        }
        String clazzIdGroupIds = SafeConverter.toString(jsonMap.get("clazzid_groupid_list"), "");
        Set<Long> groupIdSet = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(clazzIdGroupIds)) {
            List<String> clazzIdGroupIdList = Arrays.asList(clazzIdGroupIds.trim().split(","));
            clazzIdGroupIdList.forEach(str -> {
                String[] strs = str.split("_");
                if (strs.length == 2) {
                    groupIdSet.add(SafeConverter.toLong(strs[1]));
                }
            });
        }
        String sys = getRequestString(REQ_SYS);
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        String bookId = SafeConverter.toString(jsonMap.get("book_id"), "");
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"), "");
        String sections = SafeConverter.toString(jsonMap.get("section_ids"), "");
        List<String> sectionIds = Arrays.asList(StringUtils.split(sections, ","));
        String type = SafeConverter.toString(jsonMap.get("type"), "");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        String objectiveConfigId = SafeConverter.toString(jsonMap.get("objective_config_id"), "");
        int currentPageNum = SafeConverter.toInt(jsonMap.get("page_num"));

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.loadObjectiveContent(teacherDetail, groupIdSet, sectionIds, unitId, bookId, objectiveConfigType, objectiveConfigId, currentPageNum, HomeworkSourceType.App, sys, appVersion);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CONTENT, message.get("content"));
                if (ObjectiveConfigType.LEVEL_READINGS == objectiveConfigType) {
                    String previewUrl = UrlUtils.buildUrlQuery(TopLevelDomain.getHttpsMainSiteBaseUrl() + "/resources/apps/hwh5/levelreadings/V1_0_0/index.html", MapUtils.m("from", "preview"));
                    resultMap.add(RES_PREVIEW_URL, previewUrl);
                }
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    @RequestMapping(value = "copy/clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadSameLevelClazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_HOMEWORK_ID);
            } else {
                validateRequest(REQ_HOMEWORK_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            String homeworkId = getRequestString(REQ_HOMEWORK_ID);
            MapMessage message = newHomeworkContentServiceClient.loadSameLevelClazzList(teacher, homeworkId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    @RequestMapping(value = "copy.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage copyHomework() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HOMEWORK_DATA, "作业内容");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_HOMEWORK_DATA);
            } else {
                validateRequest(REQ_HOMEWORK_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String homeworkData = getRequestString(REQ_HOMEWORK_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(homeworkData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.put(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.put(RES_MESSAGE, "参数错误");
            return resultMap;
        }
        String homeworkId = SafeConverter.toString(jsonMap.get("homeworkId"));
        String endTime = SafeConverter.toString(jsonMap.get("endTime"));
        String groupIds = SafeConverter.toString(jsonMap.get("groupIds"));
        if (StringUtils.isBlank(homeworkId) || StringUtils.isBlank(endTime) || StringUtils.isBlank(groupIds)) {
            resultMap.put(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.put(RES_MESSAGE, "参数错误");
            return resultMap;
        }
        List<Long> groupIdList = StringUtils.toLongList(groupIds);
        if (CollectionUtils.isEmpty(groupIdList)) {
            resultMap.put(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.put(RES_MESSAGE, "班组ids错误");
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        try {
            String startTime = DateUtils.nowToString();
            MapMessage message = newHomeworkServiceClient.copyHomework(teacher, homeworkId, groupIdList, startTime, endTime, HomeworkSourceType.App);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, "布置成功");
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 趣味配音专辑
     */
    @RequestMapping(value = "dubbing/albumlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadDubbingAlbumList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CLAZZ_LEVEL, "年级");
            validateRequest(REQ_CLAZZ_LEVEL);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        int clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);
        if (clazzLevel < 1 || clazzLevel > 6) {
            return MapMessage.errorMessage("年级错误");
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadDubbingAlbumList(clazzLevel);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CHANNEL_LIST, message.get("channelList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 趣味配音推荐搜索词
     */
    @RequestMapping(value = "dubbing/recommendsearchwords.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadDubbingRecommendSearchWords() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadDubbingRecommendSearchWords();
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_WORDS, message.get("words"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 查询趣味配音
     */
    @RequestMapping(value = "dubbing/search.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage searchDubbing() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SEARCH_DATA, "查询条件");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_SEARCH_DATA);
            } else {
                validateRequest(REQ_SEARCH_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String searchData = getRequestString(REQ_SEARCH_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(searchData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "查询条件错误");
            return resultMap;
        }
        int clazzLevel = SafeConverter.toInt(jsonMap.get("clazz_level"));
        String searchWord = SafeConverter.toString(jsonMap.get("search_word"), "");
        String channelId = SafeConverter.toString(jsonMap.get("channel_id"), "");
        String albumId = SafeConverter.toString(jsonMap.get("album_id"), "");
        String themeId = SafeConverter.toString(jsonMap.get("theme_id"), "");
        String bookId = SafeConverter.toString(jsonMap.get("book_id"), "");
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"), "");
        int pageNum = SafeConverter.toInt(jsonMap.get("page_num"), 1);
        int pageSize = SafeConverter.toInt(jsonMap.get("page_size"), 10);
        String type = SafeConverter.toString(jsonMap.get("type"), "DUBBING");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            List<String> channelIdList = StringUtils.isBlank(channelId) ? Collections.EMPTY_LIST : Collections.singletonList(channelId);
            List<String> albumIdList = StringUtils.isBlank(albumId) ? Collections.EMPTY_LIST : Collections.singletonList(albumId);
            List<String> themeIdList = StringUtils.isBlank(themeId) ? Collections.EMPTY_LIST : Collections.singletonList(themeId);
            MapMessage message = newHomeworkContentServiceClient.searchDubbing(teacherDetail, clazzLevel, searchWord, channelIdList, albumIdList, themeIdList, bookId, unitId, pageable, objectiveConfigType);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_DUBBING_LIST, message.get("dubbingList"));
                resultMap.add(RES_TOTAL_SZIE, message.get("totalSize"));
                resultMap.add(RES_PAGE_COUNT, message.get("pageCount"));
                resultMap.add(RES_PAGE_NUM, message.get("pageNum"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }


    /**
     * 趣味配音详情
     */
    @RequestMapping(value = "dubbing/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadDubbingDetail() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_UNIT_ID, "单元ID");
            validateRequired(REQ_DUBBING_ID, "配音ID");
            validateRequired(REQ_DUBBING_TYPE, "作业形式");
            validateRequest(REQ_BOOK_ID, REQ_UNIT_ID, REQ_DUBBING_ID, REQ_DUBBING_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject(Subject.ENGLISH);
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        String bookId = getRequestString(REQ_BOOK_ID);
        String unitId = getRequestString(REQ_UNIT_ID);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        String type = SafeConverter.toString(getRequestString(REQ_DUBBING_TYPE), "DUBBING");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.loadDubbingDetail(teacherDetail, bookId, unitId, dubbingId, objectiveConfigType);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_DUBBING_DETAIL, message.get("dubbingDetail"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 收藏趣味配音
     */
    @RequestMapping(value = "dubbing/collection.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage collectDubbing() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_DUBBING_ID, "配音ID");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_DUBBING_ID);
            } else {
                validateRequest(REQ_DUBBING_ID);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            teacher = getCurrentTeacher();
        }
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }

        String dubbingId = getRequestString(REQ_DUBBING_ID);

        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkServiceClient.collectDubbing(teacherDetail, dubbingId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 趣味配音：我的收藏
     */
    @RequestMapping(value = "dubbing/collection/record.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadDubbingCollectionRecord() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_COLLECTION_DATA, "配音收藏参数");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_COLLECTION_DATA);
            } else {
                validateRequest(REQ_COLLECTION_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            teacher = getCurrentTeacher();
        }
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String collectionData = getRequestString(REQ_COLLECTION_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(collectionData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "配音收藏参数错误");
            return resultMap;
        }
        String sys = getRequestString(REQ_SYS);
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        String bookId = SafeConverter.toString(jsonMap.get("book_id"), "");
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"), "");
        int pageNum = SafeConverter.toInt(jsonMap.get("page_num"), 1);
        int pageSize = SafeConverter.toInt(jsonMap.get("page_size"), 10);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.loadDubbingCollectionRecord(teacherDetail, bookId, unitId, pageable, sys, appVersion);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_DUBBING_LIST, message.get("dubbingList"));
                resultMap.add(RES_TOTAL_SZIE, message.get("totalSize"));
                resultMap.add(RES_PAGE_COUNT, message.get("pageCount"));
                resultMap.add(RES_PAGE_NUM, message.get("pageNum"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 新绘本主题列表
     */
    @RequestMapping(value = "picturebookplus/topiclist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadPictureBookPlusTopicList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadPictureBookPlusTopicList();
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_TOPIC_LIST, message.get("topicList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 新绘本系列列表
     */
    @RequestMapping(value = "picturebookplus/serieslist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadPictureBookPlusSeriesList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadPictureBookPlusSeriesList();
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_SERIES_LIST, message.get("seriesList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 新绘本阅读推荐搜索词
     */
    @RequestMapping(value = "picturebookplus/recommendsearchwords.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadPictureBookPlusRecommendWords() {
        MapMessage resultMap = new MapMessage();
        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            if (Subject.CHINESE == teacher.getSubject()) {
                return successMessage().add(RES_WORDS, Collections.emptyList());
            }
            MapMessage message = newHomeworkContentServiceClient.loadPictureBookPlusRecommendSearchWords();
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_WORDS, message.get("words"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 新绘本阅读搜索
     */
    @RequestMapping(value = "picturebookplus/search.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage searchPictureBookPlus() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SEARCH_DATA, "查询条件");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_SEARCH_DATA);
            } else {
                validateRequest(REQ_SEARCH_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            teacher = getCurrentTeacher();
        }
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String searchData = getRequestString(REQ_SEARCH_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(searchData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "查询条件错误");
            return resultMap;
        }
        String sys = getRequestString(REQ_SYS);
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        String clazzLevel = SafeConverter.toString(jsonMap.get("clazz_level"));
        String topicIds = SafeConverter.toString(jsonMap.get("topic_ids"), "");
        String seriesIds = SafeConverter.toString(jsonMap.get("series_ids"), "");
        String searchWord = SafeConverter.toString(jsonMap.get("search_word"), "");
        String bookId = SafeConverter.toString(jsonMap.get("book_id"), "");
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"), "");
        int pageNum = SafeConverter.toInt(jsonMap.get("page_num"), 1);
        int pageSize = SafeConverter.toInt(jsonMap.get("page_size"), 10);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);

        if (noAccessPermission(teacher)) {
            return failMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            List<String> topicIdList = Arrays.stream(topicIds.trim().split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            List<String> seriesIdList = Arrays.stream(seriesIds.trim().split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            MapMessage message = newHomeworkContentServiceClient.searchPictureBookPlus(teacherDetail, clazzLevel, topicIdList, seriesIdList, searchWord, bookId, unitId, pageable, sys, appVersion);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_PICTURE_BOOK_LIST, message.get("pictureBookList"));
                resultMap.add(RES_TOTAL_SZIE, message.get("totalSize"));
                resultMap.add(RES_PAGE_COUNT, message.get("pageCount"));
                resultMap.add(RES_PAGE_NUM, message.get("pageNum"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 新绘本阅读布置历史
     */
    @RequestMapping(value = "picturebookplus/history.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadPictureBookPlusHistory() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_HISTORY_DATA, "布置历史参数");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_HISTORY_DATA);
            } else {
                validateRequest(REQ_HISTORY_DATA);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            teacher = getCurrentTeacher();
        }
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        String historyData = getRequestString(REQ_HISTORY_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(historyData);
        if (MapUtils.isEmpty(jsonMap)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "布置历史参数错误");
            return resultMap;
        }
        String sys = getRequestString(REQ_SYS);
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        String bookId = SafeConverter.toString(jsonMap.get("book_id"), "");
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"), "");
        int pageNum = SafeConverter.toInt(jsonMap.get("page_num"), 1);
        int pageSize = SafeConverter.toInt(jsonMap.get("page_size"), 10);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.loadPictureBookPlusHistory(teacherDetail, bookId, unitId, pageable, sys, appVersion);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_PICTURE_BOOK_LIST, message.get("pictureBookList"));
                resultMap.add(RES_TOTAL_SZIE, message.get("totalSize"));
                resultMap.add(RES_PAGE_COUNT, message.get("pageCount"));
                resultMap.add(RES_PAGE_NUM, message.get("pageNum"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.get("info"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SYSTEM_ERROR);
        }
        return resultMap;
    }

    /**
     * 口语交际推荐搜索词
     */
    @RequestMapping(value = "oralcommunication/recommendsearchwords.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadOralCommunicationRecommendSearchWords() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            return failMessage(RES_NOT_CLAZZ_TEACHER_MSG);
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadOralCommunicationSearchWords();
            if (message.isSuccess()) {
                return successMessage().add(RES_WORDS, message.get("words"));
            } else {
                return failMessage(SafeConverter.toString(message.get("info")));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return failMessage(RES_RESULT_INTERNAL_ERROR_CODE, RES_RESULT_SYSTEM_ERROR);
        }
    }

    /**
     * 口语交际搜索
     */
    @RequestMapping(value = "oralcommunication/search.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage searchOralCommunication() {
        try {
            validateRequired(REQ_SEARCH_DATA, "查询条件");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_SEARCH_DATA);
            } else {
                validateRequest(REQ_SEARCH_DATA);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            teacher = getCurrentTeacher();
        }
        if (teacher == null) {
            return failMessage(RES_NOT_CLAZZ_TEACHER_MSG);
        }
        String searchData = getRequestString(REQ_SEARCH_DATA);
        Map<String, Object> jsonMap = JsonUtils.fromJson(searchData);
        if (MapUtils.isEmpty(jsonMap)) {
            return failMessage("查询条件错误");
        }
        String clazzLevel = SafeConverter.toString(jsonMap.get("clazz_level"));
        String type = SafeConverter.toString(jsonMap.get("type"));
        String searchWord = SafeConverter.toString(jsonMap.get("search_word"));
        String bookId = SafeConverter.toString(jsonMap.get("book_id"));
        String unitId = SafeConverter.toString(jsonMap.get("unit_id"));
        int pageNum = SafeConverter.toInt(jsonMap.get("page_num"), 1);
        int pageSize = SafeConverter.toInt(jsonMap.get("page_size"), 10);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.searchOralCommunication(teacherDetail, clazzLevel, type, searchWord, bookId, unitId, pageable);
            if (message.isSuccess()) {
                MapMessage resultMap = successMessage();
                resultMap.add(RES_ORAL_COMMUNICATION_LIST, message.get("oralCommunicationList"));
                resultMap.add(RES_TOTAL_SZIE, message.get("totalSize"));
                resultMap.add(RES_PAGE_COUNT, message.get("pageCount"));
                resultMap.add(RES_PAGE_NUM, message.get("pageNum"));
                return resultMap;
            } else {
                return failMessage(SafeConverter.toString(message.get("info")));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return failMessage(RES_RESULT_INTERNAL_ERROR_CODE, RES_RESULT_SYSTEM_ERROR);
        }
    }

    /**
     * 口语交际详情
     */
    @RequestMapping(value = "oralcommunication/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadOralCommunicationDetail() {
        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_UNIT_ID, "单元ID");
            validateRequired(REQ_ORAL_COMMUNICATION_ID, "口语交际ID");
            validateRequest(REQ_BOOK_ID, REQ_UNIT_ID, REQ_ORAL_COMMUNICATION_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        Teacher teacher = getCurrentTeacherBySubject(Subject.ENGLISH);
        if (teacher == null) {
            return failMessage(RES_NOT_CLAZZ_TEACHER_MSG);
        }
        String bookId = getRequestString(REQ_BOOK_ID);
        String unitId = getRequestString(REQ_UNIT_ID);
        String oralCommunicationId = getRequestString(REQ_ORAL_COMMUNICATION_ID);
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage message = newHomeworkContentServiceClient.loadOralCommunicationDetail(teacherDetail, bookId, unitId, oralCommunicationId);
            if (message.isSuccess()) {
                return successMessage().add(RES_ORAL_COMMUNICATION_DETAIL, message.get("oralCommunicationDetail"));
            } else {
                return failMessage(SafeConverter.toString(message.get("info")));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return failMessage(RES_RESULT_INTERNAL_ERROR_CODE, RES_RESULT_SYSTEM_ERROR);
        }
    }

    /**
     * 预览口语交际详情
     *
     * @return
     */
    @RequestMapping(value = "oralcommunication/preview.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage previewOralCommunicationDetail() {
        try {
            validateRequired(REQ_STONE_ID, "情景包id");
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            teacher = getCurrentTeacher();
        }
        if (teacher == null) {
            return failMessage(RES_NOT_CLAZZ_TEACHER_MSG);
        }
        String stoneId = getRequestString(REQ_STONE_ID);
        return successMessage()
                .add(RES_RESULT_PACKAGE_INFO, oralCommunicationClient.getHomeworkStonDetaiInfo(stoneId))
                .add(REQ_ORAL_COMMUNICATION_SINGLE_SCORE, newHomeworkContentServiceClient.loadVoxOralCommunicationSingleLevel(null));
    }
}

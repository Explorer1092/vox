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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * @author guoqiang.li
 * @since 2016/11/21
 */
@Controller
@RequestMapping(value = "/v1/teacher/termreview")
public class TeacherTermReviewApiController extends AbstractTeacherApiController {
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
            MapMessage message = newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.TermReview), true);
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
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
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
                Map<String, Object> newClazzBookMap = new LinkedHashMap<>();
                if (MapUtils.isNotEmpty(clazzBook)) {
                    newClazzBookMap.put("bookId", clazzBook.get("bookId"));
                    newClazzBookMap.put("bookName", clazzBook.get("bookName"));
                }
                resultMap.add(RES_CLAZZ_BOOK, newClazzBookMap);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
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

        List<NewBookProfile> newBookProfiles = newHomeworkContentServiceClient.loadBooks(teacher, clazzLevel, term);
        // 过滤期末复习不支持的教材
        if (CollectionUtils.isNotEmpty(newBookProfiles)) {
            newBookProfiles = newBookProfiles.stream()
                    .filter(book -> !NewHomeworkConstants.TERM_REVIEW_NOT_SUPPORTED_BOOK_SERIES.contains(book.getSeriesId()))
                    .collect(Collectors.toList());
        }
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
     * 获取教材下不同类型的作业
     */
    @RequestMapping(value = "typelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadContentTypeList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_CLAZZ_IDS, "班级ids");
            validateRequest(REQ_BOOK_ID, REQ_CLAZZ_IDS);
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
        List<Long> groupIds = new ArrayList<>();
        String clazzGroupIdStr = getRequestString(REQ_CLAZZ_IDS);
        if (StringUtils.isNotBlank(clazzGroupIdStr)) {
            List<String> clazzGroupIds = StringUtils.toList(clazzGroupIdStr, String.class);
            if (CollectionUtils.isNotEmpty(clazzGroupIds)) {
                for (String clazzGroupId : clazzGroupIds) {
                    String[] arr = clazzGroupId.split("_");
                    if (arr.length == 2) {
                        groupIds.add(SafeConverter.toLong(arr[1]));
                    }
                }
            }
        }
        String bookId = getRequestString(REQ_BOOK_ID);
        if (NewHomeworkConstants.TERM_REVIEW_NOT_SUPPORTED_BOOK_SERIES.contains(bookId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_SUPPORT_BOOK_MSG);
            return resultMap;
        }
        try {
            Subject subject;
            NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            if (newBookProfile == null || newBookProfile.getSubjectId() == null) {
                subject = teacher.getSubject();
            } else {
                subject = Subject.fromSubjectId(newBookProfile.getSubjectId());
            }
            String sys = getRequestString(REQ_SYS);
            String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
            MapMessage message = newHomeworkContentServiceClient.loadTermReviewContentTypeList(subject, bookId, groupIds, false, getCdnBaseUrlStaticSharedWithSep(), teacher, sys, appVersion);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CONTENT_TYPE_LIST, message.get("contentTypes"));
                resultMap.add(RES_DOMAIN, getWebRequestContext().getWebAppBaseUrl());
                resultMap.add(RES_HOMEWORK_URL, RES_TERMREVIEW_URL_VALUE);
                resultMap.add(RES_PREVIEW_URL, RES_PREVIEW_URL_VALUE);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DATA_PROCESSING_MSG);
            return resultMap;
        }
    }

    /**
     * 作业内容
     */
    @RequestMapping(value = "content.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadContent() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CONTENT_TYPE, "类型");
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_CLAZZ_IDS, "班级ids");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CONTENT_TYPE, REQ_BOOK_ID, REQ_CLAZZ_IDS);
            } else {
                validateRequest(REQ_CONTENT_TYPE, REQ_BOOK_ID, REQ_CLAZZ_IDS);
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

        String bookId = getRequestString(REQ_BOOK_ID);
        if (NewHomeworkConstants.TERM_REVIEW_NOT_SUPPORTED_BOOK_SERIES.contains(bookId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_SUPPORT_BOOK_MSG);
            return resultMap;
        }

        List<Long> groupIds = new ArrayList<>();
        String clazzGroupIdStr = getRequestString(REQ_CLAZZ_IDS);
        if (StringUtils.isNotBlank(clazzGroupIdStr)) {
            List<String> clazzGroupIds = StringUtils.toList(clazzGroupIdStr, String.class);
            if (CollectionUtils.isNotEmpty(clazzGroupIds)) {
                for (String clazzGroupId : clazzGroupIds) {
                    String[] arr = clazzGroupId.split("_");
                    if (arr.length == 2) {
                        groupIds.add(SafeConverter.toLong(arr[1]));
                    }
                }
            }
        }

        String type = getRequestString(REQ_CONTENT_TYPE);
        try {
            TermReviewContentType termReviewContentType = TermReviewContentType.of(type);
            MapMessage message = newHomeworkContentServiceClient.loadTermReviewContent(
                    teacher,
                    groupIds,
                    bookId,
                    termReviewContentType);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_PACKAGES, message.get(RES_PACKAGES));
                resultMap.add(RES_BASIC, message.get(RES_BASIC));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    /**
     * 作业预览
     */
    @RequestMapping(value = "basicreview/preview.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage preview() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CONTENT_TYPES, "温习内容");
            validateRequired(REQ_BOOK_ID, "教材id");
            if (StringUtils.isNotBlank(getRequestString(REQ_SUBJECT))) {
                validateRequest(REQ_SUBJECT, REQ_CONTENT_TYPES, REQ_BOOK_ID);
            } else {
                validateRequest(REQ_CONTENT_TYPES, REQ_BOOK_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e.getMessage());
        }
        String bookId = getRequestString(REQ_BOOK_ID);
        if (StringUtils.isBlank(bookId)) {
            return failMessage("教材id错误");
        }
        String contentTypes = getRequestString(REQ_CONTENT_TYPES);
        List<String> contentTypeList = StringUtils.toList(contentTypes, String.class);
        if (CollectionUtils.isEmpty(contentTypeList)) {
            return failMessage("温习内容错误");
        }
        Subject subject = Subject.ofWithUnknown(getRequestString(REQ_SUBJECT));
        try {
            MapMessage message = newHomeworkContentServiceClient.previewBasicReviewContent(bookId, contentTypeList, getCdnBaseUrlStaticSharedWithSep());
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_TYPES, message.get(RES_TYPES));
                resultMap.add(RES_CONTENTS, message.get(RES_CONTENTS));
                if (subject.equals(Subject.MATH)) {
                    resultMap.add(RES_PREVIEW_URL,getWebRequestContext().getWebAppBaseUrl() + "/view/homeworkv5/previewmental");
                } else if (subject.equals(Subject.CHINESE)) {
                    resultMap.add(RES_PREVIEW_URL,getWebRequestContext().getWebAppBaseUrl() + "/view/termreview/chinese/preview_read_recite");
                }
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 作业布置
     */
    @RequestMapping(value = "basicreview/assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage assign() {
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
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        HomeworkSource source = HomeworkSource.newInstance(jsonMap);
        try {
            MapMessage message = newHomeworkServiceClient.assignBasicReviewHomework(teacher, source, HomeworkSourceType.App);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, "布置成功");
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }
}

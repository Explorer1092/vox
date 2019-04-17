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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.piclisten.client.AsyncPiclistenCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.alps.annotation.meta.Subject.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK_ID;

/**
 * 用户自学相关API接口
 * Created by Alex on 14-10-20.
 */
@Controller
@RequestMapping(value = "/v1/user/selfstudy")
public class UserSelfStudyApiController extends AbstractApiController {

    @Inject
    private AsyncPiclistenCacheServiceClient asyncPiclistenCacheServiceClient;

    @RequestMapping(value = "/books.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserBooks() {
        MapMessage resultMap = new MapMessage();

        try {
            if (!StringUtils.isEmpty(getRequestString(REQ_STUDENT_ID))) {
                validateRequiredNumber(REQ_STUDENT_ID, "学生ID");
                validateRequest(REQ_STUDENT_ID);
            } else
                validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User user = getApiRequestUser();
        if (user == null)
            return noUserResult;
        StudentDetail student = null;
        if (UserType.PARENT == user.fetchUserType()) {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            if (studentId != 0L) {
                Boolean isRightRef = checkStudentParentRef(studentId, user.getId());
                if (!isRightRef) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }
                student = studentLoaderClient.loadStudentDetail(studentId);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return resultMap;
            }
        } else if (UserType.STUDENT == user.fetchUserType()) {
            student = studentLoaderClient.loadStudentDetail(user.getId());
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            return resultMap;
        }
        List<Map<String, Object>> books = businessStudentServiceClient.getStudentSelfStudyDefaultBooks(student);

        // KEY名转换
        List<Map<String, Object>> retBooks = new ArrayList<>();
        for (Map<String, Object> item : books) {
            Map<String, Object> bookInfo = new LinkedHashMap<>();
            bookInfo.put(RES_BOOK_SUBJECT, item.get("bookSubject"));
            bookInfo.put(RES_BOOK_ID, item.get("bookId"));
            bookInfo.put(RES_CNAME, item.get("bookName"));
            bookInfo.put(RES_BOOK_CLASS_LEVEL, item.get("classLevel"));
            bookInfo.put(RES_BOOK_COLOR, item.get("color"));
            bookInfo.put(RES_BOOK_VIEW_CONTENT, item.get("viewContent"));
            bookInfo.put(RES_BOOK_LATEST_VERSION, item.get("latestVersion"));
            retBooks.add(bookInfo);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_BOOK, retBooks);
        return resultMap;
    }


    @RequestMapping(value = "/book/update_old.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserBookOld() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name(), MATH.name(), CHINESE.name());
            if (!StringUtils.isEmpty(getRequestString(REQ_STUDENT_ID))) {
                validateRequiredNumber(REQ_STUDENT_ID, "学生ID");
                validateRequest(REQ_STUDENT_ID, REQ_BOOK_ID, REQ_SUBJECT);
            } else
                validateRequest(REQ_BOOK_ID, REQ_SUBJECT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User user = getApiRequestUser();
        Student student;
        if (user == null)
            return noUserResult;
        if (UserType.PARENT == user.fetchUserType()) {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            if (studentId != 0L) {
                Boolean isRightRef = checkStudentParentRef(studentId, user.getId());
                if (!isRightRef) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }
                student = studentLoaderClient.loadStudent(studentId);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return resultMap;
            }
        } else if (UserType.STUDENT == user.fetchUserType()) {
            student = studentLoaderClient.loadStudent(user.getId());
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            return resultMap;
        }


        Long bookId = getRequestLong(REQ_BOOK_ID);
        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));
        contentServiceClient.setUserDefaultBook(student, bookId, subject);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/book/update.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserBook() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name(), MATH.name(), CHINESE.name());
            if (getRequest().getParameter(REQ_STUDENT_ID) != null) {
                validateRequest(REQ_STUDENT_ID, REQ_BOOK_ID, REQ_SUBJECT);
            } else
                validateRequest(REQ_BOOK_ID, REQ_SUBJECT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String bookId = getRequestString(REQ_BOOK_ID);
        User user = getApiRequestUser();
        Student student;
        if (user == null)
            return noUserResult;
        if (UserType.PARENT == user.fetchUserType()) {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            if (studentId != 0L) {
                Boolean isRightRef = checkStudentParentRef(studentId, user.getId());
                if (!isRightRef) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }
                student = studentLoaderClient.loadStudent(studentId);
            } else {
                //如果传的学生id为0,说明没有孩子,这个时候把选的教材记到家长上
                asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                        .CParentSelfStudyBookCacheManager_setParentSeflStudyBook(user.getId(), SelfStudyType.WALKMAN_ENGLISH, bookId)
                        .awaitUninterruptibly();
                return successMessage();
            }
        } else if (UserType.STUDENT == user.fetchUserType()) {
            student = studentLoaderClient.loadStudent(user.getId());
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            return resultMap;
        }


        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));
        contentServiceClient.setUserSelfStudyDefaultBook(student, subject, SelfStudyType.WALKMAN_ENGLISH.name(), bookId);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}

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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamItem;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Sadi.Wan on 2015/6/2.
 */
@Controller
@RequestMapping(value = "/v1/examquestion")
@Slf4j
public class ExamQuestionApiController extends AbstractApiController {

    @RequestMapping(value = "/getenexamquestion.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getEnExamQuestion() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_QUESTION_COUNT, "题目数");
            validateDigitNumber(REQ_QUESTION_COUNT, "题目数");
            validateRequest(REQ_QUESTION_COUNT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        long userId = getApiRequestUser().getId();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        String psrType = getApiRequestApp().getAppKey();
        if (null == studentDetail) {//非法user
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }
        NewBookProfile newBookProfile = newClazzBookLoaderClient.fetchUserBook(studentDetail, Subject.ENGLISH);
        if (newBookProfile == null || newBookProfile.getOldId() == null) {//拿不到书
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户没有课本");
            return resultMap;
        }
        long bookId = newBookProfile.getOldId();
        int qCount = getRequestInt(REQ_QUESTION_COUNT);
        List<PsrExamItem> examItems = buildExamQuestion(bookId, psrType, studentDetail, qCount, true);
        if (CollectionUtils.isEmpty(examItems)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "无法获取题目");
            return resultMap;
        }
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_CLAZZ_ID, null == studentDetail.getClazzId() ? 0L : studentDetail.getClazzId()).add(RES_BOOK_ID, bookId).add(RES_QUESTION_LIST, examItems);
    }

    protected List<PsrExamItem> buildExamQuestion(long bookId, String appId, StudentDetail studentDetail, int questionCount, boolean withBackup) {
        List<PsrExamItem> responseQuestion = new ArrayList<>();
        //PSR first
        Set<String> pushedEids = new HashSet<>();
        PsrExamContent psrRs = utopiaPsrServiceClient.getPsrExam(appId, "student", studentDetail.getId(), null == studentDetail.getCityCode() ? 0 : studentDetail.getCityCode(), bookId, -1L, questionCount, 0f, 1f, null == studentDetail.getClazzLevelAsInteger() ? 0 : studentDetail.getClazzLevelAsInteger());
        if (psrRs.getErrorContent().equals("success")) {
            for (PsrExamItem eitem : psrRs.getExamList()) {
                responseQuestion.add(eitem);
                pushedEids.add(eitem.getEid());
            }
        }
        if (responseQuestion.size() >= questionCount || !withBackup) {
            return responseQuestion;
        }
        if (responseQuestion.size() < questionCount) {
            logger.warn("FAILED TO fetch enough english exam question,bookId {},required {},fetched {}", bookId, questionCount, responseQuestion.size());
        }
        return responseQuestion;
    }
}

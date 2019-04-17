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

package com.voxlearning.washington.controller.wonderland;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.errorrecord.api.client.ErrorRecordLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static com.voxlearning.utopia.service.wonderland.api.constant.WonderlandErrorType.NEED_LOGIN;

/**
 * @author songtao
 * @since 2017/8/17
 */
@Controller
@RequestMapping("/wonderland/wrongquestion")
public class WonderlandWrongQuestionController extends AbstractController {

    @Inject
    private ErrorRecordLoaderClient errorRecordLoaderClient;

    // 错题本首页 -- 错题本功能迁移至小U内部，其他的都可以删除了
    @RequestMapping(value = "homepage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homepage() {
        StudentDetail student = fetchStudent();
        if (student == null) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());

        // 用户是否在黑名单
        boolean black = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(student))
                .getOrDefault(student.getId(), false);

        // 判断用户是否被家长关闭了成长世界，家长通按关闭算
        StudentExtAttribute ext = studentLoaderClient.loadStudentExtAttribute(student.getId());
        boolean closed = currentUser().isParent() || (ext != null && (ext.fairylandClosed() || ext.vapClosed()));

        // 学生三科错题总数
        int totalWrongQuestions = 0;
        try {
            MapMessage remoteResult = errorRecordLoaderClient.getRemote().fetchTotalWrongQuestionQuantity(student.getId()).getUninterruptibly();
            if (remoteResult.isSuccess()) {
                totalWrongQuestions = SafeConverter.toInt(remoteResult.get("totalWrongQuestions"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return MapMessage.successMessage()
                .add("clazzLevel", student.getClazzLevel() != null ? student.getClazzLevel().getLevel() : 0)
                .add("black", black)
                .add("fairylandClosed", closed || black)
                .add("result", Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE))
                .add("totalWrongQuestions", totalWrongQuestions)
                .add("studentName", student.fetchRealname());
    }

    private StudentDetail fetchStudent() {
        User user = currentUser();
        if (user == null || (!user.isParent() && !user.isStudent())) return null;

        StudentDetail student;
        if (user.isParent()) {
            long studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
            if (studentId == 0L) studentId = getRequestLong("sid");
            if (studentId == 0L) return null;
            student = studentLoaderClient.loadStudentDetail(studentId);
        } else {
            student = user instanceof StudentDetail ? (StudentDetail) user : currentStudentDetail();
        }
        return student;
    }
}

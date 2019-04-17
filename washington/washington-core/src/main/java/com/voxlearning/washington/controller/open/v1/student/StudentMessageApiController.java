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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_NATIVE_VERSION;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_SUCCESS;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_MESSAGE_LIST;

/**
 * @author Shuai Huan
 * @since 2015/6/12
 */
@Controller
@RequestMapping(value = "/v1/student/message")
@Slf4j
public class StudentMessageApiController extends AbstractStudentApiController {
    @RequestMapping(value = "/get.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudentAppMessage() {
        logger.error("client version error: ver=={}", getRequestString(REQ_APP_NATIVE_VERSION));
        MapMessage resultMap = new MapMessage();
        resultMap.add(RES_MESSAGE_LIST, Collections.emptyList());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}

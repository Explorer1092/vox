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

package com.voxlearning.washington.controller.parent.homework;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.CorrectHomeworkService;
import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 订正作业api
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Controller
@RequestMapping(value = "/parent/homework/correct")
@Slf4j
public class CorrectHomeworkController extends AbstractController {

    //local variables
    @ImportService(interfaceClass = CorrectHomeworkService.class)
    private CorrectHomeworkService correctHomeworkService;

    //Login
    /**
     * 订正作业流程
     *
     * @param homeworkId 作业id
     * @param studentId 学生id
     * @param command 命令，默认index
     * @return
     */
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index(String homeworkId, Long studentId,
                            @RequestParam(name = "command", defaultValue = "INDEX") Command command,
                            @RequestParam(required = false)String courseId,
                            @RequestParam(required = false)String homeworkResultId,
                            @RequestParam(required = false)ObjectiveConfigType objectiveConfigType) {
        CorrectParam correctParam = new CorrectParam();
        correctParam.setCommand(command);
        if(command == Command.SUBMIT){
            correctParam.setData(JsonUtils.convertJsonObjectToMap(getRequestString("data")));
        }
        correctParam.setStudentId(studentId);
        correctParam.setHomeworkId(homeworkId);
        correctParam.setCurrentUserId(currentUserId());
        correctParam.setHomeworkResultId(homeworkResultId);
        correctParam.setObjectiveConfigType(objectiveConfigType);
        correctParam.setCourseId(courseId);
        MapMessage result = correctHomeworkService.dos(correctParam);
        return result;
    }

}

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
import com.voxlearning.utopia.service.parent.homework.api.HomeworkDoService;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 做作业api
 *
 * @author Wenlong Meng
 * @version 20181102
 * @date 2018-11-02
 */
@Controller
@RequestMapping(value = "/parent/homework/do")
@Slf4j
public class HomeworkDoController extends AbstractStudentApiController {

    //local variables
    @ImportService(interfaceClass = HomeworkDoService.class)
    private HomeworkDoService homeworkDoService;

    //Login
    /**
     * 做作业流程
     *
     * @param homeworkId 作业id
     * @param studentId 学生id
     * @param objectiveConfigType 作业形式，默认EXAM
     * @param command 命令，默认index
     * @return
     */
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage process(String homeworkId, Long studentId, @RequestParam(name = "objectiveConfigType", defaultValue = "EXAM")String objectiveConfigType,
                              @RequestParam(name = "command", defaultValue = "index") String command) {
        HomeworkParam homeworkParam = new HomeworkParam();
        homeworkParam.setCommand(command);
        if(homeworkParam.getCommand().equals("submit")){
            homeworkParam.setData(JsonUtils.convertJsonObjectToMap(getRequestString("data")));
        }
        homeworkParam.setStudentId(studentId);
        homeworkParam.setHomeworkId(homeworkId);
        homeworkParam.setObjectiveConfigType(objectiveConfigType);
        homeworkParam.setCurrentUserId(currentUserId());
        MapMessage result = homeworkDoService.dos(homeworkParam);
        return result;
    }

}

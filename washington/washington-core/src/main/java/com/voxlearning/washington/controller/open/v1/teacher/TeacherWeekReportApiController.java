package com.voxlearning.washington.controller.open.v1.teacher;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

@Controller
@RequestMapping("/v1/teacher/week/report")
public class TeacherWeekReportApiController extends AbstractTeacherApiController {


    @Inject
    private WeekReportLoaderClient weekReportLoaderClient;


    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage historyIndex() {

        MapMessage resultMap = new MapMessage();
        try {
            // 兼容iOS用了多余的参数来计算sig
            String[] paramKeys = new String[]{REQ_SUBJECT, REQ_CLAZZ_GROUP_IDS, REQ_PAGE_NUMBER, REQ_HOMEWORK_STATUS};
            List<String> sigParamKeys = new ArrayList<>();
            for (String param : paramKeys) {
                if (StringUtils.isNotBlank(getRequestString(param))) {
                    sigParamKeys.add(param);
                }
            }
            String[] sigParamKeysArray = new String[sigParamKeys.size()];
            validateRequest(sigParamKeys.toArray(sigParamKeysArray));
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {

            resultMap = weekReportLoaderClient.fetchWeekReportBriefV2(teacher);
            if (resultMap.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                return resultMap;
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                return resultMap;
            }

        } catch (Exception e) {
            logger.info("fetch Week ReportBrief  tid of {} failed ", teacher.getId(), e);
            resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

    }
}

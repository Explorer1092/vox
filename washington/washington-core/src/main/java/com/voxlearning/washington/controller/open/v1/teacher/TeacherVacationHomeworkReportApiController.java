package com.voxlearning.washington.controller.open.v1.teacher;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.RES_NOT_CLAZZ_TEACHER_MSG;

@Controller
@RequestMapping(value = "/v1/teacher/vacation/report")
public class TeacherVacationHomeworkReportApiController extends AbstractTeacherApiController {

    /**
     * APP端：分享家长端接口
     * @return
     */
    @RequestMapping(value = "sharejztmsg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage pushShareJztMsg() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(RES_PACKAGES, "包IDs");
            validateRequest(RES_PACKAGES);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            String packageIdsStr = getRequestString(RES_PACKAGES);
            String[] split = StringUtils.split(packageIdsStr, ",");
            List<String> packageIds = Arrays.asList(split);
            MapMessage mapMessage = vacationHomeworkReportLoaderClient.pushShareJztMsg(packageIds, teacher);

            if (mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("rewardNum", mapMessage.get("rewardNum"));
                resultMap.add("successPackages", mapMessage.get("successPackages"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    /**
     * APP端：微信&QQ分享接口
     * @return
     */
    @RequestMapping(value = "shareweixin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage shareReportWeiXin() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(RES_PACKAGES, "包IDs");
            validateRequest(RES_PACKAGES);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_CLAZZ_TEACHER_MSG);
            return resultMap;
        }
        try {
            String packageIdsStr = getRequestString(RES_PACKAGES);
            String[] split = StringUtils.split(packageIdsStr, ",");
            List<String> packageIds = Arrays.asList(split);

            MapMessage mapMessage = vacationHomeworkReportLoaderClient.shareReportWeiXin(packageIds, teacher);

            if (mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("successPackages", mapMessage.get("successPackages"));
                resultMap.add("rewardNum", mapMessage.get("rewardNum"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

}

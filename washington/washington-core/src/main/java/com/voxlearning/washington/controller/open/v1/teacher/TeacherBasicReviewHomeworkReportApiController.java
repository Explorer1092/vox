package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.RES_NOT_CLAZZ_TEACHER_MSG;

@Controller
@RequestMapping(value = "/v1/teacher/basicreview/report")
@Slf4j
public class TeacherBasicReviewHomeworkReportApiController extends AbstractTeacherApiController {
    @Inject
    private BasicReviewHomeworkReportLoaderClient basicReviewHomeworkReportLoaderClient;

    //老师有作业包的班级列表 h5 and app
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchClazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateRequest(REQ_SUBJECT);
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
            MapMessage mapMessage = basicReviewHomeworkReportLoaderClient.fetchBasicReviewClazzInfo(teacher, false);
            if (mapMessage.isSuccess()) {
                resultMap.add("clazzInfoList", mapMessage.get("clazzInfoList"));
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }
    //删除作业包接口
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateRequired(REQ_PACKAGE_ID, "包ID");
            validateRequest(REQ_SUBJECT,REQ_PACKAGE_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String packageId = getRequestString(REQ_PACKAGE_ID);
        if (StringUtils.isBlank(packageId)) {
            return MapMessage.errorMessage("作业不存在");
        }
        try {
            MapMessage mapMessage = newHomeworkServiceClient.deleteBasicReviewHomework(teacher, packageId);
            if (mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            }
            return resultMap;
        } catch (Exception ex) {
            logger.error("Failed to delete basic review, error is:{}", ex);
            return MapMessage.errorMessage("删除期末复习基础必过异常");
        }
    }


    //班级关卡信息 app
    @RequestMapping(value = "stagelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchStageListToClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PACKAGE_ID, "包ID");
            validateRequest(REQ_PACKAGE_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacherBySubject();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String packageId = this.getRequestString(REQ_PACKAGE_ID);
        try {
            MapMessage mapMessage = basicReviewHomeworkReportLoaderClient.fetchStageListToClazz(packageId);
            if (mapMessage.isSuccess()) {
                resultMap.add("stageBriefList", mapMessage.get("stageBriefList"));
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            }
            return resultMap;
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }
}

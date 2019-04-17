package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/teacher/basicreview/report")
public class TeacherBasicReviewHomeworkReportController extends AbstractTeacherController {

    @Inject
    private BasicReviewHomeworkReportLoaderClient basicReviewHomeworkReportLoaderClient;

    //班级关卡信息 app
    @RequestMapping(value = "stagelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchStageListToClazz() {
        Teacher teacher = currentTeacher();
        String packageId = getRequestString("packageId");
        if (teacher == null || StringUtils.isBlank(packageId)) {
            return MapMessage.errorMessage("老师未登入，或者参数作业PACKAGE_ID不存在");
        }
        try {
            return basicReviewHomeworkReportLoaderClient.fetchStageListToClazz(packageId);
        } catch (Exception e) {
            logger.error("/teacher/basicreview/report/stagelist failed : packageId {},tid {}", packageId, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }


    //老师有作业包的班级列表 h5 and app
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchClazzList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        String subject = getRequestString("subject");
        if (teacher == null || StringUtils.isAnyBlank(subject)) {
            return MapMessage.errorMessage("老师未登入");
        }
        try {
            return basicReviewHomeworkReportLoaderClient.fetchBasicReviewClazzInfo(teacher, true);
        } catch (Exception e) {
            logger.error("/teacher/basicreview/report/clazzlist failed : subject {},tid {}", subject, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    //班级关卡信息 app
    @RequestMapping(value = "pushmsg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage pushBasicReviewReportMsgToJzt() {
        Teacher teacher = currentTeacher();
        String packageId = getRequestString("packageId");
        String homeworkId = getRequestString("homeworkId");
        if (teacher == null || StringUtils.isAnyBlank(packageId, homeworkId)) {
            return MapMessage.errorMessage("老师未登入，或者参数作业PACKAGE_ID,HOMEWORK_ID不存在");
        }
        try {
            return basicReviewHomeworkReportLoaderClient.pushBasicReviewReportMsgToJzt(teacher, packageId, homeworkId);
        } catch (Exception e) {
            logger.error("/teacher/basicreview/report/pushmsg failed : packageId {},hid {},tid {}", packageId, homeworkId, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }


}

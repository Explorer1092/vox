package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.consumer.DiagnoseReportServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Set;

import static com.voxlearning.utopia.api.constant.ErrorCodeConstants.ERROR_CODE_PARAMETER;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.REQ_PAGE_NUMBER;

/**
 * 重点讲练测报告
 * @author majianxin
 * @version V1.0
 * @date 2018/6/28
 */
@Controller
@RequestMapping("/teacher/new/homework/intelligent/teaching/")
public class TeacherIntelligentTeachingReportController extends AbstractTeacherController{

    @Inject private DiagnoseReportServiceClient diagnoseReportServiceClient;

    @RequestMapping(value = "fetchclazzinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchClazzInfo() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        return MapMessage.successMessage().add("clazzList", diagnoseReportServiceClient.fetchClazzInfo(relTeacherIds));
    }

    @RequestMapping(value = "reportlist.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchReportList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        Long groupId = getRequestLong("groupId");
        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER, 1);
        Pageable pageable = new PageRequest(currentPage - 1, 10);

        return MapMessage.successMessage().add("page", diagnoseReportServiceClient.fetchReportList(groupId, pageable, currentSubject())).add("groupId", groupId);
    }

    @RequestMapping(value = "reportdetail.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchReportDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        String hid = getRequestString("hid");
        ObjectiveConfigType configType = ObjectiveConfigType.of(getRequestString("objectiveConfigType"));
        String questionBoxId = getRequestString("questionBoxId");
        return MapMessage.successMessage().add("reportDetail", diagnoseReportServiceClient.fetchReportDetail(hid, configType, questionBoxId));
    }

    @RequestMapping(value = "oralstudentquestiondetail.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage oralStudentQuestionDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        String hid = getRequestString("hid");
        ObjectiveConfigType configType = ObjectiveConfigType.of(getRequestString("objectiveConfigType"));
        String questionId = getRequestString("questionId");
        if (StringUtils.isBlank(hid) || configType == null || StringUtils.isBlank(questionId)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }
        return MapMessage.successMessage().add("questionDetail", diagnoseReportServiceClient.oralStudentQuestionDetail(hid, configType, questionId));
    }

    @RequestMapping(value = "teachingrecommend.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchIntelligentTeachingRecommend() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }
        return MapMessage.successMessage().add("teachingRecommend", diagnoseReportServiceClient.fetchIntelligentTeachingRecommend(homeworkId));
    }
}

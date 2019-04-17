package com.voxlearning.washington.controller.open.v1.teacher;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

@Controller
@RequestMapping(value = "/v1/teacher/newexam/report")
public class TeacherNewExamReportApiController extends AbstractTeacherApiController {

    @Inject private NewExamReportLoaderClient newExamReportLoaderClient;


    @RequestMapping(value = "newclazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
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

            MapMessage message = newExamReportLoaderClient.fetchTeacherClazzInfo(teacher);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get(RES_CLAZZ_LIST));

            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());

            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }


    /**
     * 考试列表
     */
    @RequestMapping(value = "examlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadExamList() {
        return failMessage("功能已下线");
    }


    /**
     * 报告班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadClazzList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        try {
            // 暂时只显示英语班级
            Long teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), Subject.ENGLISH);
            if (teacherId == null) {
                teacherId = teacher.getId();
            }
            MapMessage message = newExamServiceClient.loadTeacherClazzList(Collections.singleton(teacherId));
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_CLAZZ_LIST, message.get("clazzList"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "获取失败，请稍候重试");
        }
        return resultMap;
    }


    @RequestMapping(value = "newexamlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage examList() {
        MapMessage resultMap = new MapMessage();
        Integer currentPage = getRequestInt(REQ_MSG_PAGE);
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        try {
            validateDigitNumber(REQ_MSG_PAGE, "分页页码");
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_SUBJECT, "学科");
            validateRequest(REQ_MSG_PAGE, REQ_CLAZZ_ID, REQ_SUBJECT);
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
            MapMessage message = newExamReportLoaderClient.newPageUnifyExamList(teacher, clazzId, 10, currentPage * 10);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("pageable", message.get("pageable"));
                resultMap.add("offText", "由于系统升级11月13日之前的考试请在PC端查看报告");
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }


    @RequestMapping(value = "newattendance.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage attendance() {
        MapMessage resultMap = new MapMessage();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String examId = getRequestString(REQ_EXAM_ID);
        try {
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_EXAM_ID, "考试ID");
            validateRequest(REQ_CLAZZ_ID, REQ_EXAM_ID);
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
            MapMessage message = newExamReportLoaderClient.fetchNewExamAttendanceReport(teacher, examId, clazzId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("submitStudents", message.get("submitStudents"));
                resultMap.add("unJoinStudents", message.get("unJoinStudents"));
                resultMap.add("unSubmitStudents", message.get("unSubmitStudents"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

    @RequestMapping(value = "newstudents.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage students() {
        MapMessage resultMap = new MapMessage();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String examId = getRequestString(REQ_EXAM_ID);
        try {
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_EXAM_ID, "考试ID");
            validateRequest(REQ_CLAZZ_ID, REQ_EXAM_ID);
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
            MapMessage message = newExamReportLoaderClient.fetchNewExamStudentReport(teacher, examId, clazzId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("newExamName", message.get("newExamName"));
                resultMap.add("students", message.get("students"));
                resultMap.add("single", message.get("single"));
                resultMap.add("studentDetailUrl", "/view/mobile/student/junior/newexamv2/examdetail");
                resultMap.add("studentNum", message.get("studentNum"));
                resultMap.add("joinNum", message.get("joinNum"));
                resultMap.add("paperTotalScore", message.get("paperTotalScore"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }


    @RequestMapping(value = "newclazzquestions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzQuestions() {
        MapMessage resultMap = new MapMessage();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String examId = getRequestString(REQ_EXAM_ID);
        try {
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_EXAM_ID, "考试ID");
            validateRequest(REQ_CLAZZ_ID, REQ_EXAM_ID);
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
            MapMessage message = newExamReportLoaderClient.fetchNewExamQuestionReport(teacher, examId, clazzId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("newExamName", message.get("newExamName"));
                resultMap.add("clazzName", message.get("clazzName"));
                resultMap.add("hasOral", message.get("hasOral"));
                resultMap.add("singleQuestionDetailUrl", "/view/newexamv2/questiondetail");
                resultMap.add("examEndTime", message.get("examEndTime"));
                resultMap.add("examCorrectEndTime", message.get("examCorrectEndTime"));
                resultMap.add("newExamPaperInfos", message.get("newExamPaperInfos"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {}", examId, clazzId, e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }


    @RequestMapping(value = "newstatistics.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage statisticsReport() {
        MapMessage resultMap = new MapMessage();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String examId = getRequestString(REQ_EXAM_ID);
        try {
            validateDigitNumber(REQ_CLAZZ_ID, "班级ID");
            validateRequired(REQ_EXAM_ID, "考试ID");
            validateRequest(REQ_CLAZZ_ID, REQ_EXAM_ID);
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
            MapMessage message = newExamReportLoaderClient.fetchNewExamStatisticsReport(teacher, examId, clazzId);
            if (message.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("newExamStatistics", message.get("newExamStatistics"));
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, message.getInfo());
            }
        } catch (Exception e) {
            logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {}", examId, clazzId, e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
        }
        return resultMap;
    }

}

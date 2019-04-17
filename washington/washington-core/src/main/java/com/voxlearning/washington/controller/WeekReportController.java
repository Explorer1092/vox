package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.NewHomeworkShareReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.OcrHomeworkShareReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.WeekReportForStudent;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkContentServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkReportServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import com.voxlearning.washington.service.NewExamHelper;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


@Controller
@RequestMapping("/container")
public class WeekReportController extends AbstractTeacherController {
    @Inject
    private WeekReportLoaderClient weekReportLoaderClient;
    @Inject
    private WeekReportServiceClient weekReportServiceClient;
    @Inject
    private NewHomeworkReportServiceClient newHomeworkReportServiceClient;
    @Inject
    private NewExamHelper newExamHelper;
    @Inject
    private NewHomeworkContentServiceClient newHomeworkContentServiceClient;
    @Inject
    private FlowerServiceClient flowerServiceClient;

    @RequestMapping(value = "week/toclazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage weekReportToClazz() {
        String groupIdAndReportId = this.getRequestString("groupIdAndReportId");
        if (StringUtils.isBlank(groupIdAndReportId)) {
            return MapMessage.errorMessage("参数错误");
        }
        User user = currentUser();
        try {
            return weekReportLoaderClient.fetchWeekReportForClazz(groupIdAndReportId, user);
        } catch (Exception e) {
            logger.error("fetch week report of {}  clazz info failed ", groupIdAndReportId);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "week/push/message/toteacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage pushMessageToTeacher() {
        String tidStr = this.getRequestString("tids");
        if (StringUtils.isBlank(tidStr)) {
            return MapMessage.errorMessage();
        }
        List<Long> teacherIds = new LinkedList<>();

        for (String s : StringUtils.split(tidStr, ",")) {
            teacherIds.add(SafeConverter.toLong(s));
        }
        try {
            weekReportServiceClient.pushMessageToTeacher(teacherIds);
        } catch (Exception e) {
            logger.error("push message failed to tids {}", tidStr, e);
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage();

    }


    @RequestMapping(value = "newhomework/share/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newHomeworkShareReport() {

        String homeworkId = this.getRequestString("hid");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage();
        }
        User user = currentUser();
        try {
            NewHomeworkShareReport newHomeworkShareReport = newHomeworkReportServiceClient.processNewHomeworkShareReport(homeworkId, user, getCdnBaseUrlAvatarWithSep());
            if (newHomeworkShareReport.isSuccess()) {
                //避免传送无效数据
                newHomeworkShareReport.setStudentReportMap(null);
            } else {
                return MapMessage.errorMessage();
            }
            MapMessage mapMessage = newHomeworkShareReport.getMapMessage();
            newHomeworkShareReport.setMapMessage(null);
            List<Integer> channels = new ArrayList<>();
            if (user != null) {
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                if(teacherDetail != null){
                    channels = newHomeworkContentServiceClient.loadHomeworkReportShareChannel(teacherDetail);
                }
            }
            newHomeworkShareReport.setChannels(channels);
            mapMessage.put("newHomeworkShareReport", newHomeworkShareReport);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch share homework id {} : failed", homeworkId, e);
            return MapMessage.errorMessage();
        }

    }

    /**
     * 选择优秀配音
     * @return
     */
    @RequestMapping(value = "newhomework/share/choosedubbing.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage chooseExcellentDubbingWithScore(){
        String homeworkId = this.getRequestString("hid");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage();
        }
        return newHomeworkReportServiceClient.getExcellentDubbingStudent(homeworkId);
    }

    @RequestMapping(value = "week/teacher/push/message.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teacherPushMessage() {
        Long tid = this.getRequestLong("tid");
        if (tid == 0L) {
            return MapMessage.errorMessage();
        }
        String groupIdAndReportIdStr = this.getRequestString("groupIdToReportIds");
        List<String> groupIdAndReportIds = new LinkedList<>();
        if (StringUtils.isNotBlank(groupIdAndReportIdStr)) {
            String[] ss = StringUtils.split(groupIdAndReportIdStr, ",");
            Collections.addAll(groupIdAndReportIds, ss);
        } else {
            return MapMessage.errorMessage();
        }
        try {
            return weekReportServiceClient.teacherPushMessage(tid, groupIdAndReportIds,null);
        } catch (Exception e) {
            logger.error("teacher id of {} push message failed :", tid, e);
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "report/fetch/clazzinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchClazzInfo() {
        String homeworkIdStr = this.getRequestString("homeworkIds");
        if (StringUtils.isBlank(homeworkIdStr)) {
            return MapMessage.errorMessage();
        }
        List<String> homeworkIds = new LinkedList<>();

        String[] ss = StringUtils.split(homeworkIdStr, ",");
        for (String s : ss) {
            if (StringUtils.isNotBlank(s)) {
                homeworkIds.add(s);
            }
        }
        if (CollectionUtils.isEmpty(homeworkIds)) {
            return MapMessage.errorMessage();
        }
        try {
            return newHomeworkReportServiceClient.fetchClazzInfo(homeworkIds);
        } catch (Exception e) {
            logger.error("fetch homework's {} class info failed", homeworkIds, e);
            return MapMessage.errorMessage();
        }
    }


    /**
     * 下载学生成绩
     */
    @RequestMapping(value = "loadstudentachievementv2.vpage", method = RequestMethod.GET)
    public void loadStudentAchievementV2(HttpServletResponse response) {
        String examId = getRequestString("exam_id");
        if (StringUtils.isBlank(examId)) {
            return;
        }
        NewExam newExam = newExamLoaderClient.load(examId);
        if (newExam == null) {
            return;
        }
        try {
            Integer type = getRequestInt("type");
            List<RptMockNewExamStudent> rptMockNewExamStudents = newExamReportLoaderClient.getStudentAchievement(examId);
            HSSFWorkbook workbook = newExamHelper.fetchHSSFWorkbook(newExam, type, rptMockNewExamStudents, null);
            @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            String fileName = examId + (type == 1 ? "系统" : "最终") + "模块报告.xls";
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        byteArrayOutputStream.toByteArray()
                );
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            logger.error("学生成绩下载失败!", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "week/tostudent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage weekReportToStudent() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请用户先登入");
        }
        String studentReportId = this.getRequestString("studentReportId");
        String subjectStr = this.getRequestString("subject");
        if (StringUtils.isBlank(subjectStr) || !(subjectStr.equals("MATH") || subjectStr.equals("ENGLISH") || subjectStr.equals("CHINESE"))) {
            return MapMessage.errorMessage("参数错误");
        }
        if (StringUtils.isBlank(studentReportId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            MapMessage mapMessage = weekReportLoaderClient.fetchWeekReportForStudent(Subject.of(subjectStr), studentReportId, user);
            if (mapMessage.isSuccess()) {
                WeekReportForStudent w = (WeekReportForStudent) mapMessage.get("weekReportForStudent");
                w.setTeacherUrl(getUserAvatarImgUrl(w.getTeacherUrl()));
            }
            return mapMessage;
        } catch (Exception e) {

            logger.error("fetch studentReportId of {} , subject of {}  report failed", studentReportId, subjectStr, e);

            return MapMessage.errorMessage();
        }
    }
    /**
     * 纸质作业分享
     * @return
     */
    @RequestMapping(value = "ocrhomework/share/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage ocrHomeworkShareReport() {
        String homeworkId = this.getRequestString("hid");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage();
        }
        User user = currentUser();
        try {
            OcrHomeworkShareReport ocrHomeworkShareReport = newHomeworkReportServiceClient.processOcrHomeworkShareReport(homeworkId, user, getCdnBaseUrlAvatarWithSep());
            if (user != null) {
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                if(teacherDetail != null){
                    List<Integer> channels = newHomeworkContentServiceClient.loadHomeworkReportShareChannel(teacherDetail);
                    ocrHomeworkShareReport.setChannels(channels);
                }
            }
            MapMessage mapMessage = new MapMessage();
            mapMessage.put("ocrHomeworkShareReport", ocrHomeworkShareReport);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch share homework id {} : failed", homeworkId, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 纸质作业-作业单信息
     */
    @RequestMapping(value = "ocrhomework/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadOcrHomeworkDetail() {
        String homeworkIds = getRequestString("homeworkIds");
        List<String> homeworkIdList = StringUtils.toList(homeworkIds, String.class);
        if (CollectionUtils.isEmpty(homeworkIdList)) {
            return MapMessage.errorMessage("作业id错误");
        }
        NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkIdList.get(0));
        if (homework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!NewHomeworkType.OCR.equals(homework.getType())) {
            return MapMessage.errorMessage("非纸质作业");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.loadOcrHomeworkDetail(homeworkIdList);
        //老师信息
        if (homework.getTeacherId() != null) {
            Teacher teacher = teacherLoaderClient.loadTeacher(homework.getTeacherId());
            mapMessage.add("teacherUrl", NewHomeworkUtils.getUserAvatarImgUrl(getCdnBaseUrlAvatarWithSep(), teacher.fetchImageUrl()));
            mapMessage.add("teacherId", teacher.getId());
            mapMessage.add("teacherName", teacher.fetchRealname());
            mapMessage.add("teacherShareMsg", "我刚布置了纸质作业，内容如下。建议家长督促学生完成。");
        }

        Long studentId = getRequestLong("studentId");
        if (studentId != 0) {
            Flower flower = flowerServiceClient.getFlowerService().loadHomeworkFlowers(homeworkIdList.get(0)).getUninterruptibly()
                    .stream()
                    .filter(f -> Objects.equals(studentId, f.getSenderId()))
                    .findFirst()
                    .orElse(null);
            mapMessage.add("hasSentFlower", flower != null);
        }
        return mapMessage;
    }

    /**
     * 纸质作业
     * 报告中转接口：得到/container/ocrhomework/type/result
     * @return
     */
    @RequestMapping(value = "ocrhomework/typeresulturl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getOcrHomeworkReportTypeResultUrl() {
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isEmpty(homeworkId)) {
            return MapMessage.errorMessage("作业ID参数缺失");
        }
        Long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID参数缺失");
        }
        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (!NewHomeworkType.OCR.equals(newHomework.getType())) {
            return MapMessage.errorMessage("非纸质作业");
        }
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        if (newHomeworkResult == null) {
            return MapMessage.errorMessage("该学生还未作答");
        }
        ObjectiveConfigType objectiveConfigType = null;
        if (Subject.MATH.equals(newHomework.getSubject())) {
            objectiveConfigType = ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
        } else if (Subject.ENGLISH.equals(newHomework.getSubject())) {
            objectiveConfigType = ObjectiveConfigType.OCR_DICTATION;
        }
        String typeResultUrl = UrlUtils.buildUrlQuery("/container/ocrhomework/type/result" + Constants.AntiHijackExt,
                MapUtils.m("homeworkId", homeworkId,
                        "studentId", studentId,
                        "objectiveConfigType", objectiveConfigType));

        return MapMessage.successMessage().add("typeResultUrl", typeResultUrl).add("isFinished", newHomeworkResult.isFinished());
    }

    @RequestMapping(value = "ocrhomework/type/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportForObjectiveConfigTypeResult() {
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigType = getRequestString("objectiveConfigType");
        Long studentId = getRequestLong("studentId");
        if (StringUtils.isEmpty(homeworkId) || StringUtils.isEmpty(objectiveConfigType)) {
            return MapMessage.errorMessage("参数错误");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生id错误");
        }
        return newHomeworkReportServiceClient.homeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail);
    }
}

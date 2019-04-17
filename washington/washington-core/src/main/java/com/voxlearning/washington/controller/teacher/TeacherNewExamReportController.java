package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.teacher.TeacherNewExamReportApiController;
import com.voxlearning.washington.data.Constants;
import com.voxlearning.washington.service.NewExamHelper;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.REQ_EXAM_ID;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.RES_NOT_CLAZZ_TEACHER_MSG;

@Controller
@RequestMapping("/teacher/newexam/report")
public class TeacherNewExamReportController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private NewExamHelper newExamHelper;

    /**
     * 统考报告详情
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String historyDetail(Model model) {
        String newExamId = getRequestString("newExamId");
        String clazzId = getRequestString("clazzId");
        NewExam newExam = null;
        if (StringUtils.isNotBlank(newExamId)) {
            newExam = newExamLoaderClient.load(newExamId);
        }
        if (newExam == null) {
            logger.error("EXAM NOT EXISTS,examId:{}", newExamId);
            return "redirect:/index.vpage";
        }
        Subject subject = newExam.getSubject();
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("newExamId", newExamId);
        model.addAttribute("subject", subject);
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        if (StringUtils.isNoneBlank(newExam.getPaperId())) {
            //旧模考地址
            return "teacherv3/newexam/report/detail";
        } else {
            if(newExam.getSchoolLevel() == SchoolLevel.JUNIOR && isNewModelExam(newExam)){
                return "teacherv3/newexamv3/history";
            }else{
                return "teacherv3/newexamv2/history";
            }
        }
    }

    /**
     * 下载考试报告
     */
    @RequestMapping(value = "downloadexamreport.vpage", method = RequestMethod.GET)
    public void downloadexamreport(HttpServletResponse response) {
        String examId = getRequestString("newExamId");
        Long clazzId = getRequestLong("clazzId");

        if (StringUtils.isAnyBlank(examId) || clazzId <= 0) {
            return;
        }
        NewExam newExam = newExamLoaderClient.load(examId);
        if (newExam == null){
            return;
        }
        List<RptMockNewExamStudent> rptMockNewExamStudentList = newExamReportLoaderClient.getStudentAchievement(examId, SafeConverter.toInt(clazzId));
        if (CollectionUtils.isEmpty(rptMockNewExamStudentList)){
            return;
        }
        Teacher teacher = currentTeacher();
        if (teacher == null){
            return;
        }
        try {
            Map<Long, User> allUserMap = loadClazzStudents(teacher, newExam.getSubject(), clazzId);
            HSSFWorkbook hSSFWorkbook = newExamHelper.fetchHSSFWorkbook(newExam,2,rptMockNewExamStudentList,allUserMap);
            @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert hSSFWorkbook != null;
            hSSFWorkbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();

            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(SafeConverter.toLong(clazzId));

            String className = clazz != null ? clazz.getClassName() : "";
            String filename = className + "成绩单" + ".xls";
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            logger.error(String.format("download exception! clazzId=%s,examId=%s", clazzId, examId), e);
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ex) {
                logger.error(String.format("download exception! clazzId=%s,examId=%s", clazzId, examId), ex);
            }
        }

    }


    private Map<Long, User> loadClazzStudents(Teacher teacher, Subject subject, Long clazzId) {
        Long teacherId;
        switch (subject) {
            case JENGLISH:
                subject = Subject.ENGLISH;
                break;
            case JMATH:
                subject = Subject.MATH;
                break;
            case JCHINESE:
                subject = Subject.CHINESE;
                break;
            default:
                break;
        }
        if (subject == teacher.getSubject()) {
            teacherId = teacher.getId();
        } else {
            teacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
        }
        GroupMapper groupMapper = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
        if (groupMapper == null) {
            return Collections.emptyMap();
        }
        return studentLoaderClient.loadGroupStudents(groupMapper.getId())
                .stream()
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
    }


    /**
     * 老模考接口
     */
    @RequestMapping(value = "detail/clazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzDetail(@RequestParam("clazzId") Long clazzId, @RequestParam("newExamId") String newExamId) {
        try {
            return newExamReportLoaderClient.examDetailForClazz(currentTeacher(), newExamId, clazzId);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }


    /**
     * 老模考接口
     */
    @RequestMapping(value = "detail/users.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage usersDetail(@RequestParam("clazzId") Long clazzId, @RequestParam("newExamId") String newExamId) {
        try {
            return newExamReportLoaderClient.examDetailForStudent(currentTeacher(), newExamId, clazzId);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 老模考接口
     */
    @RequestMapping(value = "user.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage user(@RequestParam("userIds") String userStr) {
        try {
            String[] u = userStr.split(",");
            List<Long> userIds = new ArrayList<>();
            for (String uid : u) {
                userIds.add(SafeConverter.toLong(uid));
            }
            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
            List<Map<String, Object>> users = userMap.values().stream().map(user -> MapUtils.m("studentId", user.getId(),
                    "studentName", user.fetchRealname(),
                    "studentImg", user.fetchImageUrl())).collect(Collectors.toList());
            return MapMessage.successMessage().add("users", users);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }


    /**
     * 学生列表
     */
    @RequestMapping(value = "newstudents.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage students() {
        MapMessage message;
        Long clazzId = getRequestLong("clazzId");
        String examId = getRequestString("examId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamStudentReport(teacher, examId, clazzId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 老模考接口
     */
    @RequestMapping(value = "studentanswer.vpage", method = RequestMethod.GET)
    public String historyStudentanswer(Model model) {
        Long studentId = getRequestLong("userId");

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return "redirect:/index.vpage";
        }

        String id = getRequestString("newexamId");
        if (StringUtils.isBlank(id)) {
            return "redirect:/index.vpage";
        }
        NewExam newExam = newExamLoaderClient.load(id);
        if (newExam == null) {
            return "redirect:/index.vpage";
        }

        Long clazzId = null;
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        for (GroupMapper gm : groupMappers) {
            if (gm.getGroupType().equals(GroupType.TEACHER_GROUP) && gm.getSubject().equals(newExam.processSubject())) {
                clazzId = gm.getClazzId();
                break;
            }
        }

        // #37121 如果是中学而且没有匹配到对应的老师分组，再匹配一次教学班组
        if (clazzId == null && studentDetail.isJuniorStudent()) {
            for (GroupMapper gm : groupMappers) {
                if (gm.getGroupType().equals(GroupType.WALKING_GROUP) && gm.getSubject().equals(newExam.processSubject())) {
                    clazzId = gm.getClazzId();
                    break;
                }
            }
        }

        Teacher teacher = getSubjectSpecifiedTeacher(newExam.getSubject());
        if (teacher == null) {
            return "redirect:/index.vpage";
        }

        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            return "redirect:/index.vpage";
        }

        model.addAttribute("subject", newExam.getSubject());
        model.addAttribute("userId", studentId);
        model.addAttribute("id", id);
        model.addAttribute("clazzId", clazzId);

        return "teacherv3/newexam/report/studentanswer";
    }


    /**
     * 单题接口
     */
    @RequestMapping(value = "singlesubquestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage singleSubQuestion() {
        MapMessage message;
        Long clazzId = getRequestLong("clazzId");
        String paperId = getRequestString("paperId");
        String examId = getRequestString("examId");
        String qid = getRequestString("questionId");
        int subIndex = getRequestInt("subIndex");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamSingleQuestionDimension(teacher, examId, clazzId, paperId, qid, subIndex);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }


    /**
     * 试卷题信息(老接口)
     */
    @RequestMapping(value = "newpaperclazzanswer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperClazzAnswer() {
        MapMessage message;
        String paperId = getRequestString("paperId");
        Long clazzId = getRequestLong("clazzId");
        String examId = getRequestString("examId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamPaperQuestionAnswerInfo(teacher, examId, clazzId, paperId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 试卷题信息(一起测)
     */
    @RequestMapping(value = "paperclazzanswerdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperClazzAnswerDetail() {
        MapMessage message;
        String paperId = getRequestString("paperId");
        Long clazzId = getRequestLong("clazzId");
        String examId = getRequestString("examId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            message = newExamReportLoaderClient.paperClazzAnswerDetail(teacher, examId, clazzId, paperId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }



    /**
     * 页面信息
     */
    @RequestMapping(value = "newpaperinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperInfo() {
        MapMessage message;
        Long clazzId = getRequestLong("clazzId");
        String examId = getRequestString("examId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamPaperInfo(examId, clazzId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }


    /**
     * 统计页面接口
     */
    @RequestMapping(value = "newstatistics.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage statistics() {
        MapMessage message;
        Long clazzId = getRequestLong("clazzId");
        String examId = getRequestString("examId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            message = newExamReportLoaderClient.fetchNewExamStatisticsReport(teacher, examId, clazzId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 老模考接口
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String historyIndex(Model model) {

        List<Map<String, Object>> clazzMaps = getClazzList();

        Map<Integer, List<Map<String, Object>>> clazzLevelMap = new LinkedHashMap<>();
        for (Map<String, Object> clazzMap : clazzMaps) {
            Integer clazzLevel = SafeConverter.toInt(clazzMap.get("classLevel"));
            List<Map<String, Object>> clazzs = clazzLevelMap.get(clazzLevel);
            if (clazzs == null) {
                clazzs = new ArrayList<>();
            }
            clazzs.add(clazzMap);
            clazzLevelMap.put(clazzLevel, clazzs);
        }
        model.addAttribute("clazzLevelMap", JsonUtils.toJson(clazzLevelMap));
        return "teacherv3/newexam/report/index";
    }


    /**
     * 模块列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reportList(@RequestParam("clazzId") Long clazzId) {
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            if (teacher == null || teacher.getSubject() == null) {
                return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
            }
            Integer currentPage = getRequestInt("currentPage");
            return newExamReportLoaderClient.newPageUnifyExamList(teacher, clazzId, 10, currentPage * 10);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * Copy by {@link TeacherNewExamReportApiController#clazzQuestions()}
     * 供H5调用
     */
    @RequestMapping(value = "newclazzquestions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzQuestions() {
        MapMessage resultMap = new MapMessage();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String examId = getRequestString(REQ_EXAM_ID);

        Teacher teacher = getSubjectSpecifiedTeacher();
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

    /**
     * 老师考试报告
     */
    @RequestMapping(value = "detailsanalysis.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherNewExamReport() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage("请使用老师账号登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String examId = getRequestString("exam_id");
        String classId = getRequestString("class_id");
        String moduleId = getRequestString("module_id");
        if (examId == null || classId == null || moduleId == null) {
            return MapMessage.errorMessage("请求错误");
        }

        String domain = RuntimeMode.current().le(Mode.TEST) ? Constants.YQC_REPORT_URL_TEST : Constants.YQC_REPORT_URL_PROD;
        String url = UrlUtils.buildUrlQuery(domain + "/report_api/v1/teacher_report", MapUtils.map("exam_id", examId, "class_id", classId, "module_id", moduleId));
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .get(url)
                    .contentType("application/json").socketTimeout(3 * 1000)
                    .execute();
            if (response != null && response.getStatusCode() == 200) {
                Map<String, Object> dataMap = JsonUtils.fromJson(response.getResponseString());
                Object success = dataMap.get("success");
                if ("true".equals(success)) {
                    return MapMessage.successMessage().add("data", dataMap.get("dataMap"));
                } else {
                    return MapMessage.errorMessage(SafeConverter.toString(dataMap.get("msg")));
                }
            } else {
                return MapMessage.errorMessage("获取考试报告失败, 请稍后重试");
            }
        } catch (Exception e) {
            logger.error("获取学生考试报告接口失败:{}", url);
            return MapMessage.errorMessage("获取考试报告失败, 请稍后重试");
        }
    }
}

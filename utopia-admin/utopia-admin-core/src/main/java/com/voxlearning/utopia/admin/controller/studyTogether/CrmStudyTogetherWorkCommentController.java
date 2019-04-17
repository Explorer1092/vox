package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructLesson;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherWorkCommentService;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.*;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CommentCourseVO;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.FeedbackVO;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.StudyPoemMapper;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.WorkCommentVO;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuerui.zhang
 * @since 2018/7/11 上午11:56
 **/
@Slf4j
@Controller
@RequestMapping("opmanager/studyTogether/workcomment")
public class CrmStudyTogetherWorkCommentController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmStudyTogetherWorkCommentService.class)
    private CrmStudyTogetherWorkCommentService commentService;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @StorageClientLocation(storage = "news-video-content")
    private StorageClient imgStorageClient;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    private static final List<Integer> LEVEL = Arrays.asList(0, 1);

    private StudyLesson getStudyLesson(String lessonId){
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }


    /**
     * 进入点评课配置页面
     */
    @RequestMapping(value = "/commentcourse.vpage", method = RequestMethod.GET)
    public String gotoCommentCoursePage(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String lessonId = getRequestString("searchLessonId");
        String courseId = getRequestString("searchCourseId");
        List<String> lessonIds = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .filter(e -> e.getSeriesType() == 1)
                .map(x -> SafeConverter.toString(x.getLessonId())).collect(Collectors.toList());

        if (StringUtils.isBlank(lessonId) && CollectionUtils.isNotEmpty(lessonIds)) {
            lessonId = lessonIds.get(0);
        }
        Page<CommentCourseVO> resultList = commentService.getStudyTogetherCommentCoursePage(lessonId, pageRequest, courseId);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("lessonIds", lessonIds);
        model.addAttribute("searchLessonId", lessonId);
        return "opmanager/studyTogether/commentcourse";
    }

    /**
     * 点评课详情页面
     */
    @RequestMapping(value = "coursedetail.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        String courseId = getRequestString("courseId");
        String lessonId = getRequestString("lessonId");
        String lid = getRequestString("lid");

        List<String> lessonIds = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .filter(e -> e.getSeriesType() == 1)
                .map(x -> SafeConverter.toString(x.getLessonId())).collect(Collectors.toList());

        StudyLesson studyLesson = getStudyLesson(lid);
        if (null != studyLesson) {
            List<CourseStructLesson> courseLessonList = studyLesson.getCourseLessonList();
//            model.addAttribute("innerIds", courseLessons);
        }

        if (StringUtils.isNotBlank(courseId)) {
            CommentCourseVO commentCourse = commentService.loadCommentCourseById(courseId);
            if (commentCourse != null) {
                model.addAttribute("course", commentCourse);
                model.addAttribute("contents", commentCourse.getContents());
            }
        }
        model.addAttribute("courseId", courseId);
        model.addAttribute("lessonIds", lessonIds);
        model.addAttribute("lessonId", lessonId);
        return "opmanager/studyTogether/coursedetail";
    }

    /**
     * 添加或修改点评课
     */
    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        // 获取参数
        String courseId = getRequestString("courseId");
        String lessonId = getRequestString("lessonId");
        Long innerId = getRequestLong("innerId");
        String title = getRequestString("title");
        String reminder = getRequestString("reminder");
        String productId = getRequestString("productId");
        String openDate = getRequestString("openDate");
        String endDate = getRequestString("endDate");
        String poemTitle = getRequestString("poemTitle");
        String author = getRequestString("author");
        String body = getRequestString("body");
        List<StudyPoemMapper> contents = JsonUtils.fromJsonToList(body, StudyPoemMapper.class);

        try {
            StudyTogetherCommentCourse commentCourse;
            if (StringUtils.isBlank(courseId)) {
                commentCourse = new StudyTogetherCommentCourse();
            } else {
                commentCourse = commentService.loadCommentCourse(courseId);
                if (commentCourse == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
            }
            commentCourse.setLessonId(lessonId);
            commentCourse.setInnerId(innerId);
            commentCourse.setTitle(title);
            commentCourse.setReminder(reminder);
            commentCourse.setProductId(productId);
            commentCourse.setOpenDate(DateUtils.stringToDate(openDate));
            commentCourse.setEndDate(DateUtils.stringToDate(endDate));
            commentCourse.setPoemTitle(poemTitle);
            commentCourse.setAuthor(author);
            commentCourse.setContents(contents.stream()
                    .sorted(Comparator.comparingInt(StudyPoemMapper::getOrder)).collect(Collectors.toList()));

            MapMessage message;
            if (StringUtils.isBlank(courseId)) {
                message = commentService.$saveCommentCourse(commentCourse);
            } else {
                commentCourse.setUpdateDate(new Date());
                commentCourse.setCourseId(courseId);
                message = commentService.$updateCommentCourse(commentCourse);
            }
            return message;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 进入作业点评页面
     */
    @RequestMapping(value = "/workervaluate.vpage", method = RequestMethod.GET)
    public String gotoWorkCommentPage(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String courseId = getRequestString("searchCourseId");
        Long studentId = getRequestLong("searchStudentId", 0L);
        int status = getRequestInt("status");
        int buy = getRequestInt("buy");
        String level = getRequestString("level");

        Page<WorkCommentVO> resultList = commentService.getStudyTogetherWorkCommentPage(pageRequest, studentId,
                courseId, status, level, buy);

        if (0L != studentId) {
            model.addAttribute("searchStudentId", studentId);
        }
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("searchCourseId", courseId);
        model.addAttribute("status", status);
        model.addAttribute("buy", buy);
        model.addAttribute("level", level);
        return "opmanager/studyTogether/workcomment";
    }

    /**
     * 作业点评详情页面
     */
    @RequestMapping(value = "evaluatedetail.vpage", method = RequestMethod.GET)
    public String evaluateDetail(Model model) {
        String evaluateId = getRequestString("evaluateId");
        String courseId = getRequestString("courseId");
        Long studentId = getRequestLong("studentId", 0L);
        Long parentId = getRequestLong("parentId");
        String studentName = getRequestString("studentName");
        String readUrl = getRequestString("readUrl");
        if (StringUtils.isNotBlank(evaluateId)) {
            StudyTogetherTeacherEvaluate teacherEvaluate = commentService.$loadTeacherEvaluateById(evaluateId);
            if (teacherEvaluate != null) {
                String voiceUrl = teacherEvaluate.getCommentVoiceUrl();
                model.addAttribute("relativeUrl", voiceUrl);
                teacherEvaluate.setCommentVoiceUrl(generateVoiceUrl(voiceUrl));
                model.addAttribute("evaluate", teacherEvaluate);
            }
        } else {
            model.addAttribute("evaluate", new StudyTogetherTeacherEvaluate());
        }
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        model.addAttribute("courseId", courseId);
        model.addAttribute("teacherName", null == adminUser ? "" : adminUser.getAdminUserName());
        model.addAttribute("studentId", studentId);
        model.addAttribute("parentId", parentId);
        model.addAttribute("evaluateId", evaluateId);
        model.addAttribute("levels", LEVEL);
        model.addAttribute("studentName", studentName);
        model.addAttribute("readUrl", readUrl);
        return "opmanager/studyTogether/evaluatedetail";
    }

    /**
     * 上传点评录音
     */
    @ResponseBody
    @RequestMapping(value = "uploadevaluate.vpage", method = RequestMethod.POST)
    public MapMessage uploadVoice(MultipartFile inputFile) {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String folder = "workcomment";
        String env = folder + File.separator;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = folder + File.separator + "test" + File.separator;
        }
        try {
            String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
            if (StringUtils.isBlank(suffix)) {
                suffix = "jpg";
            }
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String realName = imgStorageClient.upload(inputFile.getInputStream(), fileName, path);
            AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
            AliyunOSSConfig config = configManager.getAliyunOSSConfig("news-video-content");
            Objects.requireNonNull(config);
            String fileUrl = "https://" + StringUtils.defaultString(config.getHost()) + realName;
            return MapMessage.successMessage().add("path", fileUrl).add("relativeUrl", realName);
        } catch (Exception e) {
            logger.warn("File upload failed", e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 添加或修改点评课
     */
    @ResponseBody
    @RequestMapping(value = "saveevaluate.vpage", method = RequestMethod.POST)
    public MapMessage saveTeacherEvaluate() {
        // 获取参数
        String evaluateId = getRequestString("evaluateId");
        String courseId = getRequestString("courseId");
        Long studentId = getRequestLong("studentId");
        Long parentId = getRequestLong("parentId");
        String fullComment = getRequestString("fullComment");
        String teacherName = getRequestString("teacherName");
        String relativeUrl = getRequestString("relativeUrl");
        String question = getRequestString("question");
        int level = getRequestInt("level");

        if (0L == parentId) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            StudyTogetherTeacherEvaluate teacherEvaluate;
            if (StringUtils.isBlank(evaluateId)) {
                teacherEvaluate = new StudyTogetherTeacherEvaluate();
                teacherEvaluate.setId(StudyTogetherTeacherEvaluate.generateId(studentId, courseId));
            } else {
                teacherEvaluate = commentService.$loadTeacherEvaluateById(evaluateId);
                if (teacherEvaluate == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
            }
            teacherEvaluate.setFullComment(fullComment);
            teacherEvaluate.setTeacherName(teacherName);
            teacherEvaluate.setCommentVoiceUrl(relativeUrl);
            teacherEvaluate.setLevel(level);
            if (StringUtils.isNotBlank(question)) {
                teacherEvaluate.setQuestion(question);
            }
            MapMessage message;
            if (StringUtils.isBlank(evaluateId)) {
                message = commentService.$saveTeacherEvaluate(teacherEvaluate);
            } else {
                teacherEvaluate.setUpdateDate(new Date());
                teacherEvaluate.setId(evaluateId);
                message = commentService.$updateTeacherEvaluate(teacherEvaluate);
            }
            //更新学生作业信息
            StudyTogetherWorkComment workComment = new StudyTogetherWorkComment();
            if (StringUtils.isNotBlank(evaluateId)) {
                workComment.setId(evaluateId);
            } else {
                workComment.setId(StudyTogetherWorkComment.generateId(studentId, courseId));
            }
            workComment.setStatus(1);
            workComment.setLevel(level);
            commentService.$updateWorkComment(workComment);

            //PUSH 消息到 App 端
            commentService.sendEvaluateMessage(parentId, courseId);
            return message;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "getcourselist.vpage", method = RequestMethod.POST)
    public MapMessage getCourseList(String lessonId) {
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage();
        }
        StudyLesson studyLesson = getStudyLesson(lessonId);
//        List<CourseLesson> courseLessons = studyLesson.getCourseLessonList();
//        if (CollectionUtils.isNotEmpty(courseLessons)) {
//            courseLessons = courseLessons.stream()
//                    .filter(e -> StringUtils.isNotBlank(e.getLessonName()))
//                    .sorted(Comparator.comparingInt(CourseLesson::getSeq))
//                    .collect(Collectors.toList());
//        }
//        return MapMessage.successMessage().add("courseLessons", courseLessons);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "exportworkdata.vpage", method = RequestMethod.GET)
    public void exportworkdata() {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String courseId = getRequestString("courseId");
        Long studentId = getRequestLong("studentId", 0L);
        Integer status = getRequestInt("status");
        int buy = getRequestInt("buy");
        String level = getRequestString("level");

        Page<WorkCommentVO> resultList = commentService.getStudyTogetherWorkCommentPage(pageRequest, studentId, courseId, status, level, buy);
        if (resultList.getTotalElements() == 0) {
            return;
        }
        List<WorkCommentVO> exportList = new ArrayList<>(resultList.getContent());
        while (resultList.hasNext()) {
            Pageable pageable = resultList.nextPageable();
            pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
            resultList = commentService.getStudyTogetherWorkCommentPage(pageRequest, studentId, courseId, status, level, buy);
            exportList.addAll(resultList.getContent());
        }
        if (CollectionUtils.isEmpty(exportList)) {
            return;
        }
        String fileName = "作业信息-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + ".xlsx";
        List<List<String>> exportData = generateDataList(exportList);
        XSSFWorkbook xssfWorkbook;
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "家长ID",
                    "学生姓名",
                    "学生ID",
                    "所属班级",
                    "古诗课程ID",
                    "古诗名称",
                    "点评课程ID",
                    "点评状态",
                    "作业等级",
                    "创建日期",
                    "购买方式",
                    "学员问题",
            };
            int[] dateDataWidth = new int[]{5000, 5000, 5000, 5000, 5000, 5000, 8000, 5000, 5000, 5000, 5000, 5000};
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
                @Cleanup
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", outStream.toByteArray());
            } catch (Exception e) {
                logger.error("generate data error!", e);
            }
        }
    }

    /**
     * 学生反馈列表
     */
    @RequestMapping(value = "feedback.vpage", method = RequestMethod.GET)
    public String feedbackPage(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String courseId = getRequestString("searchCourseId");
        Long studentId = getRequestLong("searchStudentId", 0L);
        int satisfaction = getRequestInt("satisfaction", -1);

        Page<FeedbackVO> resultList = commentService.getFeedbackPage(pageRequest, studentId, courseId, satisfaction);
        if (0L != studentId) {
            model.addAttribute("searchStudentId", studentId);
        }
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("searchCourseId", courseId);
        model.addAttribute("satisfaction", satisfaction);
        return "opmanager/studyTogether/feedback";
    }


    @RequestMapping(value = "exportbackdata.vpage", method = RequestMethod.GET)
    public void exportBackData() {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String courseId = getRequestString("courseId");
        Long studentId = getRequestLong("studentId", 0L);
        int satisfaction = getRequestInt("satisfaction", -1);

        Page<FeedbackVO> resultList = commentService.getFeedbackPage(pageRequest, studentId, courseId, satisfaction);
        if (resultList.getTotalElements() == 0) {
            return;
        }
        List<FeedbackVO> exportList = new ArrayList<>(resultList.getContent());
        while (resultList.hasNext()) {
            Pageable pageable = resultList.nextPageable();
            pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
            resultList = commentService.getFeedbackPage(pageRequest, studentId, courseId, satisfaction);
            exportList.addAll(resultList.getContent());
        }
        if (CollectionUtils.isEmpty(exportList)) {
            return;
        }
        String fileName = "反馈结果-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + ".xlsx";
        List<List<String>> exportData = generateFeedbackDataList(exportList);
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "家长ID",
                    "学生姓名",
                    "学生ID",
                    "古诗课程ID",
                    "点评课程ID",
                    "古诗名称",
                    "提交日期",
                    "反馈结果",
                    "其他留言"
            };
            int[] dateDataWidth = new int[]{5000, 5000, 5000, 5000, 8000, 5000, 5000, 5000, 8000};
            try {
                XSSFWorkbook xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
                @Cleanup
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", outStream.toByteArray());
            } catch (Exception e) {
                logger.error("generate data error!", e);
            }
        }
    }

    private String generateVoiceUrl(String relativePath) {
        if (StringUtils.isBlank(relativePath)) {
            return "";
        }
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig config = configManager.getAliyunOSSConfig("news-video-content");
        Objects.requireNonNull(config);
        return "https://" + StringUtils.defaultString(config.getHost()) + relativePath;
    }

    private List<List<String>> generateDataList(List<WorkCommentVO> exportList) {
        List<List<String>> returnList = new ArrayList<>();
        for (WorkCommentVO bean : exportList) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(bean.getParentId()));
            list.add(SafeConverter.toString(bean.getStudentName()));
            list.add(SafeConverter.toString(bean.getStudentId()));
            String clazzInfo = "";
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(bean.getStudentId());
            if (null != clazz) {
                clazzInfo = clazz.getClassLevel() + "年级" + clazz.getClassName();
            }
            list.add(clazzInfo);//所属班级
            CommentCourseVO courseVO = commentService.loadCommentCourseById(bean.getCourseId());
            list.add(SafeConverter.toString(null == courseVO ? "" : courseVO.getLessonId()));//古诗课程ID
            list.add(SafeConverter.toString(null == courseVO ? "" : courseVO.getPoemTitle()));//古诗名称
            list.add(SafeConverter.toString(bean.getCourseId()));
            list.add(bean.getStatus() == 0 ? "未点评" : bean.getStatus() == 1 ? "已点评" : "其他");
            list.add(bean.getStatus() == 0 ? "" : SafeConverter.toInt(bean.getLevel()) == 0 ? "普通作品" : "优秀作品");
            list.add(bean.getCreateDate());
            list.add(null == bean.getBuyType() ? "" : bean.getBuyType() == 0 ? "免费课" : bean.getBuyType() == 1 ? "人民币" : bean.getBuyType() == 2 ? "学习币" : "");
            list.add(getQuestionStr(bean.getQuestion()));
            returnList.add(list);
        }
        return returnList;
    }

    private List<List<String>> generateFeedbackDataList(List<FeedbackVO> exportList) {
        List<List<String>> returnList = new ArrayList<>();
        for (FeedbackVO bean : exportList) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(bean.getParentId()));
            list.add(SafeConverter.toString(bean.getStudentName()));
            list.add(SafeConverter.toString(bean.getStudentId()));
            CommentCourseVO courseVO = commentService.loadCommentCourseById(bean.getCourseId());
            list.add(SafeConverter.toString(null == courseVO ? "" : courseVO.getLessonId()));//古诗课程ID
            list.add(SafeConverter.toString(bean.getCourseId()));
            list.add(SafeConverter.toString(null == courseVO ? "" : courseVO.getPoemTitle()));//古诗名称
            list.add(bean.getCreateDate());
            list.add(bean.getSatisfaction() == 0 ? "不满意" : "满意");
            list.add(SafeConverter.toString(null == bean.getDesc() ? "" : bean.getDesc()));
            returnList.add(list);
        }
        return returnList;
    }


    private String getQuestionStr(String qst) {
        if (StringUtils.isBlank(qst)) {
            return "";
        }
        String[] qstArr = qst.split(",");
        StringBuilder res = new StringBuilder();
        res.append('[');
        for (int i = 0; i < qstArr.length; i++) {
            String num = qstArr[i];
            String content;
            switch (num) {
                case "1":
                    content = "咬字不清";
                    break;
                case "2":
                    content = "语言不流畅";
                    break;
                case "3":
                    content = "识字错误";
                    break;
                case "4":
                    content = "固定语势";
                    break;
                case "5":
                    content = "感情不够投入";
                    break;
                case "6":
                    content = "节奏单一";
                    break;
                case "7":
                    content = "节奏变化幅度过大";
                    break;
                default:
                    content = "";
            }
            res.append(content);
            if (i != qstArr.length - 1) {
                res.append(", ");
            }
        }
        res.append("]");
        return res.toString();
    }

}


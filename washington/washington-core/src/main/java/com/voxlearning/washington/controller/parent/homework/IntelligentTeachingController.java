package com.voxlearning.washington.controller.parent.homework;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressService;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserProgress;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.EmbedPage;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * 讲练测课程api
 *
 * @author Wenlong Meng
 * @since Feb 18, 2019
 */
@Controller
@RequestMapping("/parent/IntelligentTeaching")
public class IntelligentTeachingController extends AbstractController {

    @ImportService(interfaceClass = HomeworkAssignLoader.class)
    private HomeworkAssignLoader homeworkAssignLoader;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject protected IntelDiagnosisClient intelDiagnosisClient;

    @ImportService (interfaceClass = HomeworkUserProgressService.class)
    private HomeworkUserProgressService userProgressService;
    @ImportService (interfaceClass = HomeworkUserProgressLoader.class)
    private HomeworkUserProgressLoader homeworkUserProgressLoader;

    //Logic
    /**
     * 根据课时获取课程列表
     *
     * @return
     */
    @RequestMapping(value = "/course/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage courseList(String bookId, String unitId, String sectionId, Long studentId,String bizType) {
        return homeworkUserProgressLoader.loadCourseProgresses(studentId, bookId, unitId, sectionId, bizType);
    }

    /**
     * 根据课程id获取课程信息
     *
     * @return
     */
    @RequestMapping(value = "/course/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage courseDetail(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            return MapMessage.errorMessage("请选择课程");
        }
        List<IntelDiagnosisCourse> intelDiagnosisCourses = intelDiagnosisClient.loadDiagnosisCoursesByIds(Collections.singleton(courseId));
        if(CollectionUtils.isEmpty(intelDiagnosisCourses)){
            return MapMessage.errorMessage().setInfo("无对应课程");
        }
        List<Map<String, Object>> courseInfo = toDubhe(intelDiagnosisCourses);
        return MapMessage.successMessage().set("courseInfo", courseInfo.get(0));
    }

    /**
     * 转化课程格式
     *
     * @param intelDiagnosisCourses
     * @return
     */
    private List<Map<String, Object>> toDubhe(List<IntelDiagnosisCourse> intelDiagnosisCourses) {
        List<Map<String, Object>> courseList = Lists.newArrayList();
        for (IntelDiagnosisCourse intelDiagnosisCourse : intelDiagnosisCourses) {
            Map<String, Object> course = new LinkedHashMap<>();
            Map<String, Object> theme = intelDiagnosisCourse.getTheme();
            String backgroundImage = "";
            if (MapUtils.isNotEmpty(theme)) {
                backgroundImage = SafeConverter.toString(theme.get("backgroundImage"), "");
            }
            course.put("backgroundImage", backgroundImage);
            List<EmbedPage> embedPages = intelDiagnosisCourse.getPages();
            if (CollectionUtils.isNotEmpty(embedPages)) {
                List<Map<String, Object>> pagesList = new LinkedList<>();
                for (EmbedPage embedPage : embedPages) {
                    pagesList.add(JsonUtils.convertJsonObjectToMap(embedPage.getPageContent()));
                }
                course.put("pages", pagesList);
            }
            courseList.add(course);
        }
        return courseList;
    }


    /**
     * 保存用户学习进度
     *
     * @return
     */
    @RequestMapping(value = "course/userprogress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setUserProgress() {
        long studentId = getRequestLong("studentId");
        String bizType = getRequestString("bizType");
        if (StringUtils.isBlank(bizType)) {
            return MapMessage.errorMessage("业务类型不能为空");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String sectionId = getRequestString("sectionId");
        String courseId = getRequestString("courseId");
        UserProgress userProgress = new UserProgress();
        userProgress.setBookId(bookId);
        userProgress.setUnitId(unitId);
        userProgress.setSectionId(sectionId);
        userProgress.setCourse(courseId);
        return userProgressService.save(studentId, bizType, userProgress);
    }

}

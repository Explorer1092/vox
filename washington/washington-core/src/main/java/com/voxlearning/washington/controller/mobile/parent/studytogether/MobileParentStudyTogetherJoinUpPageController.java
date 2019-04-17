package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherJoinPage;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/6/26
 */
@Controller
@RequestMapping(value = "/parentMobile/study_together/join_up_page")
public class MobileParentStudyTogetherJoinUpPageController extends AbstractMobileParentStudyTogetherController {

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;


    //报名详情页
    @RequestMapping(value = "get_join_up_page.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJoinUpPage() {
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("课程id不能为空");
        }

        StudyTogetherJoinPage joinPageByLessonId = studyTogetherServiceClient.getStudyTogetherBuffer().getJoinPageByLessonId(lessonId);
        if (joinPageByLessonId == null) {
            return MapMessage.errorMessage("页面不存在");
        }
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null) {
            return MapMessage.errorMessage("课程不存在");
        }
        Map<String, Object> pageContent = getPageContent(joinPageByLessonId);
        //TODO:体验课ID
        return MapMessage.successMessage().add("page_map", pageContent).add("experience_lesson_id", SafeConverter.toLong(studyLesson.getExperienceLessonId()));
    }

    private Map<String, Object> getPageContent(StudyTogetherJoinPage joinPageByLessonId) {
        if (joinPageByLessonId == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("lesson_id", joinPageByLessonId.getLessonId());
        returnMap.put("head_img", getOssImgUrl(joinPageByLessonId.getHeadImg()));
        returnMap.put("bg_color", joinPageByLessonId.getBgColor());
        returnMap.put("lesson_content", joinPageByLessonId.getLessonContent());
        returnMap.put("lesson_content_button", getOssImgUrl(joinPageByLessonId.getLessonContentButtonImg()));
        returnMap.put("first_content", joinPageByLessonId.getFirstContent());
        returnMap.put("second_content", joinPageByLessonId.getSecondContent());
        returnMap.put("button_color", joinPageByLessonId.getButtonColor());

        return returnMap;
    }


    private String getOssImgUrl(String relativeUrl) {
        if (StringUtils.isBlank(relativeUrl)) {
            return "";
        }
        return ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host") + relativeUrl;
    }
}

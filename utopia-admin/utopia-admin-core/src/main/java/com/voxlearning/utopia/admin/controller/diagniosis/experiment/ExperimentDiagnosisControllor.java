package com.voxlearning.utopia.admin.controller.diagniosis.experiment;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.entity.CourseAnalysisResult;
import com.voxlearning.utopia.admin.entity.UserCourseBehavior;
import com.voxlearning.utopia.admin.entity.UserQuestion;
import com.voxlearning.utopia.admin.entity.UserQuestionBehavior;
import com.voxlearning.utopia.admin.service.experiment.ExperimentDiagnosisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/7/18
 */
@Controller
@RequestMapping("/crm/experiment/diagnosis")
public class ExperimentDiagnosisControllor  extends AbstractAdminSystemController {

    @Inject
    private ExperimentDiagnosisService experimentDiagnosisService;

    @RequestMapping(value = "courseAnalysis/list.vpage", method = RequestMethod.GET)
    public String listCourseAnalysis(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        String group = getRequestString("group");
        List<CourseAnalysisResult> courseAnalysisResultList;
        if (StringUtils.isBlank(group)) {
            courseAnalysisResultList = experimentDiagnosisService.findAllCourseAnalysisResult();
        } else {
            courseAnalysisResultList = experimentDiagnosisService.findCourseAnalysisResultByGroupId(group);
        }

        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 20);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(converCourseAnalysisList(courseAnalysisResultList), pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("total", courseAnalysisResultList != null ? courseAnalysisResultList.size() : 0);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("group", group);
        return "experiment/courseAnalysisList";
    }

    private  List<Map<String, Object>> converCourseAnalysisList(List<CourseAnalysisResult> courseAnalysisResultList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(courseAnalysisResultList)) {
            return list;
        }
        courseAnalysisResultList.forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("expGroupId", e.getExpGroupId());
            bean.put("expId",e.getExpId());
            bean.put("courseId", e.getCourseId());
            bean.put("preQuestionDoNum",e.getPreQuestionDoNum());
            bean.put("preQuestionRightRate",e.getPreQuestionRightRate());
            bean.put("courseTargetNum",e.getCourseTargetNum());
            bean.put("courseBeginNum",e.getCourseBeginNum());
            bean.put("courseFinishNum",e.getCourseFinishNum());
            bean.put("courseCompleteRate",e.getCourseCompleteRate());
            bean.put("postQuestionCompleteRate",e.getPostQuestionCompleteRate());
            bean.put("postQuestionRightRate",e.getPostQuestionRightRate());
            bean.put("courseName", e.getCourseName());
            bean.put("expName", e.getExpName());
            list.add(bean);
        });
        return list;
    }

    @RequestMapping(value = "userCourse/behavior.vpage", method = RequestMethod.GET)
    public String queyUserCourseBehavior(Model model) {
        String expGroupId = getRequestString("expGroupId");
        String expId = getRequestString("expId");
        String courseId = getRequestString("courseId");
        String courseName = getRequestString("courseName");
        List<UserCourseBehavior> userCourseBehaviorList = experimentDiagnosisService.findUserCourseBehaviorByExpGroupIdAndExpIdAndCourseId(expGroupId, expId, courseId);
        List<Map<String, Object>> mapList = converUserCourseBehaviorList(userCourseBehaviorList);
        model.addAttribute("pageData", mapList);
        List<UserQuestion> userQuestionList = queryUserQuestionBehavior();
        model.addAttribute("userQuestionList",userQuestionList );
        model.addAttribute("courseName", courseName);
        model.addAttribute("courseId", courseId);
        return "experiment/userCourseBehavior";
    }

    private  List<Map<String, Object>> converUserCourseBehaviorList(List<UserCourseBehavior> userCourseBehaviorList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(userCourseBehaviorList)) {
            return list;
        }
        userCourseBehaviorList.forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("expGroupId", e.getExpGroupId());
            bean.put("expId",e.getExpId());
            bean.put("courseId", e.getCourseId());
            bean.put("page",e.getPage());
            bean.put("loadNum",e.getLoadNum());
            bean.put("quitNum",e.getQuitNum());
            bean.put("preBrowse",e.getPreBrowse());
            bean.put("postBrowse",e.getPostBrowse());
            bean.put("avgStayTime",e.getAvgStayTime());
            bean.put("defStayTime",e.getDefStayTime());
            list.add(bean);
        });
        return list;
    }

    public List<UserQuestion>  queryUserQuestionBehavior() {
        String expGroupId = getRequestString("expGroupId");
        String expId = getRequestString("expId");
        String courseId = getRequestString("courseId");
        List<UserQuestionBehavior> userQuestionBehaviorList = experimentDiagnosisService.findUserQuestionBehaviorByExpGroupIdAndExpIdAndCourseId(expGroupId, expId, courseId);
        if (CollectionUtils.isEmpty(userQuestionBehaviorList)) {
            logger.info("info","no record expId: " + expId + " ; courseId: " + courseId);
            return new ArrayList<>();
        }
        List<UserQuestion> answerList = converUserQuestionBehaviorAnswerList(userQuestionBehaviorList);
        return answerList;
    }


    private  List<Map<String, Object>> converUserQuestionBehaviorAnswerList(UserQuestionBehavior userQuestionBehavior) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (userQuestionBehavior == null || CollectionUtils.isEmpty(userQuestionBehavior.getAnswer())) {
            return list;
        }
        userQuestionBehavior.getAnswer().forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("userAnswer", e.getUserAnswer());
            bean.put("result",e.getResult());
            bean.put("rate", e.getRate());
            list.add(bean);
        });
        return list;
    }

    private  List<UserQuestion> converUserQuestionBehaviorAnswerList(List<UserQuestionBehavior> userQuestionBehaviorList) {
        List<UserQuestion> list = new ArrayList<>();
        if (userQuestionBehaviorList == null || CollectionUtils.isEmpty(userQuestionBehaviorList)) {
            return list;
        }
        userQuestionBehaviorList.forEach(q -> {
            List<Map<String, Object>> answerList = converUserQuestionBehaviorAnswerList(q);
            if (answerList == null || CollectionUtils.isEmpty(answerList)) {
                return;
            }
            UserQuestion userQuestion = new UserQuestion();
            userQuestion.setAnswerList(answerList);
            userQuestion.setAnswerSize(answerList.size());
            userQuestion.setAvgAnswerTime(q.getAvgAnswerTime());
            userQuestion.setPage(q.getPage());
            list.add(userQuestion);
        });
        return list;
    }

}


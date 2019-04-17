package com.voxlearning.utopia.admin.service.experiment;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.admin.dao.experiment.CourseAnalysisResultDao;
import com.voxlearning.utopia.admin.dao.experiment.UserCourseBehaviorDao;
import com.voxlearning.utopia.admin.dao.experiment.UserQuestionBehaviorDao;
import com.voxlearning.utopia.admin.entity.CourseAnalysisResult;
import com.voxlearning.utopia.admin.entity.UserCourseBehavior;
import com.voxlearning.utopia.admin.entity.UserQuestionBehavior;
import com.voxlearning.utopia.business.api.TeachingDiagnosisExperimentService;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * @author guangqing
 * @since 2018/7/18
 */
@Service
public class ExperimentDiagnosisService {

    @Inject
    private CourseAnalysisResultDao courseAnalysisResultDao;

    @Inject
    private UserCourseBehaviorDao userCourseBehaviorDao;

    @Inject
    private UserQuestionBehaviorDao userQuestionBehaviorDao;

    /**
     * 获取课程名称
     */
    @Inject
    protected IntelDiagnosisClient intelDiagnosisClient;
    /**
     * 获取实验名称
     */
    @ImportService(interfaceClass = TeachingDiagnosisExperimentService.class)
    private TeachingDiagnosisExperimentService teachingDiagnosisExperimentService;

    public List<CourseAnalysisResult> findAllCourseAnalysisResult() {
        List<CourseAnalysisResult> courseAnalysisResultList = courseAnalysisResultDao.findAllCourseAnalysisResult();
        handleCourseName(courseAnalysisResultList);
        handleExpName(courseAnalysisResultList);
        return courseAnalysisResultList;
    }

    public List<CourseAnalysisResult> findCourseAnalysisResultByGroupId(String expGroupId) {
        if (StringUtils.isBlank(expGroupId)) {
            return Collections.emptyList();
        }
        List<CourseAnalysisResult> courseAnalysisResultList = courseAnalysisResultDao.findCourseAnalysisResultByGroupId(expGroupId);
        handleCourseName(courseAnalysisResultList);
        handleExpName(courseAnalysisResultList);
        return courseAnalysisResultList;
    }

    private void handleExpName(List<CourseAnalysisResult> courseAnalysisResultList) {
        if (CollectionUtils.isEmpty(courseAnalysisResultList)) {
            return;
        }
        List<String> expIdList = new ArrayList<>();
        courseAnalysisResultList.forEach(c -> expIdList.add(c.getExpId()));
        Map<String, TeachingDiagnosisExperimentConfig> experimentConfigMap = teachingDiagnosisExperimentService.loadExperimentByIds(expIdList);
        if(experimentConfigMap == null || experimentConfigMap.isEmpty()){
            return;
        }
        courseAnalysisResultList.forEach(c -> {
            TeachingDiagnosisExperimentConfig config = experimentConfigMap.get(c.getExpId());
            if (config == null) {
                return;
            }
            c.setExpName(config.getName());
        });
    }

    /**
     * 处理courseName
     * @param courseAnalysisResultList
     */
    private void handleCourseName(List<CourseAnalysisResult> courseAnalysisResultList) {
        if (CollectionUtils.isEmpty(courseAnalysisResultList)) {
            return;
        }
        List<String> courseIdList = new ArrayList<>();
        courseAnalysisResultList.forEach(c -> {
            courseIdList.add(c.getCourseId());
        });
        List<IntelDiagnosisCourse> courseList = intelDiagnosisClient.loadDiagnosisCoursesByIds(courseIdList);
        if(CollectionUtils.isEmpty(courseList)){
            return;
        }
        Map<String, String> idNameMap = new HashMap<>();
        courseList.forEach(c -> {
            if (c.getId() == null || c.getName() == null) {
                return;
            }
            idNameMap.put(c.getId(), c.getName());

        });
        courseAnalysisResultList.forEach(c -> {
            String name = idNameMap.get(c.getCourseId());
            if (name == null) {
                return;
            }
            c.setCourseName(name);
        });
    }

    public List<UserCourseBehavior> findUserCourseBehaviorByExpGroupIdAndExpIdAndCourseId(String expGroupId,String expId, String courseId) {
        return userCourseBehaviorDao.findUserCourseBehaviorByExpGroupIdAndExpIdAndCourseId(expGroupId,expId, courseId);
    }

    public List<UserQuestionBehavior> findUserQuestionBehaviorByExpGroupIdAndExpIdAndCourseId(String expGroupId,String expId, String courseId) {
        return userQuestionBehaviorDao.findUserQuestionBehaviorByExpGroupIdAndExpIdAndCourseI(expGroupId, expId, courseId);
    }

}

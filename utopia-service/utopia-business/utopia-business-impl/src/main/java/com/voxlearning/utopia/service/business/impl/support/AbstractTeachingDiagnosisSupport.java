package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentGroupConfig;
import com.voxlearning.utopia.service.business.impl.persistence.*;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public abstract class AbstractTeachingDiagnosisSupport extends SpringContainerSupport {

    @Inject
    protected TeachingDiagnosisTaskDao teachingDiagnosisTaskDao;

    @Inject
    protected IntelDiagnosisClient intelDiagnosisClient;

    @Inject
    protected TeachingDiagnosisCourseQuestionResultDao teachingDiagnosisCourseQuestionResultDao;

    @Inject
    protected TeachingDiagnosisCourseResultDao teachingDiagnosisCourseResultDao;

    @Inject
    protected TeachingDiagnosisExperimentConfigDao teachingDiagnosisExperimentConfigDao;

    @Inject
    protected TeachingDiagnosisExperimentGroupConfigDao teachingDiagnosisExperimentGroupConfigDao;

    @Inject
    protected DiagnosisExperimentLogDao diagnosisExperimentLogDao;

    @Inject
    protected TeachingDiagnosisPreviewQuestionResultDao teachingDiagnosisPreviewQuestionResultDao;

    @Inject
    protected StudentLoaderClient studentLoaderClient;

    @Inject protected QuestionLoaderClient questionLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class)
    protected UserIntegralService userIntegralService;

    @Inject protected NewHomeworkDiagnosisCourseResultDao newHomeworkDiagnosisCourseResultDao;


    protected List<TeachingDiagnosisExperimentConfig> fetchOnlineDiagnosisExperiment(StudentDetail studentDetail) {
        if (studentDetail == null) {
            return Collections.emptyList();
        }
        List<TeachingDiagnosisExperimentGroupConfig> groupConfigList = teachingDiagnosisExperimentGroupConfigDao.findAll(ExperimentType.COMMON);

        if (CollectionUtils.isEmpty(groupConfigList)) {
            return Collections.emptyList();
        }

        List<TeachingDiagnosisExperimentConfig> res = new ArrayList<>();

        groupConfigList.forEach(e -> {
            List<TeachingDiagnosisExperimentConfig> experimentConfigList = teachingDiagnosisExperimentConfigDao.findByGroupId(e.getId()).stream()
                    .filter(e1 -> e1.getStatus() == TeachingDiagnosisExperimentConfig.Status.ONLINE)
                    .filter(e1 -> studentDetail.getClazzLevelAsInteger() != null //年级匹配
                            && CollectionUtils.isNotEmpty(e1.getGrades())
                            && e1.getGrades().contains(studentDetail.getClazzLevelAsInteger()))
                    .filter(e1 -> {  //地区匹配
                        Integer studentRegionCode = studentDetail.getStudentSchoolRegionCode();
                        if (studentRegionCode == null) {
                            return false;
                        }
                        String studentRegionCodeStr = studentRegionCode.toString();
                        if (CollectionUtils.isEmpty(e1.getRegions())) {
                            return false;
                        }
                        for (String code : e1.getRegions()) {
                            String reg = removeTail0(code);
                            if (StringUtils.isBlank(reg)) {
                                continue;
                            }
                            String stuCode = studentRegionCodeStr.substring(0, Math.min(studentRegionCodeStr.length(), reg.length()));
                            if (stuCode.equals(reg)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .filter(e1 -> { //流量匹配
                        if (CollectionUtils.isEmpty(e1.getLabels())) {
                            return false;
                        }
                        String userId = studentDetail.getId().toString();
                        for(String string : e1.getLabels()) {
                            if (StringUtils.isBlank(string)) {
                                continue;
                            }
                            String kt = string.trim();
                            if (userId.substring(Math.max(0, userId.length() - kt.length()), userId.length()).equals(string)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .filter(e1 -> CollectionUtils.isNotEmpty(e1.getDiagnoses()) &&
                            e1.getDiagnoses().stream().filter(e2 -> CollectionUtils.isNotEmpty(e2.getCourse_ids())).findFirst().orElse(null) != null)
                    .sorted(Comparator.comparing(TeachingDiagnosisExperimentConfig::getUpdateTime).reversed())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(experimentConfigList)) {
                res.addAll(experimentConfigList);
            }
        });
        return res;
    }

    private static String removeTail0(String str) {
        if (!str.substring(str.length() - 1).equals("0")) {
            return str;
        } else {
            return removeTail0(str.substring(0, str.length() - 1));
        }
    }
}

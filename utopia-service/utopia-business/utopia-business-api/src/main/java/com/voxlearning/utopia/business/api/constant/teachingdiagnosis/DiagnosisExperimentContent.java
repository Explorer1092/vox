package com.voxlearning.utopia.business.api.constant.teachingdiagnosis;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class DiagnosisExperimentContent implements Serializable {

    private static final long serialVersionUID = 903218648134447724L;
    private String id;
    private String name;
    private String grades;
    private String status;
    private String statusName;
    private String regions;
    private String labels;
    private String preDescription;
    private Integer bonus;
    private String previewQuestion;
    private String postQuestion;
    private List<DiagnosisExperimentConfig> configList;

    public static List<DiagnosisExperimentContent> toContentList(List<TeachingDiagnosisExperimentConfig> experimentConfigList) {
        if (CollectionUtils.isEmpty(experimentConfigList)) {
            return Collections.emptyList();
        }
        List<DiagnosisExperimentContent> result = new ArrayList<>();
        for(TeachingDiagnosisExperimentConfig config : experimentConfigList) {
            result.add(toContent(config));
        }
        return result;
    }

    public static DiagnosisExperimentContent toContent(TeachingDiagnosisExperimentConfig experimentConfig) {
        DiagnosisExperimentContent content = new DiagnosisExperimentContent();
        content.setId(experimentConfig.getId());
        content.setName(experimentConfig.getName());
        content.setGrades(CollectionUtils.isNotEmpty(experimentConfig.getGrades()) ? StringUtils.join(experimentConfig.getGrades(), ",") : "");
        content.setStatus(experimentConfig.getStatus() != null ? experimentConfig.getStatus().name() : "");
        content.setStatusName(experimentConfig.getStatus() != null ? experimentConfig.getStatus().getDescription() : "");
        content.setRegions(CollectionUtils.isNotEmpty(experimentConfig.getRegions()) ? StringUtils.join(experimentConfig.getRegions(), ",") : "");
        content.setLabels(CollectionUtils.isNotEmpty(experimentConfig.getLabels()) ? StringUtils.join(experimentConfig.getLabels(), ",") : "");
        content.setPreDescription(experimentConfig.getPreBonusDescription());
        content.setBonus(experimentConfig.getBonus());
        content.setPreviewQuestion(experimentConfig.getPreQuestion());
        content.setPostQuestion(experimentConfig.getPostQuestion());
        content.setConfigList(toConfigList(experimentConfig.getDiagnoses()));
        return content;
    }

    private static List<DiagnosisExperimentConfig> toConfigList(List<TeachingDiagnosisExperimentConfig.ExperimentCourseConfig> diagnoses) {
        if (CollectionUtils.isEmpty(diagnoses)) {
            return Collections.emptyList();
        }

        List<DiagnosisExperimentConfig> result = new ArrayList<>();
        for(TeachingDiagnosisExperimentConfig.ExperimentCourseConfig config : diagnoses) {
            DiagnosisExperimentConfig bean = new DiagnosisExperimentConfig();
            bean.setAnswers(config.getAnswers());
            bean.setCourseIds(CollectionUtils.isNotEmpty(config.getCourse_ids()) ? StringUtils.join(config.getCourse_ids(), ",") : "");
            result.add(bean);
        }
        return result;
    }
}

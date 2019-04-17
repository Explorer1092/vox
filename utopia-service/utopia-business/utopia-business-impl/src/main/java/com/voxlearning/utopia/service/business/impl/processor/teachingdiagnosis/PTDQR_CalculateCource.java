package com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.NewHomeworkDiagnosisCourseResult;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisPreviewQuestionResult;
import com.voxlearning.utopia.service.business.impl.processor.ExecuteTask;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Named
public class PTDQR_CalculateCource extends AbstractTeachingDiagnosisSupport implements ExecuteTask<PreQuestionResultContext> {

    @Override
    public void execute(PreQuestionResultContext context) {
        List<String> courseIds = new ArrayList<>();
        String questionId = context.getConfig().getPreQuestion();
        TeachingDiagnosisPreviewQuestionResult result = teachingDiagnosisPreviewQuestionResultDao.load(TeachingDiagnosisPreviewQuestionResult.generateId(context.getStudent().getId(), questionId, context.getExperimentId()));
        if (Boolean.FALSE.equals(result.getMaster())) {

            NewHomeworkDiagnosisCourseResult doCourse = newHomeworkDiagnosisCourseResultDao.load(context.getStudent().getId());

            //过滤掉订正任务中已经学过的课程
            List<TeachingDiagnosisExperimentConfig.ExperimentCourseConfig> diagnoses = context.getConfig().getDiagnoses();
            if (doCourse != null && CollectionUtils.isNotEmpty(doCourse.getCourseIds())) {
                List<TeachingDiagnosisExperimentConfig.ExperimentCourseConfig> newDiagnoses = Lists.newLinkedList();
                diagnoses.forEach(o -> {
                    List<String> courseIdList = o.getCourse_ids().stream().filter(courseId -> !doCourse.getCourseIds().contains(courseId)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(courseIdList)) {
                        o.setCourse_ids(courseIdList);
                        newDiagnoses.add(o);
                    }
                });
                diagnoses = newDiagnoses;
            }

            TeachingDiagnosisExperimentConfig.ExperimentCourseConfig courseConfig = diagnoses.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getAnswers()) && e.getAnswers().contains(result.getUserAnswer().get(0).get(0)))
                    .filter(e -> CollectionUtils.isNotEmpty(e.getCourse_ids()))
                    .findFirst().orElse(null);
            if (courseConfig == null) {
                courseConfig = diagnoses.stream()
                        .filter(e -> "@".equals(e.getAnswers()))
                        .filter(e -> CollectionUtils.isNotEmpty(e.getCourse_ids()))
                        .findFirst().orElse(null);
            }
            courseIds = Optional.ofNullable(courseConfig)
                    .map(TeachingDiagnosisExperimentConfig.ExperimentCourseConfig::getCourse_ids)
                    .orElse(Collections.emptyList());

            if (CollectionUtils.isNotEmpty(courseIds)) {//用户id取模 只取一个课程
                int index = (int) Math.floorMod(context.getStudent().getId(), courseIds.size());
                index = Math.min(courseIds.size() - 1, Math.max(index, 0));
                courseIds = Collections.singletonList(courseIds.get(index));
            }
            context.setWrongNum(1);
        } else {
            context.setWrongNum(0);
        }
        context.setCourseIds(courseIds);
        context.setTotalNum(1);
    }
}

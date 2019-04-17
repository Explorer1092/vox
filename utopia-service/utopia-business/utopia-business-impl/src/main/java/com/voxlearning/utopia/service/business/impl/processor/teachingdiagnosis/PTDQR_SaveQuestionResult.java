package com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisPreviewQuestionResult;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTask;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.service.business.impl.processor.ExecuteTask;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Named
public class PTDQR_SaveQuestionResult  extends AbstractTeachingDiagnosisSupport implements ExecuteTask<PreQuestionResultContext> {

    @Override
    public void execute(PreQuestionResultContext context) {
        TeachingDiagnosisExperimentConfig config = teachingDiagnosisExperimentConfigDao.load(context.getExperimentId());
        if (config == null || CollectionUtils.isEmpty(config.getDiagnoses()) ||
                config.getDiagnoses().stream().filter(e -> CollectionUtils.isNotEmpty(e.getCourse_ids())).findFirst().orElse(null) == null) {
            context.errorResponse("题目配置为空");
            return;
        }
        context.setConfig(config);
        if (!context.getQuestionId().equals(config.getPreQuestion())) {
            context.errorResponse("题目不匹配");
            return;
        }

        List<TeachingDiagnosisTask> taskList = teachingDiagnosisTaskDao.findByUserId(context.getStudent().getId()).stream()
                .filter(e -> StringUtils.isNotBlank(e.getExperimentGroupId()) && e.getExperimentGroupId().equals(config.getGroupId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskList)) {
            context.setErrorCode("402");
            context.errorResponse("报告已经生成，请退出然后重新进入");
            return;
        }
        TeachingDiagnosisPreviewQuestionResult previewQuestionResult = new TeachingDiagnosisPreviewQuestionResult();
        previewQuestionResult.setUpdateTime(new Date());
        previewQuestionResult.setCreateTime(new Date());
        previewQuestionResult.setDuration(context.getFinishTime());
        previewQuestionResult.setUserId(context.getStudent().getId());
        previewQuestionResult.setMaster(context.getMaster());
        previewQuestionResult.setQuestionId(context.getQuestionId());
        previewQuestionResult.setUserAnswer(context.getAnswer());
        previewQuestionResult.setExperimentId(config.getId());
        previewQuestionResult.setId(TeachingDiagnosisPreviewQuestionResult.generateId(context.getStudent().getId(), context.getQuestionId(), config.getId()));
        teachingDiagnosisPreviewQuestionResultDao.upsert(previewQuestionResult);
    }
}

package com.voxlearning.utopia.service.newexam.consumer.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.loader.EvaluationReportLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report.EvaluationAverScoreInfo;
import com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report.KnowledgeNameBO;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;

public class EvaluationReportLoaderClient implements EvaluationReportLoader {
    @ImportService(interfaceClass = EvaluationReportLoader.class)
    private EvaluationReportLoader remoteReference;

    @Override
    public MapMessage fetchNewExamSkillInfo(String newExamId) {
        return remoteReference.fetchNewExamSkillInfo(newExamId);
    }

//    @Override
//    public MapMessage fetchBigData(String newExamId) {
//        return remoteReference.fetchBigData(newExamId);
//    }

    @Override
    public Map<String, KnowledgeNameBO> fetchKnowledgeName(NewPaper newPaper) {
        return remoteReference.fetchKnowledgeName(newPaper);
    }

    @Override
    public MapMessage fetchEvaluationReport(String newExamId, User user) {
        return remoteReference.fetchEvaluationReport(newExamId, user);
    }

    @Override
    public MapMessage shareEvaluationReportToJzt(Teacher teacher, String newExamId) {
        return remoteReference.shareEvaluationReportToJzt(teacher, newExamId);
    }

    @Override
    public List<EvaluationAverScoreInfo> fetchAverScoreByExamIds(Long groupId, String unitId) {
        return remoteReference.fetchAverScoreByExamIds(groupId, unitId);
    }
}

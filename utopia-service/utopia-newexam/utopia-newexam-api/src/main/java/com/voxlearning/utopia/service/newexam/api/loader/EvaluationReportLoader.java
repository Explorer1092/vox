package com.voxlearning.utopia.service.newexam.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report.EvaluationAverScoreInfo;
import com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report.KnowledgeNameBO;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180303")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface EvaluationReportLoader extends IPingable {

    MapMessage fetchNewExamSkillInfo(String newExamId);

//    MapMessage fetchBigData(String newExamId);

    Map<String, KnowledgeNameBO> fetchKnowledgeName(NewPaper newPaper);

    MapMessage fetchEvaluationReport(String newExamId, User user);


    MapMessage shareEvaluationReportToJzt(Teacher teacher, String newExamId);


    List<EvaluationAverScoreInfo> fetchAverScoreByExamIds(Long groupId, String unitId);
}

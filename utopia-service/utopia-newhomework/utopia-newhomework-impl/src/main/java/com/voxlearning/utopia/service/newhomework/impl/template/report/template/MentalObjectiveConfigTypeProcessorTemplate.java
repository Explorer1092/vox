package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.TeacherReportParameter;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class MentalObjectiveConfigTypeProcessorTemplate extends CommonObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL;
    }

    @Override
    public void handleKnowledgePointStatistics(TeacherReportParameter teacherReportParameter, NewHomeworkResultAnswer newHomeworkResultAnswer) {
        teacherReportParameter.handleKnowledgePointStatistics(newHomeworkResultAnswer);
    }

    @Override
    public void initKnowledgePointIdToQIds(TeacherReportParameter teacherReportParameter) {
        teacherReportParameter.initKnowledgePointIdToQIds();
    }

}

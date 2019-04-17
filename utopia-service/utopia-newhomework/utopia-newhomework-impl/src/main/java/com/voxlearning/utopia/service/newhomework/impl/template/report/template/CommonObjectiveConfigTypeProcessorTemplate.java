package com.voxlearning.utopia.service.newhomework.impl.template.report.template;


import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.TeacherReportParameter;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CommonObjectiveConfigTypeProcessorTemplate extends ObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    @Override
    public void processor(TeacherReportParameter teacherReportParameter) {
        Map<String, NewHomeworkResult> newHomeworkResultMap = teacherReportParameter.getNewHomeworkResultMap();
        ObjectiveConfigType type = teacherReportParameter.getType();


        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());

        initKnowledgePointIdToQIds(teacherReportParameter);

        handleBaseAppType(teacherReportParameter, teacherReportParameter.isPcWay());

        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer typeDuration = newHomeworkResultAnswer.processDuration();
            typeDuration = Objects.nonNull(typeDuration) ? typeDuration : 0;
            typeDuration = new BigDecimal(typeDuration).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue();

            //口算的知识点统计
            handleKnowledgePointStatistics(teacherReportParameter, newHomeworkResultAnswer);

            //总共时间
            teacherReportParameter.setNewTotalFinishTime(teacherReportParameter.getNewTotalFinishTime() + typeDuration);
            //类型完成人数
            teacherReportParameter.handleHomeworkTypeFinishCount(type);

            //类型时间
            teacherReportParameter.handleHomeworkTypeDuration(type, typeDuration);

            if (teacherReportParameter.getTypeQuestionsInfo().containsKey(type)) {
                //类型每题正确率的统计
                teacherReportParameter.handleTypeQuestionsInfoStatistics(type, newHomeworkResultAnswer);
            }
            if (type.isSubjective()) {
                if (newHomeworkResultAnswer.getCorrectedAt() == null) {
                    if (teacherReportParameter.getHomeworkTypeUnCorrect().get(type) == null) {
                        teacherReportParameter.getHomeworkTypeUnCorrect().put(type, 1);
                    } else {
                        teacherReportParameter.getHomeworkTypeUnCorrect().put(type, teacherReportParameter.getHomeworkTypeUnCorrect().get(type) + 1);
                    }
                }
            } else {
                // 类型分数
                int score = SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                if (teacherReportParameter.getHomeworkTypeScore().get(type) == null) {
                    teacherReportParameter.getHomeworkTypeScore().put(type, score);
                } else {
                    teacherReportParameter.getHomeworkTypeScore().put(type, teacherReportParameter.getHomeworkTypeScore().get(type) + score);
                }

            }
        }
    }


    //用于子类扩展
    @Override
    public void handleKnowledgePointStatistics(TeacherReportParameter teacherReportParameter, NewHomeworkResultAnswer newHomeworkResultAnswer) {

    }

    //用于子类扩展
    @Override
    public void handleBaseAppType(TeacherReportParameter teacherReportParameter, boolean isPcWay) {

    }

    //用于子类扩展
    @Override
    public void initKnowledgePointIdToQIds(TeacherReportParameter teacherReportParameter) {

    }


}

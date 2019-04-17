package com.voxlearning.utopia.service.newhomework.impl.template;


import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract public class ProcessNewHomeworkAnswerDetailTemplate extends NewHomeworkSpringBean {

    abstract public ObjectiveConfigType getObjectiveConfigType();

    //按学生查看部分各个类型模板接口==》pc
    abstract public String processStudentPartTypeScore(NewHomework newHomework ,NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type);

    //按题查看部分各个类型模板接口==>pc
    abstract public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl);

    //获取整个班级整份作业报告接口==>pc、老手机端h5
    abstract public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext);

    //获取单个学生整份作业模板接口==》pc,手机端h5
    abstract public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext);

    //获取单个类型报告模板接口==>h5
    abstract public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context);

    //获取单个题信息的模板接口==》h5
    abstract public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context);

    protected Map<String, NewHomeworkResult> handlerNewHomeworkResultMap(Map<Long, NewHomeworkResult> newHomeworkResultMap, ObjectiveConfigType objectiveConfigType) {
        return newHomeworkResultMap
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(objectiveConfigType))
                .collect(Collectors
                        .toMap(NewHomeworkResult::getId, Function.identity()));
    }
}

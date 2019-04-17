package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailReadReciteTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READ_RECITE;
    }


    @Override
    public String processStudentPartTypeScore(NewHomework newHomework,NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        if (newHomeworkResultAnswer.isCorrected()){
            return "已批改";
        }else{
            return "未批改";
        }
    }
    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            typeResult.put("finishCount", 0);
            result.put(type, typeResult);
            return;
        }
        int finishCount = 0;
        int unCorrectCount = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            finishCount++;
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            if (newHomeworkResultAnswer.isCorrected()) {
                unCorrectCount++;
            }
        }
        //完成人数，待批改人数
        typeResult.put("finishCount", finishCount);
        typeResult.put("unCorrectCount", unCorrectCount);
        result.put(type, typeResult);
    }
}

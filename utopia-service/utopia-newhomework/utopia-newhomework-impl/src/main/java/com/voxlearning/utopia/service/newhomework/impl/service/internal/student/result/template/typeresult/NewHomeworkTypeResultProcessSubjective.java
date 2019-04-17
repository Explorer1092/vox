package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

/**
 * @author guoqiang.li
 * @since 2017/7/31
 */
@Named
public class NewHomeworkTypeResultProcessSubjective extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.SUBJECTIVE;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        Collection<String> processResultIds = new ArrayList<>();
        if (MapUtils.isEmpty(resultAnswer.getAnswers())) {
            logger.error("processHomeworkTypeResult EXAM error, homeworkId:{}, studentId:{}",
                    baseHomeworkResult.getHomeworkId(),
                    baseHomeworkResult.getUserId());
        } else {
            processResultIds = resultAnswer.getAnswers().values();
        }
        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        List<Map<String, Object>> results = new ArrayList<>();
        for (String processResultId : resultAnswer.getAnswers().values()) {
            NewHomeworkProcessResult nhpr = nhprMap.get(processResultId);
            results.add(MapUtils.m(
                    "questionId", nhpr.getQuestionId(),
                    "review", nhpr.getReview() != null && nhpr.getReview(),
                    "correction", nhpr.getCorrection() != null ? nhpr.getCorrection().getDescription() : ""));
        }
        return MapMessage.successMessage().add("datas", results);
    }
}

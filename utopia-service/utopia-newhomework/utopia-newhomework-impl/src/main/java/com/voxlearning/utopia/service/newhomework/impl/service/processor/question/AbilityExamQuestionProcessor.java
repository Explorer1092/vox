package com.voxlearning.utopia.service.newhomework.impl.service.processor.question;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamQuestion;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.AbilityExamSpringBean;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamQuestionProcessor extends AbilityExamSpringBean {

    public Map<String, Object> loadQuestions(Long studentId) {
        Map<String, Object> returnData = new HashMap<>();
        AbilityExamBasic basic = abilityExamBasicDao.load(String.valueOf(studentId));
        if (basic == null) {
            return Collections.emptyMap();
        }
        returnData.put("eids", basic.getQuestionIds());
        returnData.put("examUnitMap", Collections.emptyMap()); // 占位没用
        return returnData;
    }

    public Map<String, Object> loadQuestionAnswer(Long studentId) {
        Map<String, Object> returnData = new HashMap<>();
        AbilityExamQuestion question = abilityExamQuestionDao.load(String.valueOf(studentId));
        if (question == null) {
            return Collections.emptyMap();
        }
        for (String questionId : question.getFinishedQuestions().keySet()) {
            Map<String, Object> value = MapUtils.m(
                    "subMaster", new ArrayList<>(Collections.singleton(question.getFinishedQuestions().get(questionId).toString())),
                    "master", question.getFinishedQuestions().get(questionId),
                    "userAnswers", new ArrayList<>(Collections.emptyList()),
                    "score", 0
            );
            returnData.put(questionId, value);
        }
        return returnData;
    }

}

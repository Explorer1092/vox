package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 作业结果封装
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
public class HomeworkMapMessageProcessor implements HomeworkProcessor {

    //Local variables

    //Logic
    /**
     * 作业结果封装
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        Map<String, Object> result = new HashMap<>();
        hc.getHomeworkProcessResults().forEach(hpr->
                result.put(hpr.getQuestionId(),MapUtils.m(
                        "subMaster", hpr.getUserSubGrasp(),
                        "master", hpr.getRight(),
                        "subScore", hpr.getUserSubScore(),
                        "userAnswers", hpr.getUserAnswers(),
                        "answers", hpr.getAnswers(),
                        "fullScore", hpr.getScore(),
                        "score",hpr.getScore()
                ))
        );
        hc.setMapMessage(MapMessage.successMessage().add("result", result));
    }

}

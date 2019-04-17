package com.voxlearning.utopia.service.parent.homework.impl.template.correct.processor;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 作业结果封装
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
public class MapMessageSubmitCorrectProcessor implements IProcessor<CorrectContext> {

    //Local variables

    //Logic
    /**
     * 作业结果封装
     *
     * @param c args
     * @return result
     */
    @Override
    public void process(CorrectContext c) {
        Map<String, Object> result = new HashMap<>();
        c.getHomeworkProcessResults().forEach(hpr->
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
        c.setMapMessage(MapMessage.successMessage().add("result", result));
    }

}

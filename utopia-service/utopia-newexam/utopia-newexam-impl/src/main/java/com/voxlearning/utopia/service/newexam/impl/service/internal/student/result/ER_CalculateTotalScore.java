package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;

import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
public class ER_CalculateTotalScore extends SpringContainerSupport implements NewExamResultTask {
    @Override
    public void execute(NewExamResultContext context) {
        double score = context.getCurrentProcessResult().getScore() != null ? context.getCurrentProcessResult().getScore(): 0;

        if(context.getOldProcessResult() != null ){
            double oldScore = context.getOldProcessResult().getScore() != null ? context.getOldProcessResult().getScore() : 0;
            score = score - oldScore;
        }
        context.setTotalScore(SafeConverter.toDouble(context.getNewExamResult().getScore()) + score);
    }
}

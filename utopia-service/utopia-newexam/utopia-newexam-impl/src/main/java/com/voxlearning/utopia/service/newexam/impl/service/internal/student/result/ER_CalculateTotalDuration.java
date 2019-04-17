package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;

import javax.inject.Named;
import java.util.Date;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
public class ER_CalculateTotalDuration extends SpringContainerSupport implements NewExamResultTask {
    @Override
    public void execute(NewExamResultContext context) {
        Long durationMilliseconds = new Date().getTime() - context.getNewExamResult().getFlightRecorderTime().getTime();
        Long totalDurationMilliseconds = SafeConverter.toLong(context.getNewExamResult().getDurationMilliseconds()) + durationMilliseconds;
        long remainingTime = context.getNewExam().getDurationMinutes() * 60 * 1000 - totalDurationMilliseconds;
        //+ 60*1000目的是延长一分钟，来解决页面记时到提交后端可能出现的短暂时差。
//        if((remainingTime + 60*1000) <= 0 ){
//            context.errorResponse("您的测试时间已用尽，无法继续答题。");
//            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_REMAINING_TIME_NOT_ENOUGH);
//            return;
//        }
        context.setTotalDureation(totalDurationMilliseconds);
    }
}

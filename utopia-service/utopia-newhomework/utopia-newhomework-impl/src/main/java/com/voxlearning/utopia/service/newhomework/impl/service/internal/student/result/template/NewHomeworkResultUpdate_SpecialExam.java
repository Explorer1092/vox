package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;

import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/1/19
 */
@Named
public class NewHomeworkResultUpdate_SpecialExam extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.SPECIAL_EXAM;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();
        for (NewHomeworkProcessResult npr : processResultMap.values()) {
            newHomeworkResultService.doHomeworkExamAnswer(
                    location,
                    context.getUserId(),
                    npr.getObjectiveConfigType(),
                    npr.getQuestionId(),
                    npr.getId());
        }
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {

    }
}

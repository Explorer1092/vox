package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Map;

/**
 * @Description: 听写
 * @author: Mr_VanGogh
 * @date: 2019/1/23 下午3:52
 */
@Named
public class NewHomeworkResultUpdate_Dictation extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.DICTATION;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        if (context.getObjectiveConfigType().equals(ObjectiveConfigType.OCR_DICTATION)) {
            return;
        }
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

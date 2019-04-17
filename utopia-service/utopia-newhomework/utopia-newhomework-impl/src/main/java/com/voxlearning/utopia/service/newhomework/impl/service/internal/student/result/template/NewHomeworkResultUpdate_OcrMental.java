package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;

@Named
public class NewHomeworkResultUpdate_OcrMental extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.OCR_MENTAL_ARITHMETIC;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {

    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {

    }
}

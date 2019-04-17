package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/3/1
 */
@Named
public class NewHomeworkResultUpdate_SpecialApp extends NewHomeworkResultUpdateTemplate {
    @Inject private NewHomeworkResultUpdate_BasicApp newHomeworkResultUpdate_basicApp;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.SPECIAL_APP;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        newHomeworkResultUpdate_basicApp.processHomeworkContent(context);
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {
        newHomeworkResultUpdate_basicApp.checkNewHomeworkAppFinish(context);
    }
}

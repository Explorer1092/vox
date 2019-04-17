package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;


import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkProcessResultService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/10/25
 * \* Time: 4:21 PM
 * \* Description: 图片鉴黄，针对题目类型： 纸质口算 。。。
 * \
 */
@Named
public class FH_ClassifyImage extends SpringContainerSupport implements FinishHomeworkTask {

    @Inject private NewHomeworkProcessResultService newHomeworkProcessResultService;

    @Override
    public void execute(FinishHomeworkContext context) {
        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == context.getObjectiveConfigType() && CollectionUtils.isNotEmpty(context.getOcrMentalAnswerIds())) {
            AlpsThreadPool
                    .getInstance()
                    .submit(() -> newHomeworkProcessResultService.classifyImage(context.getHomeworkId(), context.getUserId(), context.getOcrMentalAnswerIds()));
        } else if (ObjectiveConfigType.OCR_DICTATION == context.getObjectiveConfigType() && CollectionUtils.isNotEmpty(context.getOcrDictationAnswerIds())) {
            AlpsThreadPool
                    .getInstance()
                    .submit(() -> newHomeworkProcessResultService.classifyOcrDictationImage(context.getHomeworkId(), context.getUserId(), context.getOcrDictationAnswerIds()));
        }
    }
}

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.FinishHomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/14
 */
@Named
public class HR_FinishHomework extends SpringContainerSupport implements HomeworkResultTask {
    @Inject private FinishHomeworkProcessor finishHomeworkProcessor;

    @Override
    public void execute(HomeworkResultContext context) {
        FinishHomeworkContext ctx = new FinishHomeworkContext();
        ctx.setUserId(context.getUserId());
        ctx.setUser(context.getUser());
        ctx.setClazzGroupId(context.getClazzGroupId());
        ctx.setClazzGroup(context.getClazzGroup());
        ctx.setHomeworkId(context.getHomeworkId());
        ctx.setHomework(context.getHomework());
        ctx.setNewHomeworkType(context.getNewHomeworkType());
        ctx.setObjectiveConfigType(context.getObjectiveConfigType());
        ctx.setClientType(context.getClientType());
        ctx.setClientName(context.getClientName());
        ctx.setIpImei(context.getIpImei());
        ctx.setSupplementaryData(false);
        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == context.getObjectiveConfigType()) {
            if (CollectionUtils.isNotEmpty(context.getOcrMentalProcessResults())) {
                ctx.setOcrMentalAnswerIds(context.getOcrMentalProcessResults().stream().map(NewHomeworkProcessResult::getId).collect(Collectors.toList()));
            }
            long duration = SafeConverter.toLong(context.getConsumeTime());
            if (duration > NewHomeworkConstants.OCR_MENTAL_MAX_DURATION_MILLISECONDS) {
                duration = NewHomeworkConstants.OCR_MENTAL_MAX_DURATION_MILLISECONDS;
            }
            ctx.setPracticeDureation(duration);
        }
        if (ObjectiveConfigType.OCR_DICTATION == context.getObjectiveConfigType()) {
            if (CollectionUtils.isNotEmpty(context.getOcrDictationProcessResults())) {
                ctx.setOcrDictationAnswerIds(context.getOcrDictationProcessResults().stream().map(NewHomeworkProcessResult::getId).collect(Collectors.toList()));
            }
            ctx.setPracticeDureation(context.getConsumeTime());
        }

        AlpsThreadPool.getInstance().submit(() -> finishHomeworkProcessor.process(ctx));
    }
}

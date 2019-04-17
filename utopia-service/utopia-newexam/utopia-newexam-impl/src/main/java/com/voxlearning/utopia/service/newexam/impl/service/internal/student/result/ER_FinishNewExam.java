package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.FinishNewExamContext;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.service.internal.student.finished.FinishNewExamProcessor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
public class ER_FinishNewExam extends SpringContainerSupport implements NewExamResultTask {
    @Inject private FinishNewExamProcessor finishNewExamProcessor;

    @Override
    public void execute(NewExamResultContext context) {
        FinishNewExamContext ctx = new FinishNewExamContext();
        ctx.setCurrentProcessResult(context.getCurrentProcessResult());
        EventBus.execute(() -> finishNewExamProcessor.process(ctx));
    }
}

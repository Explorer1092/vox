package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkAppTimingMessageSender;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * @author xinxin
 * @since 29/7/2016
 */
@Named
public class PostFinishNewHomeworkSendAppTimingMessage extends SpringContainerSupport implements PostFinishHomework {
    @Inject
    private NewHomeworkAppTimingMessageSender newHomeworkAppTimingMessageSender;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        newHomeworkAppTimingMessageSender.sendTimingMessage(context);
    }
}

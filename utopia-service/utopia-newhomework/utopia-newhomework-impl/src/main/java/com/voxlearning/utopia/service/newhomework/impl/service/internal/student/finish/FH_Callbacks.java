package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/16
 */
@Named
public class FH_Callbacks extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private PostFinishNewHomeworkPublishMessage postFinishNewHomeworkPublishMessage;
    @Inject private PostFinishMentalArithmeticPublishMessage postFinishMentalArithmeticPublishMessage;
    @Inject private PostFinishNewHomeworkMakeUpRelevant postFinishHomeworkMakeUpRelevant;
    @Inject private PostFinishNewHomeworkUserActivity postFinishHomeworkUserActivity;
    @Inject private PostFinishNewHomeworkPrize postFinishNewHomeworkPrize;
    @Inject private PostFinishNewHomeworkSendAppTimingMessage postFinishNewHomeworkSendAppTimingMessage;
    @Inject private PostFinishNewHomeworkActionEvent postFinishNewHomeworkActionEvent;
    @Inject private PostFinishGenerateSelfStudyHomework postFinishGenerateSelfStudyHomework;
    @Inject private PostFinishHomeworkAddPopup postFinishHomeworkAddPopup;
    @Inject private PostFinishHomeworkUpdateHomeworkTask postFinishHomeworkUpdateHomeworkTask;
    @Inject private PostFinishMothersDayHomeworkParentMessage postFinishMothersDayHomeworkParentMessage;
    @Inject private PostFinishKidsDayHomeworkParentMessage postFinishKidsDayHomeworkParentMessage;
    @Inject private PostFinishNewHomeworkMagicCastle postFinishNewHomeworkMagicCastle;

    private final List<PostFinishHomework> callbacks = new LinkedList<>();
    private final List<PostFinishHomework> basicReviewCallbacks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        callbacks.add(postFinishNewHomeworkPublishMessage);
        callbacks.add(postFinishMentalArithmeticPublishMessage);
        callbacks.add(postFinishHomeworkMakeUpRelevant);
        callbacks.add(postFinishHomeworkUserActivity);
        callbacks.add(postFinishNewHomeworkMagicCastle);
        callbacks.add(postFinishNewHomeworkPrize);
        callbacks.add(postFinishNewHomeworkSendAppTimingMessage);
        callbacks.add(postFinishNewHomeworkActionEvent);
        callbacks.add(postFinishGenerateSelfStudyHomework);
        callbacks.add(postFinishHomeworkAddPopup);
        callbacks.add(postFinishHomeworkUpdateHomeworkTask);
        callbacks.add(postFinishMothersDayHomeworkParentMessage);
        callbacks.add(postFinishKidsDayHomeworkParentMessage);

        basicReviewCallbacks.add(postFinishNewHomeworkPublishMessage);
    }

    @Override
    public void execute(FinishHomeworkContext context) {

        List<PostFinishHomework> executeCallbacks = callbacks;
        if (StringUtils.equalsIgnoreCase(context.getNewHomeworkType().name(), NewHomeworkType.BasicReview.name())) {
            executeCallbacks = basicReviewCallbacks;
        }

        for (PostFinishHomework callback : executeCallbacks) {
            try {
                callback.afterHomeworkFinished(context);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}

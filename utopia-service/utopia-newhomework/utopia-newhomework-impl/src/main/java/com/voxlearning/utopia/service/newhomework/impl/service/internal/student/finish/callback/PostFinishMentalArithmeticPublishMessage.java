package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 学生做完限时、有奖口算训练作业后，在消息中心推送过程性奖励通知
 *
 * @author zhangbin
 * @since 2018/1/31
 */

@Named
public class PostFinishMentalArithmeticPublishMessage extends SpringContainerSupport implements PostFinishHomework {
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        NewHomework newHomework = context.getHomework();
        if (newHomework == null) {
            return;
        }

        //口算过程奖励消息
        if (context.getMentalArithmeticProgressReward()) {
            String title = "口算训练过程奖励已发放";
            String content = "口算训练过程性奖励已经发放，请查收。";
            String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/currentmonth/history/detail.vpage",
                    MiscUtils.m(
                            "subject", newHomework.getSubject(),
                            "homeworkId", context.getHomeworkId()));
            AppMessage message = new AppMessage();
            message.setUserId(context.getUserId());
            message.setMessageType(StudentAppPushType.METAL_ARITHMETIC_AWARD_REMIND.getType());
            message.setTitle(title);
            message.setContent(content);
            message.setLinkUrl(link);
            message.setLinkType(1); // 站内的相对地址
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
        }
    }


}
